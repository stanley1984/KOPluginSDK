/**
 * Copyright (C) © 2014 深圳市掌玩网络技术有限公司
 * KoTvGameBox
 * KoTvBaseActivity.java
 */
package cn.vszone.ko.plugin.sdk.misc;

import java.io.File;
import java.io.FilenameFilter;

import android.os.Environment;
import android.util.Log;

/**
 * @author stanley
 * @Create at 2015年11月17日 下午9:55:55
 * @Version 1.0
 * <p><strong>程序全局使用的运行环境配置信息</strong></p>
 */
public class VSBuildConfig {

    // ===========================================================
    // Constants
    // ===========================================================

    public final static int     TEST_MODE_RELEASE       = 0;
    public final static int     TEST_MODE_CP_TEST       = 1;
    
    /**
     * 内部项目集体共用测试环境
     */
    public final static int     TEST_MODE_INNER_TEST    = 2;
    
    /**
     * matchvs开发组专用测试环境
     */
    public final static int     TEST_MODE_DEV_TEST      = 3;
    

    private final static String TAG                     = "VSBuildConfig";
    

    // ===========================================================
    // Fields
    // ===========================================================
    /***
     * 运行环境： 0 正式发布环境 1 CP测试环境 2 开发测试环境 3 内网测试环境
     */
    private static int          testMode                = TEST_MODE_RELEASE;

    private static String       buildConfigPath         = "";

    private static boolean      enableCPTestEntry       = false;

    private static boolean      isInPluginTestMode = false;

    //private static boolean      enableLogging           = false;


    // ===========================================================
    // Constructors
    // ===========================================================
    static {
        loadConfigs();
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    
    public static boolean isEnableCPTestEntry() {
        return enableCPTestEntry;
    }

    /**
     * 
     * @return true 插件加载处于测试模式， 如果是，将使用本地的插件包进行加载，否则使用网上下载最新的插件包进行加载
     */
    public static boolean isInPluginTestMode() {
        return isInPluginTestMode;
    }

    public static int getTestMode() {
        return testMode;
    }
    
    public static boolean isInTestMode() {
        return testMode > 0;
    }

//    public static boolean isEnableLogging() {
//        checkConfigsInited();
//        return enableLogging;
//    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    
    /**
     * 判断SD卡是否可用
     * 
     * @return SD卡可用返回true, 否则返回false.
     */
    public static boolean checkSDCard() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }
    
    private static void loadConfigs() {
        if (!checkSDCard()) {
            Log.w(TAG, "No valid SDCard, skip VSBuildConfig!");
            return;
        }
        buildConfigPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.vscfg";
        Log.i(TAG, "start to load configs for VSBuildConfig from: " + buildConfigPath);

        
        File configDir = new File(buildConfigPath);
        if (configDir.exists()) {
            
            File[] configFiles = configDir.listFiles(new ConfigFilenameFliter());
            File lastModifiedMvscFile = null;
            if (configFiles != null) {
                for (File cf : configFiles) {
                    if (cf.getName().startsWith(".mvsc")) {
                        if (lastModifiedMvscFile == null || cf.lastModified() > lastModifiedMvscFile.lastModified()) {
                            lastModifiedMvscFile = cf;
                        }
                    } else if (".cpt".equals(cf.getName())) {
                        enableCPTestEntry = true;
                        Log.i(TAG, "enableCPTestEntry = true");
                    } else if (".kopt".equals(cf.getName())) {
                        isInPluginTestMode = true;
                        Log.i(TAG, "enablePluginUpdateCheck = true");
                    } 
                }
            }
            if (lastModifiedMvscFile != null) {
                if (".mvsc".equals(lastModifiedMvscFile.getName()) || ".mvsc1".equals(lastModifiedMvscFile.getName())) {
                    testMode = TEST_MODE_CP_TEST;
                } else if (".mvsc3".equals(lastModifiedMvscFile.getName())) {
                    testMode = TEST_MODE_DEV_TEST;
                } else if (".mvsc2".equals(lastModifiedMvscFile.getName())) {
                    testMode = TEST_MODE_INNER_TEST;
                }
                Log.i(TAG, "testMode = " + testMode);
            }
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    
    public static class ConfigFilenameFliter implements FilenameFilter{

        @Override
        public boolean accept(File dir, String filename) {
            return filename.startsWith(".");
        }
        
    }

}
