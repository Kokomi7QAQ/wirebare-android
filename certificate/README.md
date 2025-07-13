[语言：简体中文](./README_CN.md)

# How to use these certificate files?

The certificate that need to be installed is [318facc2.0](./318facc2.0) or [wirebare.pem](./wirebare.pem).



## Here are three installation methods

#### Install directly from system settings (For Android Marshmallow or below)

You can easily find how to install by this way by searching on Google.


#### Move the certificate to Android CA Store

You need to move [318facc2.0](./318facc2.0) to `system/etc/security/cacerts/`

Directly modifying the `system/etc/security/cacerts/` directory is difficult and usually requires root access.


#### Easily install through Magisk

If you use [Magisk](https://github.com/topjohnwu/Magisk), you can easily install it through it.

The Magisk module file's prefix is "wirebare_certificate_installer", you must install it through the Magisk App.



## Can I use my own certificate?

Yes, if you want to use your own certificate, you need to do the following.

- Step I: Modify the certificate information in [WireBare source](../app/src/main/java/top/sankokomi/wirebare/ui/launcher/LauncherModel.kt)
- Step II: Replace the certificate following.
  - [The KeyStore packaged into the WireBare](../app/src/main/assets/wirebare.jks)
  - [The certificate packaged into Magisk Module](../wirebare-zygisk/magisk/system/etc/security/cacerts/318facc2.0)
- Step III: Repacked [WireBare App](https://github.com/Kokomi7QAQ/wirebare-android) and [Magisk Module](https://github.com/Kokomi7QAQ/wirebare-zygisk)
