![J NDC](https://s1.ax1x.com/2020/11/04/B6HETJ.png)
![jdk12](https://img.shields.io/badge/jdk-8-orange.svg)



## 介绍
* "J NDC" 是 "java no distance connection"的缩写，意在提供简便易用的端口映射应用，应用基于netty编写。 
* 应用核心由ndc私有协议支撑

## 文档摘要
* [数据流向](#数据流向)
* [ndc协议](#ndc协议)
* [数据加解密](#数据加解密)
* [IP黑白名单](#IP黑白名单)
* [开发计划](#开发计划)



## 数据流向
```
broser     ------->               (tunnel)               ---------->local_app
client     -------> jndc server <----------> jndc client ---------->local_app
other      ------->                                      ---------->local_app
```

## ndc协议
* 协议设计为仅支持ipv4
* 单包数据长度限制,超出将自动拆包
```
public static final int AUTO_UNPACK_LENGTH = 5 * 1024 * 1024
```
* 协议结构：
```
--------------------------------
  3byte      2byte      1byte
|  ndc   |  version  |  type   |
--------------------------------
            4byte
|          local ip            |
--------------------------------
            4byte
|          remote ip           |
--------------------------------
            4byte
|          local port          |
--------------------------------
            4byte
|          server port         |
--------------------------------
            4byte
|          remote port         |
--------------------------------
            7byte
|          data length         |
--------------------------------
           data length byte
|            data              |
--------------------------------
```

## 数据加解密
* 应用通过```DataEncryption```接口对协议内"变长部分数据”进行加解密，默认使用AES算法执行加解密过程。
* 可替换为更为安全的非对称加密

## IP黑白名单
* 可通过配置文件限制服务端IP黑白名单
  1. 黑名单：名单内ip限行
  2. 白名单：仅白名单内ip放行 
* 默认白名单拥有更高权重，即黑白名单均出现127.0.0.1,那么127.0.0.1将被放行
```yaml
blackList:
  - "127.0.0.1"
  - "192.168.1.1"

whiteList:
  - "127.0.0.1"
  - "127.0.0.2"
```



## 开发计划
* 二级域名的端口复用
* 流量统计
* 流量特征识别

