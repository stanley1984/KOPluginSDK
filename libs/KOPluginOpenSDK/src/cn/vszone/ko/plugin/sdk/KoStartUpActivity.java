package cn.vszone.ko.plugin.sdk;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.vszone.ko.plugin.sdk.CET189storeUrlFetcher.FetchDownloadUrlCallback;
import cn.vszone.ko.plugin.sdk.KoDownloadManager.IDownloadListener;
import cn.vszone.ko.plugin.sdk.KoDownloadManager.Task;
import cn.vszone.ko.plugin.sdk.misc.BranchConfig;
import cn.vszone.ko.plugin.sdk.misc.PartnerAppIDs;
import cn.vszone.ko.plugin.sdk.misc.VSBuildConfig;
import cn.vszone.ko.plugin.sdk.util.AppUtils;
import cn.vszone.ko.plugin.sdk.util.SharedPreferenceUtils;

public class KoStartUpActivity extends Activity {

    private static final String          TAG                      = "KoStartUpActivity";

    private static final String          APK_FILE_NAME                     = "KoMobileArena.apk";
    // ===========================================================
    // Constants
    // ===========================================================
    public static final int              STEP_DOWNLOAD                     = 1;
    public static final int              STEP_DOWNLOADING                  = 2;

    public static final String           KEY_DOWNLOAD_URL                  = "key_download_URL";
    public static final String           KEY_FILE_PATH                     = "key_file_path";
    public static final String           KEY_FILE_NAME                     = "key_file_name";
    public static final String           KEY_TODAY                         = "today";
    public static final String           DEFAULT_APK_DOENLOAD_URL_189STORE =
        "http://download.vszone.cn/android/KoMobileArena_189store_latest.apk";
    public static final String           DEFAULT_APK_DOENLOAD_URL_TV =
            "http://download.vszone.cn/android/KoTvArena_kobox_latest.apk";
    // ===========================================================
    // Fields
    // ===========================================================

    private KoHProgressBar               mHProgress;
    private KoPromptDialog               mDialog;
    private Resources                    mResources;
    private TextView                     mStatusTipTv;
    private String                       mPluginDir;
    private String                       m186StoreRequestUrl;
    //private String                       mDownloadUrl;
    private ImageView                    mStartBgImageView;
    // 实际要下载的url
    private String                       mApkUrl;

    private DownloadListener             mDownloadListener;
    private KoDownloadManager.Task       mDownloadTask;

    private boolean                      mIsBackFromSetting       = false;
    private boolean                      isHasAPKFile             = false;

    private ExitStartUpBroadcastReceiver mRec                     = null;

    private String                       mApkName                 = "KoMobileArena.apk";

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
        overridePendingTransition(0, 0);
        setContentView(R.layout.ko_start_up_activity);
        initView();
        Bundle bundle = getIntent().getExtras();
        mPluginDir = bundle.getString(KEY_FILE_PATH);
        if (TextUtils.isEmpty(mPluginDir)) {
            mPluginDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        KoDownloadManager.getInstance().init(this);
        String localApkFullPath = null;
        if (mPluginDir != null) {
            if (mPluginDir.endsWith("/")) {
                localApkFullPath = mPluginDir + APK_FILE_NAME;
            } else {
                localApkFullPath = mPluginDir + "/" + APK_FILE_NAME;
            }
        }
        IntentFilter filter = new IntentFilter(ExitStartUpBroadcastReceiver.FILTER_ACTION);
        mRec = new ExitStartUpBroadcastReceiver(this);
        registerReceiver(mRec, filter);

        String downloadUrl = bundle.getString(KEY_DOWNLOAD_URL);
        if(PartnerAppIDs.APP_ID_189STORE.equalsIgnoreCase(AppUtils.getKOChannel(getApplicationContext()))){
            
            // 设置背景图：
            mStartBgImageView.setImageResource(R.drawable.ko_startup_bg_189store);
            // 设置地址
            m186StoreRequestUrl = downloadUrl;
            if (TextUtils.isEmpty(m186StoreRequestUrl) || VSBuildConfig.isInPluginTestMode()) {
                mApkUrl = DEFAULT_APK_DOENLOAD_URL_189STORE;
                startPlugin(mApkUrl);
            } else {
                if (!isToday()) {
                    requestDownloadUrlFrom189Store(localApkFullPath);
                } else {
                    mApkUrl = DEFAULT_APK_DOENLOAD_URL_189STORE;
                    startPlugin(mApkUrl);
                }
            }
        } else {
            mApkUrl = downloadUrl;
            startPlugin(mApkUrl);
        }

    }
    
