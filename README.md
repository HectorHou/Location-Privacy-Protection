# 基于k-匿名的位置隐私保护系统 Java + Netty + protobuf + Maven
====================================================================

## 背景简介
定位服务和基于位置服务在为用户的日常生活带来极大便利的同时，又不可避免地导致一定程度上用户隐私信息的泄露。为了使用户在使用定位服务和基于位置的服务时可以保护自己的位置信息隐私，提出了一种基于k-匿名的保护位置隐私和轨迹隐私的策略，并开发定位服务器和代理匿名服务器予以测试和验证。

## 定位方式分析
* 基于GPS的定位 - LocationManager.GPS_PROVIDER（Android） - 卫星定位，卫星发信号，手机接受信号并计算出经纬度，安全
* 基于网络的定位 - LocationManager.NETWORK_PROVIDE（Android）
  * 基站定位
  * Wifi定位 - 主流
    * 三角定位 - 已知Access Points（APs）的位置，根据信号衰减定位
    * 指纹库定位（主流） - 根据已知位置收集到的APs和Signal Strength（SS）制作指纹库，根据用户收集到的APs + SS匹配指纹库进行定位

## 基于k-匿名的位置隐私保护方式

### k-匿名
* 要求发布的数据中存在一定数量(至少为k) 的在准标识符上不可区分的记录,使攻击者不能判别出隐私信息所属的具体个体,从而保护了个人隐私
* 位置k匿名的目标:构造k-1个假数据,使攻击者无法分辨具体的用户数据

### 基于图模型的匿名原理
* 在某位置收集到的信息（APs + SS) -> 搜集到的AP覆盖区域相互重叠 -> 在无向图中任意两个AP有边（完全图）
* 用户发给位置服务器的信息必是全局AP信息图模型G的某个完全子图G'中的APs + SS
* 从全局AP信息图模型G中找出k-1组包含n个结点的近完全子图，即实现k匿名

<div align=center><img src="https://raw.githubusercontent.com/HectorHou/Location-Privacy-Protection/master/images-folder/location1.png"></div>

### 近完全子图的寻找
* 完全图:v * (v - 1) / 2 = e   - >   聚集系数 = 2e / (v * (v - 1)) 聚集系数越大,v的邻接结点中的边越密集,从v和它的邻接结点数找到的完全图的概率越高
* 从v出发根据聚集系数判断由v和它所相邻的点所构成的图是完全图的概率
* 贪心策略：构造包含每个点的邻接系数的缓存,每次做匿名随机从取一个点,检查它的邻接系数,若大于某个阈值p,那么从它和它的邻接结点用随机取n个点作为一组匿名

### 轨迹隐私策略
* 研究目的：当用户连续发起位置请求时,通过地图额外信息等攻击手段,会发现真是的用户轨迹是一条曲线,而匿名的信息为随机话的点
* 假设:如果第i次匿名位置的结果在第i-1次匿名位置的结果附近,那么攻击者将无法功过上述方案进行攻击
* 方案:在第i次匿名时不再随即选择AP节点,而是选择i-1次匿名结果中的一个点作为起始点

## 系统实现

### 简介
* 使用maven搭建，项目结构
-geo-parent
--geo-localization //定位实现包，指纹库 + 协议格式 + 定位实现
--geo-localization-server //定位服务器实现包，日志 + 半包处理 + 编解码 + 长连接处理 + 用户认证 + 定位处理
--geo-localization-client //定位客户伏案，通过指定socket连接定位服务器或匿名服务器，匿名过程对用户透明
--geo-anonymity //匿名实现包，数据结构（AP图，聚集系数表，表项） + 匿名实现
--geo-anonymity-server //匿名服务器 + 代理服务器，日志 + 半包处理 + 编解码 + 匿名生成 + 代理定位

<div align=center><img src="https://raw.githubusercontent.com/HectorHou/Location-Privacy-Protection/master/images-folder/location2.png"></div>

