package cn.vszone.ko.plugin.sample;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import cn.vszone.ko.plugin.framework.PluginLoadListener;
import cn.vszone.ko.plugin.framework.utils.PluginOpener;
import cn.vszone.ko.plugin.sdk.KoStartUpActivity;
import cn.vszone.ko.plugin.sdk.misc.BranchConfig;

public class StartPluginWithDownloadActivity extends Activity {

    private final static String LOG_TAG             = "PluginDemo";

     ##首先， 请务必正确设置AndroidManifest.xml的 KO_APP_KEY，KO_APP_ID，具体参考文档

    ##插件文件名
    private String              mApkFileName        = "";
    ##插件在本地sdcard的目录
    private String              mLocalPluginDirPath = "";
    ##插件文件名下载地址，针对方案2需要设置
    private String              mPluginDownloadUrl  = "";

    ##是否让插件直接使用宿主的so
    private boolean             mUseHostNativeLibs  = true;

    private Button              mStartLocal;
    private Button              mDownloadApk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartLocal = (Button) findViewById(R.id.main_btn_start_local_plugin);
        mDownloadApk = (Button) findViewById(R.id.main_btn_download_plugin);
        BranchConfig.isTvArena = true;
        initView();
        // 若没有设置各种路径
        if (TextUtils.isEmpty(mApkFileName) ||TextUtils.isEmpty(mLocalPluginDirPath)  || TextUtils.isEmpty(mPluginDownloadUrl)) {
            initData();
        }
    }

    private void initView() {
        if (BranchConfig.isTvArena) {
            mStartLocal.setText(R.string.tv_start_plugin);
            mDownloadApk.setText(R.string.tv_start_download_launch_plugin);
        } else {
            mStartLocal.setText(R.string.start_plugin);
            mDownloadApk.setText(R.string.start_download_launch_plugin);
        }
    }

    private void initData() {
        if (BranchConfig.isTvArena) {
            mLocalPluginDirPath = getString(R.string.tv_plugin_dir_path);
            mPluginDownloadUrl = getString(R.string.tv_plugin_download_url);
            mApkFileName = getString(R.string.tv_plugin_filename);
        } else {
            mLocalPluginDirPath = getString(R.string.plugin_dir_path);
            mPluginDownloadUrl = getString(R.string.plugin_download_url);
            mApkFileName = getString(R.string.plugin_filename);
        }

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

    public void onOpenLocalPluginClick(View v) {
        String apkFullPath = mLocalPluginDirPath + mApkFileName;
        if (!TextUtils.isEmpty(apkFullPath)) {
            File file = new File(apkFullPath);
            if (file.exists()) {
                ## 如果自行处理插件下载，下载好后自己调用该方法：mUseHostNativeLibs 一般为false， 如果宿主已经包含了插件的所有so，设置为true
                PluginOpener.startPlugin(this, apkFullPath, mUseHostNativeLibs, new OnPluginLoadListener());
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
        intent.putExtra(KoStartUpActivity.KEY_FILE_NAME, mApkFileName);
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
