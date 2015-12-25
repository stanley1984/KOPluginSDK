## 查看Debug keystore的MD5:
![](https://github.com/stanley1984/KOPluginSDK/blob/develop/images/debug_md5.png)

##查看正式keystore 的MD5
可通过命令行：
```
keytool  -v -list -alias xxxx -keystore xxxxx.keystore
```
![](https://github.com/stanley1984/KOPluginSDK/blob/develop/images/release_md5.png)

##查看系统应用的签名（platform.pk8)的md5

可以先把apk解压， 找到解压后的META-INFO文件夹下面的CERT.RSA文件， 通过keytool工具查看。
![](https://github.com/stanley1984/KOPluginSDK/blob/develop/images/system_md5.png)
