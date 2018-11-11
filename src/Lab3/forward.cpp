/*
* THIS FILE IS FOR IP FORWARD TEST
*/
#include "sysInclude.h"
#include <map>

// system support
extern void fwd_LocalRcv(char *pBuffer, int length);

extern void fwd_SendtoLower(char *pBuffer, int length, unsigned int nexthop);

extern void fwd_DiscardPkt(char *pBuffer, int type);

extern unsigned int getIpv4Address();

// implemented by students

typedef struct ip_header
{
    char version_header_len;       // version + header length
    char ToS;                      // Tos
    unsigned short total_length;   // total length
    unsigned short identification; // identification
    unsigned short flag_offset;    // flag and offset
    char ttl;                      // ttl
    char protocol;                 //  protocol type
    unsigned short checksum;       // check sum
    unsigned int srcAddr;          // source address
    unsigned int dstAddr;          // destiny address
} ip_header_type;

map<unsigned int, unsigned int> table;

void stud_Route_Init()
{
    table.clear();
    return;
}

void stud_route_add(stud_route_msg *proute)
{
    unsigned int destAdr = (ntohl(proute->dest)) & (0xffffffff << (32 - htonl(proute->masklen)));
    unsigned int nextAdr = (ntohl(proute->nexthop));
    table.insert(map<unsigned int, unsigned int>::value_type(destAdr, nextAdr));
    return;
}

int stud_fwd_deal(char *pBuffer, int length)
{
    void *ip;
    ip = pBuffer;
    unsigned int destAdr = ntohl(((ip_header_type *)pBuffer)->dstAddr);

    if (destAdr == getIpv4Address())
    {
        fwd_LocalRcv(pBuffer, length);
        return 0;
    }
    // ttl equal to 0 , means it can't be transported anymore
    if (((ip_header_type *)ip)->ttl == 0)
    {
        fwd_DiscardPkt(pBuffer, STUD_IP_TEST_TTL_ERROR);
        return 1;
    }
    map<unsigned int, unsigned int>::iterator iterator;
    iterator = table.find(destAdr);
    if (iterator != table.end())
    {
        ((ip_header_type *)ip)->ttl -= 1;
        // update the check sum
        unsigned int sum = 0;
        for (int i = 0; i < 10; i++)
        {
            if (i != 5)
            {
                sum += (int)(*((unsigned char *)ip + 2 * i) << 8);
                sum += (int)(*((unsigned char *)ip + 2 * i + 1));
            }
        }
        while ((sum & 0xffff0000) != 0)
        {
            sum = (sum & 0xffff) + ((sum >> 16) & 0xffff);
        }
        ((ip_header_type *)ip)->checksum = htons(~sum);
        fwd_SendtoLower(pBuffer, length, iterator->second);
        return 0;
    }
    else
    { // can't find next ip from forward table
        fwd_DiscardPkt(pBuffer, STUD_FORWARD_TEST_NOROUTE);
        return 1;
    }
}