    private String getDefaultDownloadApkUrl(){
        String downloadUrl = null;
        if(PartnerAppIDs.APP_ID_189STORE.equalsIgnoreCase(AppUtils.getKOChannel(getApplicationContext()))){
            downloadUrl = DEFAULT_APK_DOENLOAD_URL_189STORE;
        }
        if(BranchConfig.isTvArena){
            downloadUrl = DEFAULT_APK_DOENLOAD_URL_TV;
        }
        return downloadUrl;
    }

    /**
     * 请求下载地址
     * 
     * @param localApkFullPath
     */
    private void requestDownloadUrlFrom189Store(String localApkFullPath) {
        updateProgress(STEP_DOWNLOADING, 0);
        CET189storeUrlFetcher urlFetcher = new CET189storeUrlFetcher();
        urlFetcher.fetchDownloadUrl(this, m186StoreRequestUrl, localApkFullPath, new FetchDownloadUrlCallback() {

            @Override
            public void onSuccess(String oldApkHash, String pNewApkHash, String pApkUrl) {
                Log.d("fetchDownloadUrl", "pApkUrl:" + pApkUrl);
                Log.d("fetchDownloadUrl", "oldApkHash: " + oldApkHash + ",pNewApkHash:" + pNewApkHash);
                if (!TextUtils.isEmpty(pApkUrl)) {
                    mApkUrl = pApkUrl;
                    if (TextUtils.isEmpty(oldApkHash) || !oldApkHash.equals(pNewApkHash)) {
                        // 下载或者升级
                        downloadPlugin(mApkUrl);
                    } else {
                        startPlugin(mApkUrl);
                    }
                } else {
                    // 请求天翼的下载链接失败后，则直接下载KO服务端的APK包
                    mApkUrl = DEFAULT_APK_DOENLOAD_URL_189STORE;
                    startPlugin(mApkUrl);
                }
            }

            @Override
            public void onFailure(Throwable error, String content) {
                Log.e("TAG", "onFailure:" + error.toString());
                // 请求天翼的下载链接失败后，则直接下载KO服务端的APK包
                mApkUrl = DEFAULT_APK_DOENLOAD_URL_189STORE;
                startPlugin(mApkUrl);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsBackFromSetting) {
            if (!TextUtils.isEmpty(mApkUrl)) {
                startPlugin(mApkUrl);
            }
            mIsBackFromSetting = false;
        }
    }

    @Override
    protected void onPause() {
        Log.d("KoStartUpActivity", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("KoStartUpActivity", "onStop");
        super.onStop();
        exit();
    }

    @Override
    protected void onDestroy() {
        Log.d("KoStartUpActivity", "onDestroy");
        KoDownloadManager.getInstance().setDownloadListener(null);
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog.cancel();
        }
        mDialog = null;
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("KoStartUpActivity", "onNewIntent");
        if (!TextUtils.isEmpty(mApkUrl)) {
            startPlugin(mApkUrl);
        };
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private void initView() {
        mStatusTipTv = (TextView) findViewById(R.id.main_loading_tv_timetips);
        mHProgress = (KoHProgressBar) findViewById(R.id.main_loading_start_hpb_load);
        mStartBgImageView = (ImageView) findViewById(R.id.start_up_bg);
    }

    private void downloadPlugin(String pDownloadUrl) {
        if (mDownloadTask == null) {
            mDownloadTask = new KoDownloadManager.Task();
            mDownloadTask.fileName = APK_FILE_NAME;
            mDownloadTask.filePath = mPluginDir;
            mDownloadTask.url = pDownloadUrl;
        }
        if (mDownloadListener == null) {
            mDownloadListener = new DownloadListener(this);
        }
        SharedPreferenceUtils.setInt(getApplicationContext(), KEY_TODAY, getToday());
        KoDownloadManager.getInstance().setDownloadListener(mDownloadListener);
        KoDownloadManager.getInstance().addDownloadTask(KoDownloadManager.MODE_HTTP, mDownloadTask);
    }

    private void handleDownloadFail() {
        if (mDialog == null) {
            mDialog = new KoPromptDialog(this);
        }
        if (mResources == null) {
            mResources = getResources();
        }
        if (NetWorkManager.hasNetWork(getApplicationContext())) {
            mDialog.setMessage(mResources.getString(R.string.ko_download_plugin_download_fail));
            mDialog.addLeftButton(mResources.getString(R.string.ko_exit), new OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            mDialog.addRightButton(mResources.getString(R.string.ko_retry), new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(mApkUrl)) {
                        startPlugin(mApkUrl);
                    }
                }
            });
        } else {
            mDialog.setMessage(getResources().getString(R.string.ko_network_connect_fail));
            mDialog.addLeftButton(mResources.getString(R.string.ko_network_setting), new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(intent);
                    mIsBackFromSetting = true;
                }
            });
            mDialog.addRightButton(mResources.getString(R.string.ko_retry), new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(mApkUrl)) {
                        startPlugin(mApkUrl);
                    }
                }
            });
        }
        mDialog.show();
        mDialog.initView();
    }

    private boolean isToday() {
        int today = SharedPreferenceUtils.getInt(getApplicationContext(), KEY_TODAY, 0);
        return getToday() == today;
    }

    private void startPlugin(String pDownloadUrl) {
        File file = new File(mPluginDir + "/" + mApkName);
        isHasAPKFile = file.exists();
        if ((isToday() || VSBuildConfig.isInPluginTestMode()) && isHasAPKFile) {
            updateProgress(STEP_DOWNLOAD, 100.0f);
            startLoadPlugin();
        } else {
            if (!TextUtils.isEmpty(pDownloadUrl)) {
                downloadPlugin(pDownloadUrl);
            } else {
                Toast.makeText(this, "Download Url is not set in the Intent!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLoadPlugin() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(KoStartUpActivity.this, KoLoadPluginActivity.class);
                intent.putExtra(KEY_FILE_PATH, mPluginDir);
                intent.putExtra(KEY_FILE_NAME, mApkName);
                startActivity(intent);
            }
        }, 400);
    }

    private void updateProgress(int pStep, float pProgress) {
        if (pStep == STEP_DOWNLOAD) {
            mStatusTipTv.setText(R.string.ko_starting_plugin_tips);
        } else {
            if (isHasAPKFile) {
                mStatusTipTv.setText(getResources().getString(R.string.ko_download_plugin_update_tips, pProgress));
            } else {
                mStatusTipTv.setText(getResources().getString(R.string.ko_download_plugin_downloading_tips, pProgress));
            }
        }
        mHProgress.setProgress(pProgress);
    }

    /**
     * 获取当前日期的数字值
     * 
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    private int getToday() {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        Date d = new Date();
        return Integer.valueOf(df.format(d));
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    /**
     * 
     * @author Jin BinBin
     * @Create at 2015-11-24 上午11:40:11
     * @Version 1.0
     *          <p>
     *          <strong>Features draft description.下载监听器</strong>
     *          </p>
     */
    private class DownloadListener implements IDownloadListener {

        private WeakReference<Context> mContextRef;

        public DownloadListener(Context pContext) {
            mContextRef = new WeakReference<Context>(pContext);
        }

        @Override
        public void changeStatus(Task pTask) {
            Log.d("TAG", "changeStatus:" + pTask.status);
            if (pTask.status == Task.STATUS_DOWNLOADED) {
                // 成功与否，反注册listener
                KoDownloadManager.getInstance().setDownloadListener(null);
                if (pTask.progress > -1) {
                    if (mContextRef != null) {
                        KoStartUpActivity activity = (KoStartUpActivity) mContextRef.get();
                        if (activity != null && !activity.isFinishing()) {
                            activity.updateProgress(STEP_DOWNLOAD, pTask.progress);
                            // 启动插件
                            activity.startLoadPlugin();
                        }
                    }
                }
            } else {
                if (mContextRef != null) {
                    KoStartUpActivity activity = (KoStartUpActivity) mContextRef.get();
                    if (activity != null && !activity.isFinishing()) {
                        activity.updateProgress(STEP_DOWNLOADING, pTask.progress);
                    }
                }
            }

        }

        @Override
        public void downloadFail(Task pTask) {
            Log.d("TAG", "downloadFail:" + pTask.status);
            if (mContextRef != null) {
                KoStartUpActivity activity = (KoStartUpActivity) mContextRef.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.handleDownloadFail();
                }
            }
        }
    }

    public void exit() {
        if (mRec != null) {
            unregisterReceiver(mRec);
        }
        mRec = null;
        finish();
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    /**
     * 
     * @author Jin BinBin
     * @Create at 2015-11-5 上午1:46:14
     * @Version 1.0
     *          <p>
     *          <strong>Features draft description.主要功能介绍</strong>
     *          </p>
     */
    public static class ExitStartUpBroadcastReceiver extends BroadcastReceiver {

        private WeakReference<KoStartUpActivity> mWeakReference;

        public ExitStartUpBroadcastReceiver(KoStartUpActivity pActivity) {
            mWeakReference = new WeakReference<KoStartUpActivity>(pActivity);
        }

        public static final String FILTER_ACTION = "cn.vszone.ko.tv.receivers.StartUpQuitReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {

            if (FILTER_ACTION.equals(intent.getAction())) {
                KoStartUpActivity activity = mWeakReference.get();
                if (activity != null) {
                    activity.exit();
                }
                mWeakReference.clear();
                mWeakReference = null;
            }
        }
    }

}
