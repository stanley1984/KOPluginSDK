/**
 * Copyright (C) © 2014 深圳市掌玩网络技术有限公司
 * KOPluginOpenSDK
 * KoLoadPluginActivity.java
 */
package cn.vszone.ko.plugin.sdk;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.dataeye.DCAgent;
import com.dataeye.DCEvent;
import com.dataeye.DCReportMode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import cn.vszone.ko.plugin.framework.PluginLoadListener;
import cn.vszone.ko.plugin.framework.utils.PluginOpener;
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
    private static final String DCKEY          = "85409328C0493F4219A18E27A544383D";
    // ===========================================================
    // Fields
    // ===========================================================
    private String              mPluginDir;
    private String              mApkName;
    private long                mStartTime;
    private long                mStartPluginTime;

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
        DCAgent.setReportMode(DCReportMode.DC_DEFAULT);
        DCAgent.initConfig(getApplicationContext(), DCKEY, AppUtils.getKOChannel(getApplicationContext()));
        Bundle bundle = getIntent().getExtras();
        mPluginDir = bundle.getString(KEY_FILE_PATH);
        mApkName = bundle.getString(KEY_FILE_NAME);
        mStartTime = bundle.getLong(KEY_START_TIME);
        // 统计 启动插件加载页面的时间
        Map<String, String> map = new HashMap<String, String>();
        long endTime = System.currentTimeMillis();
        long duration = endTime - mStartTime;
        map.put("startTime", "" + mStartTime);
        map.put("endTime", "" + endTime);
        map.put("duration", "" + duration);
        DCEvent.onEvent("startLoadPluginAct", map);
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
            mStartPluginTime = System.currentTimeMillis();
            PluginOpener.startPlugin(this, mPluginDir + "/" + mApkName, useHostNativeLibs, new MyPluginLoadListener());
        } else {
            exit(1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DCLOG", "onResume" + getClass().getSimpleName());
        DCAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("DCLOG", "onPause" + getClass().getSimpleName());
        DCAgent.onPause(this);
        Log.d("TAG", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("TAG", "onStop");
    }

    private void exit(int pResult) {
        Log.d("TAG", "exit:" + pResult);
        // 统计 启动插件的启动时间 成功和失败次数
        Map<String, String> map = new HashMap<String, String>();
        long endTime = System.currentTimeMillis();
        map.put("startTime", "" + mStartPluginTime);
        map.put("endTime", "" + endTime);
        map.put("duration", "" + (endTime - mStartTime));
        if (pResult == KoLoadPluginActivity.RESULT_OK) {
            map.put("result", "success");
        } else {
            map.put("result", "fail");
            map.put("erroMsg", "plugin file is no exists");
        }
        DCEvent.onEvent("startPlugin", map);
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
