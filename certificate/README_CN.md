[Language: English](./README.md)

# 如何使用这些证书文件？

需要被安装的证书为 [318facc2.0](./318facc2.0) 或 [wirebare.pem](./wirebare.pem).



## 下面是三种安装方法

#### 直接通过安卓系统设置安装 (适用于 Android 6.0 或以下)

在 Google 上可以轻松找到安装的方法


#### 将证书文件移动到系统信任证书目录下

你需要将 [318facc2.0](./318facc2.0) 移动到 `system/etc/security/cacerts/` 目录下

直接修改 `system/etc/security/cacerts/` 这个目录一般是困难的，通常需要 root 权限


#### 通过 Magisk 安装

如果你使用 [Magisk](https://github.com/topjohnwu/Magisk)，你可以通过它来安装

提供的 Magisk 模块文件前缀为 "wirebare_certificate_installer", 你必须通过 Magisk 应用程序来安装它



## 我能使用自己的证书吗？

可以，如果你想用自己的证书，你需要做以下的事情

- 步骤一：在[WireBare 源码](../app/src/main/java/top/sankokomi/wirebare/ui/launcher/LauncherModel.kt)中修改你的证书信息
- 步骤二：将以下的证书文件替换
    - [被打包到 WireBare 内的 .jks 文件](../app/src/main/assets/wirebare.jks)
    - [被打包到 Magisk 模块内的证书文件](../wirebare-zygisk/magisk/system/etc/security/cacerts/318facc2.0)
- 步骤三：重新打包[WireBare](https://github.com/Kokomi7QAQ/wirebare-android)和[Magisk 模块](https://github.com/Kokomi7QAQ/wirebare-zygisk)
