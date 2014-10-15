
#基于 Akka 的远程服务管理模块
**Keyworks**:  `Hadoop`,`akka`,`Spark`,`Java`

基于大数据一体机（集成了 Hadoop，Hive，HBase 等大数据工具）的管理平台，整合和管理一体机的服务。
提供如下功能：
- Hadoop 的管理，包括 HDFS 文件系统，以及 Mapred 任务管理。
- Hive 任务提交、HBase 表查询
- 云平台提供监控与告警功能 (基于 Ganglia 和 Nagios )
- 用户权限、控制面板
- 服务管理
核心模块即：服务管理，基于分布式消息通信框架 Akka 开发一个远程服务管理系统。关于 akka 的介绍可以看我的这篇文章 [akka introduce](https://github.com/flyaos/Notebook/blob/master/Notes/Akka/akka_introduce.md)。

## 0.架构

阅读了 Apache Spark 的 Standalone Deploy 模块的源码，并参考其架构。系统基于 Master-Agent 模式，通过 Master 对分布的 Agent 实现远程服务管理。参考了 Spark 源码里的 deploy 模块。
### 架构图
 ![架构图](http://ww4.sinaimg.cn/large/7377e81bjw1elcd9hy8goj21kw0rngpu.jpg)

### 详细介绍
主要分为两个模块 Server 端和 agent 端。简单介绍一下这两端所包含的包和类及其功能。
####Server 端：
- **TaskQueue**：将前台发过来的命令入队列。
- **Application**：入口，初始化 Server
>1.读取配置文件创建 Syetem actor   
  2.创建 Supervisior actor。   
  3.创建检测两端是否连接的线程，实时更新 agent 所在主机的连接状态。  

- **Supervisior**：接受 Agent 来的注册信息，创建对应的 Worker，同时监视 worker 的状态生命周期。
- **Worker**：与 Agent 端通信：  
>1.如果 Agent 空闲，则从命令队列取出一条命令并发送。  
2.接收远端的执行结果，根据结果状态来更新服务角色和服务的状态。  
3.根据心跳信息实时更新agent的连接状态。

- **ServerAcotr**：旧版本 server，现已分离 supervisior 和 worker，容错性低，已废弃。  
- **DBTool**: 更新数据库操作的工具类。（完成 Worker 的2、3）
- **Message**：包含各种通信过程中用到的 Java Bean，需要序列化。

#### Agent 端：
- **AgentActor**：初始化 agent，创建 actor，向 server 发送注册请求。并启动心跳线程。
- **HeartBeatRunner**：心跳线程，定时读取执行结果，并向 Server 端的 worker 返回状态信息。
- **ExectorRunner**：命令执行线程，发送执行 Shell 脚本的命令，并读取返回结果。
- **Message** 包：包含通信过程中用到的 Java Bean，需要序列化。 


## 1.环境配置
### Server 端环境配置：
- Hosts 配置，将Hadoop 环境的Namenode和datanode主机的主机名和IP映射起来。
例如测试的环境 hosts 172.16.9.1
> 172.16.9.1 h2master1  
> 172.16.9.2 h2master2  
> 172.16.9.3 h2slave1  
> 172.16.9.4 h2slave2  
> 172.16.9.5 h2slave3  
> 172.16.9.6 h2slave4  

- 配置文件 application.conf, 将里面的 IP 改为服务端 IP. 
- 所需脚本文件：killProcess.sh
> 脚本功能：kill 掉绑定某端口号的进程 
使用说明：sh killProcess.sh [-p] [port] ,如果不指定端口号，默认 kill 掉绑定 2552 端口号的进程。2552 为 akka server 默认绑定的端口号。
**注意**：每次启动Server时 先杀掉绑定2552端口号的进程，执行sh killProcess即可。

### Agent端环境配置
由于服务管理的默认用户为hadoop，先切换到hadoop用户,将以下文件上传到目录/home/hadoop/akka-service。如果没有该目录则新建一个。
- 文件说明：
> agent.jar------------------------- agent jar包 执行文件   
> basic.properties----------------配置文件   
> operateHbaseProcess.sh----操作Hbase的脚本   
> operateHDFSProcess.sh-----操作HDFS的脚本   
> killProcess.sh------------------kill进程的脚本(一般用不到)   

- 配置文件 basic.properties 说明 
> ServerIp=172.16.2.31-------------Server端IP   
> ServerPort=2552-------------------Server端akka服务绑定的端口号   
> LocalIp=172.16.3.52--------------本机IP   

- 若agent挂了，直接重启即可

- 配置文件 Application.conf

```
ServerSys {
  akka {
    actor {
      provider = "akka.remote.RemoteActorRefProvider"
    }
   remote {
    transport = "akka.remote.netty.tcp"
    netty.tcp {
      hostname = "172.16.1.17"
      port = 2552
    }
  }
  }
}

AgentSys {
  akka {
    actor {
      provider = "akka.remote.RemoteActorRefProvider"
    
    }
  remote {
      transport = "akka.remote.netty.tcp"
      netty.tcp {
          hostname = "localhost
          port = 0 //端口号随机
          }
        }
    }
}
```