/*
* THIS FILE IS FOR IP TEST
*/
// system support
#include "sysInclude.h"

extern void ip_DiscardPkt(char *pBuffer, int type);

extern void ip_SendtoLower(char *pBuffer, int length);

extern void ip_SendtoUp(char *pBuffer, int length);

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

int stud_ip_recv(char *pBuffer, unsigned short length)
{
    void *ip;
    ip = pBuffer;
    if (((ip_header_type *)ip)->version_header_len >> 4 != 4)
    {
        ip_DiscardPkt(pBuffer, STUD_IP_TEST_VERSION_ERROR);
        return 1;
    }
    else if ((((ip_header_type *)ip)->version_header_len & 0xf) < 5)
    {
        ip_DiscardPkt(pBuffer, STUD_IP_TEST_HEADLEN_ERROR);
        return 1;
    }
    else if (((ip_header_type *)ip)->ttl == 0)
    {
        ip_DiscardPkt(pBuffer, STUD_IP_TEST_TTL_ERROR);
        return 1;
    }
    else if (ntohl(((ip_header_type *)ip)->dstAddr) != getIpv4Address() && ntohl(((ip_header_type *)ip)->dstAddr) != 0xffffffff)
    {
        ip_DiscardPkt(pBuffer, STUD_IP_TEST_DESTINATION_ERROR);
        return 1;
    }
    else
    {
        // caculate check sum
        unsigned int sum = 0;
        for (int i = 0; i < 10; i++)
        {
            sum += (unsigned int)(*((unsigned char *)ip + 2 * i) << 8);
            sum += (unsigned int)(*((unsigned char *)ip + 2 * i + 1));
        }
        while ((sum & 0xffff0000) != 0)
        {
            sum = (sum & 0xffff) + ((sum >> 16) & 0xffff);
        }
        unsigned short short_check = ~sum;
        if (short_check != 0)
        {
            ip_DiscardPkt(pBuffer, STUD_IP_TEST_CHECKSUM_ERROR);
            return 1;
        }
        ip_SendtoUp((pBuffer + sizeof(ip_header_type)), length - sizeof(ip_header_type));
        return 0;
    }
}

int stud_ip_Upsend(char *pBuffer, unsigned short len, unsigned int srcAddr,
                   unsigned int dstAddr, byte protocol, byte ttl)
{
    unsigned int seed = 5;
    unsigned int r;
    void *ip;
    srand(seed);
    r = rand();
    ip = malloc(sizeof(ip_header_type) + len);
    ((ip_header_type *)ip)->version_header_len = (4 << 4) + 5;
    ((ip_header_type *)ip)->ToS = 0;
    ((ip_header_type *)ip)->total_length = htons(sizeof(ip_header_type) + len);
    ((ip_header_type *)ip)->identification = htons(r);
    ((ip_header_type *)ip)->flag_offset = htons(0);
    ((ip_header_type *)ip)->ttl = ttl;
    ((ip_header_type *)ip)->protocol = protocol;
    ((ip_header_type *)ip)->srcAddr = htonl(srcAddr);
    ((ip_header_type *)ip)->dstAddr = htonl(dstAddr);
    // calculate check sum
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

    for (int i = 0; i < len; i++)
    {
        *((char *)ip + sizeof(ip_header_type) + i) = *(pBuffer + i);
    }
    ip_SendtoLower((char *)ip, len + sizeof(ip_header_type));
    return 0;
}
