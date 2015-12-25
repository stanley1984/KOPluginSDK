附1：查看Debug keystore 的MD5:


附2：查看正式keystore 的MD5， 可通过命令行：
keytool  -v -list -alias xxxx -keystore xxxxx.keystore


如果查看的是系统应用的签名（platform.pk8)的md5， 可以先把apk解压， 找到解压后的META-INFO文件夹下面的CERT.RSA文件， 通过keytool工具查看。
