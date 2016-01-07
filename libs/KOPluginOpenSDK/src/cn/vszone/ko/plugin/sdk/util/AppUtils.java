/**
 * Copyright (C) © 2014 深圳市掌玩网络技术有限公司
 * LibCommon
 * AppUtils.java
 */
package cn.vszone.ko.plugin.sdk.util;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author
 * @Create at 2014-6-25 下午5:45:19
 * @Version 1.0
 */
public class AppUtils {
    // ===========================================================
    // Constants
    // ===========================================================
    private static final String TAG                       = "AppUtils";
    private static final String META_DATA_KO_STAT_KEY     = "KO_STAT_KEY";
    private static final String META_DATA_KO_CHANNEL      = "KO_CHANNEL";

    public static final String  META_DATA_KO_APPKEY       = "KO_APP_KEY";
    public static final String  META_DATA_KO_APPKEY_DEBUG = "KO_APP_KEY_DEBUG";
    public static final String  UNKNOWN_VERSION_NAME      = "UNKNOWN";
    public static final int     UNKNOWN_VERSION_CODE      = -1;
    public static final String  KO_CHANNEL_UNKNOWN        = "UNKNOWN";
    public static final String  KO_STAT_KEY_UNKNOWN       = "UNKNOWN";

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * 获取应用VersionName
     * 
     * @param pContext
     * @return
     */
    public static final String getVersionName(Context pContext) {
        PackageManager packageManager = pContext.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(pContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "initVersionInfo.error " + e.toString());
        }
        if (packageInfo != null) {
            return packageInfo.versionName;
        } else {
            return UNKNOWN_VERSION_NAME;
        }
    }

    /**
     * 获取应用VersionCode
     * 
     * @param pContext
     * @return
     */
    public static final int getVersionCode(Context pContext) {

        PackageManager packageManager = pContext.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(pContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "initVersionInfo.error " + e.toString());
        }
        if (packageInfo != null) {
            return packageInfo.versionCode;
        } else {
            return UNKNOWN_VERSION_CODE;
        }
    }

    /**
     * 获取应用DC渠道ID
     *
     * @param pContext
     * @return
     */
    public static final String getKOStatKey(Context pContext, String pPackageName) {
        try {
            ApplicationInfo applicationInfo = pContext.getPackageManager().getApplicationInfo(pPackageName,
                PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            if (applicationInfo.metaData != null) {
                return applicationInfo.metaData.getString(META_DATA_KO_STAT_KEY);
            }
        } catch (Exception e) {
            Log.e(TAG, "getKOStatKey.error " + e.toString());
        }
        return KO_STAT_KEY_UNKNOWN;
    }

    /**
     * 获取应用DC渠道ID
     *
     * @param pContext
     * @return
     */
    public static String getKOChannel(Context pContext) {
        return getKOChannel(pContext, pContext.getPackageName());
    }

    public static final String getKOChannel(Context pContext, String pPackageName) {
        String channelValue = KO_CHANNEL_UNKNOWN;
        try {
            ApplicationInfo applicationInfo = pContext.getPackageManager().getApplicationInfo(pPackageName,
                PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            Bundle metaDatas = applicationInfo.metaData;
            if (metaDatas != null) {
                String metaValue = metaDatas.getString(META_DATA_KO_CHANNEL);
                if (TextUtils.isEmpty(metaValue)) {
                    throw new Exception("Meta-data (" + META_DATA_KO_CHANNEL + ") is not found int AndroidManifest.xml!");
                }
                return metaValue;
            }
        } catch (Exception e) {
            Log.e(TAG, "getKOChannel.error " + e.toString());
        }
        return channelValue;
    }

    /**
     * 获取与KO对战频道合作的第三方应用对应的AppKey
     * 
     * @param pContext
     * @return
     */
    public static final String getKOPartnerAppKey(Context pContext) {
        try {
            ApplicationInfo applicationInfo = pContext.getPackageManager().getApplicationInfo(pContext.getPackageName(),
                PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            String metaDataAppKey = null;
            // if (BuildConfig.DEBUG) {
            // metaDataAppKey = META_DATA_KO_APPKEY_DEBUG;
            // } else {
            metaDataAppKey = META_DATA_KO_APPKEY;
            // }
            if (applicationInfo.metaData != null) {
                return applicationInfo.metaData.getString(metaDataAppKey);
            }
        } catch (Exception e) {
            Log.e(TAG, "initKOChannel.error " + e.toString());
        }
        return "";
    }

    /**
     * Android 系统是否在4.1及以上(Api Code >=16)
     * 
     * @return
     */
    public static boolean isJellyBeanUpperVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * Android 系统是否在4.2及以上(Api Code >=17)
     * 
     * @return
     */
    public static boolean isJellyBeanMR1UpperVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    /**
     * 该应用是否作为系统应用形式运行在Android系统上
     * 
     * @param pContext
     * @return
     */
    public static boolean isAsSystemApp(Context pContext) {
        return (pContext.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    /**
     * 判断当前进程是否为主进程。 例如 和包名相同的 进程 cn.vszone.tv.gamebox 是主进程
     * cn.vszone.tv.gamebox:psp 是子进程
     * 
     * @param pContext
     * @return
     */
    public static boolean isInMainProcess(Context pContext) {
        return pContext.getPackageName().equalsIgnoreCase(getCurrentProcessName(pContext));
    }

    public static String getCurrentProcessName(Context pContext) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) pContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    /**
     * 根据包名判断该应用是否处在当前界面
     * 
     * @param packageName
     * @return
     */
    public static boolean isAppOnForeground(Context pContext, String packageName) {
        ActivityManager activityManager = (ActivityManager) pContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
        if (tasksInfo.size() > 0) {
            // 应用程序位于堆栈的顶层
            if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据包名判断该应用是否已经启动
     * 
     * @param packageName
     * @return
     */
    public static boolean isAppStart(Context pContext, String packageName) {
        ActivityManager activityManager = (ActivityManager) pContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(100);
        for (RunningTaskInfo runningTaskInfo : tasksInfo) {
            if (packageName.equals(runningTaskInfo.topActivity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 用来判断服务是否运行.
     * 
     * @param context
     * @param className
     *            判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
