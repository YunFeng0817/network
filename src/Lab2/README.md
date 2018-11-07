# 可靠数据传输协议-GBN协议的设计与实现

最终的代码实现的是 `SR` 的协议传输

发送数据报格式
base(1byte) seq(1byte) + data
ACK 数据报格式
seq(1byte)

主程序运行方式及原理:

- 先运行Class Client 中的 main function  , 开始向localhost:7070 通过SR协议发送1.png文件
- 然后运行 Class Server 中的 main function , 开始接受来自 localhost:8080 发送的1.png ,保存为 2.png
- 当Server main function 接收完文件后,开始向 Client main function 发送3.png文件
- Client main function 开始接收3.png, 并保存为 4.png文件

通过两个进程的互相发送文件,表明自己的代码实现可以实现双向通信,代码中已经加入了模拟丢包并重发的过程

