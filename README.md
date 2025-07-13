[语言：简体中文](./README_CN.md)

# WireBare

WireBare is an Android App that can capture network packet through Android VPN service.

The core module is [wirebare-kernel](https://github.com/Kokomi7QAQ/wirebare-kernel), module "app" provides some extended functions and UI.

In higher versions of Android, the HTTPS packet capture function requires installing the certificate in the Android CA Store.

The certificate and guides are [here](./certificate).


### Features Overview

#### Network Layer

- Support IPv4 and IPv6
- Support parsing IP packet

#### Transport Layer

- Support transparent proxy and parsing TCP packet
- Support transparent proxy UDP packet

#### Application Layer

- Support parsing HTTP packet
- Support parsing HTTPS packet (Based on TLSv1.2, and the certificate must be installed first)

