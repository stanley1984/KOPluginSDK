package cn.vszone.ko.plugin.sample;

import cn.vszone.ko.plugin.framework.PluginLoadListener;
import cn.vszone.ko.plugin.framework.utils.PluginOpener;
import cn.vszone.ko.plugin.sdk.KoStartUpActivity;
import cn.vszone.ko.plugin.sdk.misc.BranchConfig;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class StartPluginWithDownloadActivity extends Activity {

    private final static String LOG_TAG             = "PluginDemo";

    // 首先， 请务必正确设置AndroidManifest.xml的 KO_APP_KEY，KO_APP_ID，具体参考文档

    // 插件文件名
    private String              mApkFileName        = "";
    // 插件在本地sdcard的目录
    private String              mLocalPluginDirPath = "";
    // 插件文件名下载地址，针对方案2需要设置
    private String              mPluginDownloadUrl  =
                                                        "http://estoresrvice.189store.com/api/app/download/app.json?clientid=36e162193e94c714566e369b832ae0fa&packagename=cn.vszone.ko.mobile.arena&app_id=2293017&chn=tianyi&source=com.eshore.ezone_5098&rn=1&is_update=0";

    public static final String           DEFAULT_APK_DOENLOAD_URL_TV =
            "http://download.vszone.cn/android/KoTvArena_kobox_latest.apk";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocalPluginDirPath = getString(R.string.plugin_dir_path);
//        mPluginDownloadUrl = getString(R.string.plugin_download_url);
        mApkFileName = getString(R.string.plugin_filename);
        BranchConfig.isTvArena = true;
        if(BranchConfig.isTvArena){
            mPluginDownloadUrl = DEFAULT_APK_DOENLOAD_URL_TV;
        }
        // mLocalPluginPathView = (EditText)
        // this.findViewById(R.id.main_btn_start_local_plugin);
        // mPluginDownloadUrlView = (EditText)
        // this.findViewById(R.id.main_et_plugin_url);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        Intent intent = new Intent();
        intent.setAction(KoStartUpActivity.ExitStartUpBroadcastReceiver.FILTER_ACTION);
        sendBroadcast(intent);
        Log.d("TAG", "onStop");
        super.onDestroy();
    }



    public void onOpenLocalPluginClick(View v) {
        String apkFullPath = mLocalPluginDirPath + mApkFileName;
        if (!TextUtils.isEmpty(apkFullPath)) {
            File file = new File(apkFullPath);
            if (file.exists()) {
                PluginOpener.startPlugin(this, apkFullPath, false, new OnPluginLoadListener());
            } else {
                Toast.makeText(this, "插件文件不存在，请先放置到：" + apkFullPath, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onDownloadOpenPluginClick(View v) {
        Intent intent = new Intent(this, KoStartUpActivity.class);
        intent.putExtra(KoStartUpActivity.KEY_DOWNLOAD_URL, mPluginDownloadUrl);
        File dir = new File(mLocalPluginDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        intent.putExtra(KoStartUpActivity.KEY_FILE_PATH, mLocalPluginDirPath);
        startActivity(intent);
    }

    public class OnPluginLoadListener implements PluginLoadListener {

        @Override
        public void onLoadFailed() {
            // 插件加载失败处理
            Log.e(LOG_TAG, "onLoadFailed...");

            Toast.makeText(StartPluginWithDownloadActivity.this, "加载失败！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoadFinished() {
            // 插件加载成功
            Toast.makeText(StartPluginWithDownloadActivity.this, "加载成功.", Toast.LENGTH_SHORT).show();
            Log.i(LOG_TAG, "onLoadFinished.");
        }

        @Override
        public void onLoadStart() {
            // 插件开始加载
            Toast.makeText(StartPluginWithDownloadActivity.this, "正在加载，请稍后...", Toast.LENGTH_SHORT).show();
            Log.i(LOG_TAG, "onLoadStart.");
        }

    }
}
