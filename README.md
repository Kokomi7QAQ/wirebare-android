# WireBare

WireBare 是一个基于 Android VPN Service 开发的 Android 抓包框架

整个项目是一个完整的 Android 应用程序，其中的 [wirebare-kernel](https://github.com/Kokomi7QAQ/wirebare-kernel) 模块为核心的抓包模块，app 模块则提供了一些拓展功能和简单的用户界面

在高版本的 Android 系统中的 HTTPS 的拦截抓包功能需要先安装代理服务器根证书到 Android 系统的根证书目录下

证书相关文件和使用说明在项目根目录的 certificate 目录下


### 功能概览

#### 网际层

- 支持 IPv4 和 IPv6 的代理抓包
- 支持 IP 协议解析

#### 传输层

- 支持 TCP 透明代理、拦截抓包
- 支持 UDP 透明代理

#### 应用层

- 支持 HTTP 协议解析
- 支持 HTTPS 加解密（基于 TLSv1.2，需要先为 Android 安装代理服务器根证书）