### 定位实现
* 使用相对坐标,在实验室建立6×15的直角坐标系,对于每个点收集信号强度前十位的mac地址和信号强度建立指纹库
* 消极学习，使用欧式距离 (pow(ss1 - ss1') + pow(ss2 - ss2') + ...)/ n 找到最近的三个点取均值
* 消息格式
	* LocationReq  (message)
| userId  | accessPoints  |
| :----: |:-------:|
|string |AccessPoints  |
	* AccessPoints (message)
| points  |
| :----------------: |
|map < string, float > |
	* LocationResp (message)
| userId  | location  | state  | message  |
| :----: |:-------:| :----: |:-------:|
|string |Location  |State |string  |
	* Location (message)
| longitude  | latitude  |
| :----: |:-------:|
|double |double  |
	* State (enum)
| SUCCESS  | NOT_FOUND  | AUTH_FAILED  |
| :----: |:-------:| :----: |
|0 |1  |2 |

<div align=center><img src="https://raw.githubusercontent.com/HectorHou/Location-Privacy-Protection/master/images-folder/uml1.png"></div>

### 匿名实现
* 位置匿名 单次基本流程
* 轨迹匿名

<div align=center><img src="https://raw.githubusercontent.com/HectorHou/Location-Privacy-Protection/master/images-folder/liucheng1.png"></div>
<div align=center><img src="https://raw.githubusercontent.com/HectorHou/Location-Privacy-Protection/master/images-folder/uml2.png"></div>

### Netty线程模型
* Reactor单线程模型: 由于Reactor模式使用的是异步非阻塞IO，所有的IO操作都不会导致阻塞，理论上一个线程可以独立处理所有IO相关的操作
* Reactor多线程模型: 有专门一个NIO线程-Acceptor线程用于监听服务端，接收客户端的TCP连接请求，网络IO操作-读、写等由一个NIO线程池负责
* 主从多线程模型： Acceptor线程池仅仅只用于客户端的登陆、握手和安全认证；SubReactor线程池包含一组IO线程，由IO线程负责后续的IO操作
* <b>*1个NIO线程可以同时处理N条链路，但是1个链路只对应1个NIO线程*</b>

### 定位服务器
* 序列化与反序列化： protobuf
* 半包处理： 基于数据包长度编码
* 长连接处理： 超时没有读取数据断开连接,ReadTimeoutHandler超时抛出异常，Localization处理
* 认证机制： 连接接受的第一个数据包进行认证（是否存在用户，用户是否已登陆），认证成功后删除认证Handler

<div align=center><img src="https://raw.githubusercontent.com/HectorHou/Location-Privacy-Protection/master/images-folder/pipeline1.png"></div>

### 匿名服务器
* 序列化与反序列化： protobuf
* 半包处理： 基于数据包长度编码
* 匿名处理： 生成匿名，传给下一个Handler
* 代理处理：
 * FrontendHandler拥有与定位服务器连接的channel，向定位服务器发送请求
 * BackendHandler拥有与客户端连接的channel，向用户发送定位相应
 * 根据netty线程模型的建议，同一个连接的操作应该在一个EventLoop中完成，建立与定位服务器连接时使用当前EventLoop

<div align=center><img src="https://raw.githubusercontent.com/HectorHou/Location-Privacy-Protection/master/images-folder/pipeline2.png"></div>

### 客户端
* 基本模型类比定位服务器和匿名服务器
* 使用BlockingQueue作为消息的中介，用户生产消息，客户端连接处理消息，解耦

<div align=center><img src="https://raw.githubusercontent.com/HectorHou/Location-Privacy-Protection/master/images-folder/queue.png"></div>

### 测试结果
* 匿名成功率 vs 匿名度k （p = 0.95）

<div align=center><img src="https://raw.githubusercontent.com/HectorHou/Location-Privacy-Protection/master/images-folder/test1.png"></div>

* 匿名成功率 vs 聚集系数阈值p （k = 3）

<div align=center><img src="https://raw.githubusercontent.com/HectorHou/Location-Privacy-Protection/master/images-folder/test2.png"></div>
