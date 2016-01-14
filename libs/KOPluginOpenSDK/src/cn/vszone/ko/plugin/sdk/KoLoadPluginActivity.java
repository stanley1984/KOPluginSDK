/**
 * Copyright (C) © 2014 深圳市掌玩网络技术有限公司
 * KOPluginOpenSDK
 * KoLoadPluginActivity.java
 */
package cn.vszone.ko.plugin.sdk;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import cn.vszone.ko.plugin.framework.PluginLoadListener;
import cn.vszone.ko.plugin.framework.utils.PluginOpener;
import cn.vszone.ko.plugin.framework.utils.SharedPreferenceUtils;
import cn.vszone.ko.plugin.sdk.KoStartUpActivity.ExitStartUpBroadcastReceiver;
import cn.vszone.ko.plugin.sdk.misc.PartnerAppIDs;
import cn.vszone.ko.plugin.sdk.util.AppUtils;

/**
 * @author Jin Binbin
 * @Create at 2015-11-3 上午11:09:41
 * @Version 1.0
 *          <p>
 *          <strong>Features draft description.加载插件</strong>
 *          </p>
 */
public class KoLoadPluginActivity extends Activity {

    // ===========================================================
    // Constants
    // ===========================================================
    public static final String  KEY_FILE_PATH  = "key_file_path";

    public static final String  KEY_FILE_NAME  = "key_file_name";
    public static final String  KEY_START_TIME = "startTime";
    // ===========================================================
    // Fields
    // ===========================================================
    private String              mPluginDir;
    private String              mApkName;
    private long                mStartTime;

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        mPluginDir = bundle.getString(KEY_FILE_PATH);
        mApkName = bundle.getString(KEY_FILE_NAME);
        mStartTime = bundle.getLong(KEY_START_TIME);
        //存储插件加载界面的启动时间
        SharedPreferenceUtils.setLong(getApplicationContext(), "plugin_start_time", mStartTime);
        loadPlugin();
    }

    @Override
    public void onBackPressed() {
        // 屏蔽掉加载时候返回键退出事件
    }

    // ===========================================================
    // Methods
    // ===========================================================
    private void loadPlugin() {
        boolean useHostNativeLibs = false;
        if (PartnerAppIDs.APP_ID_XIAOMI.equalsIgnoreCase(AppUtils.getKOChannel(getApplicationContext()))) {
            useHostNativeLibs = true;
        }
        File file = new File(mPluginDir + "/" + mApkName);
        if (file.exists()) {
            PluginOpener.startPlugin(this, mPluginDir + "/" + mApkName, useHostNativeLibs, new MyPluginLoadListener());
        } else {
            exit(1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DCLOG", "onResume" + getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("DCLOG", "onPause" + getClass().getSimpleName());
        Log.d("TAG", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("TAG", "onStop");
    }

    private void exit(int pResult) {
        Log.d("TAG", "exit:" + pResult);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 发出广播 让竞技台加载界面退出
        Intent intent = new Intent();
        intent.setAction(ExitStartUpBroadcastReceiver.FILTER_ACTION);
        sendBroadcast(intent);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private class MyPluginLoadListener implements PluginLoadListener {

        @Override
        public void onLoadStart() {

        }

        @Override
        public void onLoadFinished() {
            Log.d("TAG", "onLoadFinished");
            exit(KoLoadPluginActivity.RESULT_OK);
        }

        @Override
        public void onLoadFailed() {
            Log.d("TAG", "onLoadFailed");
            exit(1);
        }

    }
}
