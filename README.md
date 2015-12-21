###简介：
竞技台插件SDK（框架）， 主要提供了在免安装的情况下，运行KO对战游戏的能力。

###运行Demo
Demo 分为TV版和手机版

HostDemo.Mobile：手机版的demo源码

HostDemo.TV：TV版的demo源码

**demo中有编译错误， 目的是提醒开发者进行必要的设置**

1. 把"竞技台"的插件包（不能改名，必须为KoMobileArena.apk或者KoTvArena.apk)， 放置到sd卡的根目录。
2. 直接安装 KOPluginHostDemo.TV.apk，  运行, 点击 启动竞技台插件 按钮；

或者，

Import Eclipse 工程KOPluginHostDemo， 直接运行, 点击启动竞技台插件 按钮

###集成插件
* 1.引入KOPluginOpenSDK库工程

* 2. AndroidManifest.xml 的配置
- 权限声明
```xml
    <!-- 接收系统通知权限组 -->
    <uses-permission android:name="android.permission.RESTART_PACKAGES" />
    <uses-permission android:name="android.Manifest.permission.KILL_BACKGROUND_PROCESSES" />
    <!-- 保持屏幕高亮权限组 -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <!-- 访问网络状态权限组 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <!-- 读写SD卡权限组 -->
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    <uses-permission android:name="android.permission.WRITE_SETTINGS" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <!-- 读取IMEI的权限 -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <!-- 系统操作权限组 -->
    <uses-permission android:name="com.android.launcher.permission.READ_SETTINGS" />
    <uses-permission android:name="com.android.launcher.permission.WRITE_SETTINGS" />
    <uses-permission android:name="com.android.launcher.permission.INSTALL_SHORTCUT" />
    <uses-permission android:name="com.android.launcher.permission.UNINSTALL_SHORTCUT" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.READ_LOGS" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.CHANGE_CONFIGURATION" />
    <uses-permission android:name="android.permission.BROADCAST_STICKY" />

    <!-- 全局Dialog -->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.GET_TASKS" />

    <!-- 发送消息 -->
    <uses-permission android:name="android.permission.SEND_SMS" />
    <!-- 阅读消息 -->
    <uses-permission android:name="android.permission.READ_SMS" />
    <!-- 写入消息 -->
    <uses-permission android:name="android.permission.WRITE_SMS" />
    <!-- 接收消息 -->
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
```
- application， 进行游戏时，需要比较大的内存支持， 需要设置 largeHeap 为true。

```xml
    <application
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:largeHeap="true"
        android:theme="@android:style/Theme.Light.NoTitleBar.Fullscreen" >
```
TV版本特别配置：
```xml
    <uses-feature
        android:name="android.hardware.type.television"
        android:required="true" />
```
- meta-data
```xml
        <meta-data
            android:name="KO_APP_KEY"
            android:value="dE0oRhauOX75w0bEXvb29。。。。。。。。XXXxxxxxx" />
        <!-- 这里填写合作方的名称ID， 用于统计 -->
        <meta-data
            android:name="KO_APP_ID"
            android:value="Xiaomi" />

```
- activity, service等配置 (TV版， 手机版需要注意横竖屏配置，具体以demo为准)
```xml
        <!-- 插件的代理器，使用框架必须声明 -->
        <activity
            android:name="cn.vszone.ko.plugin.framework.ProxyActivity"
            android:configChanges="keyboardHidden|orientation|screenSize|navigation"
            android:exported="true"
            android:label="@string/app_name"
            android:process=":koproxy"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="stateUnchanged|adjustPan" >
            <intent-filter>
                <action android:name="cn.vszone.ko.pay.action" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>
        <activity
            android:name="cn.vszone.ko.plugin.framework.TranslucentProxyActivity"
            android:configChanges="keyboardHidden|orientation|screenSize|navigation"
            android:exported="true"
            android:label="@string/app_name"
            android:process=":koproxy"
            android:screenOrientation="portrait"
            android:theme="@android:style/Theme.Translucent.NoTitleBar.Fullscreen"
            android:windowSoftInputMode="stateUnchanged|adjustPan" >
        </activity>
        <activity
            android:name="cn.vszone.ko.plugin.framework.LandscapeProxyActivity"
            android:configChanges="keyboardHidden|orientation|screenSize|navigation"
            android:exported="true"
            android:label="@string/app_name"
            android:process=":koproxy"
            android:screenOrientation="landscape"
            android:windowSoftInputMode="stateUnchanged|adjustPan" >
        </activity>
        <service
            android:name="cn.vszone.ko.plugin.framework.service.ProxyService"
            android:process=":koproxy" />

```
(使用SDK进行插件下载才需要)
```xml
        <activity
            android:name="cn.vszone.ko.plugin.sdk.KoStartUpActivity"
            android:label="@string/app_name"
            android:launchMode="singleTask"
            android:screenOrientation="portrait" >
        </activity>
        <activity
            android:name="cn.vszone.ko.plugin.sdk.KoLoadPluginActivity"
            android:label="@string/app_name"
            android:process=":koproxy"
            android:screenOrientation="portrait"
            android:theme="@android:style/Theme.Translucent.NoTitleBar.Fullscreen" >
        </activity>

```

* 3.如果自行处理插件下载，下载好后调用下面的方法
```java
    Intent intent = new Intent(this, KoLoadPluginActivity.class);
    // 插件下载后，存放在本地的目录的路径
    intent.putExtra(KoLoadPluginActivity.KEY_FILE_PATH, mLocalPluginDirPath);
    // 插件文件名
    intent.putExtra(KoLoadPluginActivity.KEY_FILE_NAME, mApkFileName);
    startActivity(intent);
```

* 4.如果使用SDK进行插件下载，使用以下代码
```java
   Intent intent = new Intent(this, KoStartUpActivity.class);
   //输入要下载插件的url，请先与我们约定
   intent.putExtra(KoStartUpActivity.KEY_DOWNLOAD_URL, mPluginDownloadUrl);
   File dir = new File(mLocalPluginDirPath);
    if (!dir.exists()) {
       dir.mkdirs();
    }
    // 插件下载后，存放在本地的目录的路径
   intent.putExtra(KoStartUpActivity.KEY_FILE_PATH, mLocalPluginDirPath);
   // 插件文件名
   intent.putExtra(KoStartUpActivity.KEY_FILE_NAME, mApkFileName);
   startActivity(intent);
```
