/**
 * Copyright (C) © 2014 深圳市掌玩网络技术有限公司
 * KoTvGameBox
 * KoTvBaseActivity.java
 */
package cn.vszone.ko.plugin.sdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author stanley
 * @Create at 2015年11月10日 上午11:31:27
 * @Version 1.0
 *          <p>
 *          <strong>Features draft description.主要功能介绍</strong>
 *          </p>
 */
public class CET189storeUrlFetcher {

    // ===========================================================
    // Constants
    // ===========================================================
    private static final int         DEFAULT_SOCKET_TIMEOUT     = 10 * 1000;
    private static final int         DEFAULT_MAX_CONNECTIONS    = 16;
    private static final int         DEFAULT_MAX_RETRIES        = 3;
    private static final int         DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    private static final String      HEADER_ACCEPT_ENCODING     = "Accept-Encoding";
    private static final String      ENCODING_GZIP              = "gzip";

    private static int               maxConnections             = DEFAULT_MAX_CONNECTIONS;
    private static int               socketTimeout              = DEFAULT_SOCKET_TIMEOUT;
    private static final char[]      HEX_CHARS                  = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'                       };

    // ===========================================================
    // Fields
    // ===========================================================

    private AbstractHttpClient       mHttpDefaultClient;

    private FetchDownloadUrlCallback mFetchDownloadUrlCallback;
    private static final String      LOG_TAG                    = "CET189storeUrlUtils";
    // ===========================================================
    // Constructors
    // ===========================================================
    private String                   mPkgHash;
    private String                   mApkUrl;
    private String                   mLocalApkFullPath;

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    public void fetchDownloadUrl(Context pCtx, String pUrl, String pLocalApkFullPath, FetchDownloadUrlCallback pCallback) {
        mFetchDownloadUrlCallback = pCallback;
        mLocalApkFullPath = pLocalApkFullPath;
        CETRequestTask reqTask = new CETRequestTask();
        reqTask.execute(pUrl);
    }

    private void makeRequest(String pUrl) {

        Log.d(LOG_TAG, "makeRequest: " + pUrl);

        mHttpDefaultClient = new DefaultHttpClient();
        //请求超时
        mHttpDefaultClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, socketTimeout); 
        //读取超时
        mHttpDefaultClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeout);
        mHttpDefaultClient.addResponseInterceptor(new HttpResponseInterceptor() {
            @Override
            public void process(HttpResponse response, HttpContext context) {

                if (response != null && response.getStatusLine() != null) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    Log.d(LOG_TAG, "StatusCode:" + statusCode);
                    if (statusCode == 302) {
                        Header headerFileHash = response.getFirstHeader("package_hash");
                        if (headerFileHash != null) {
                            mPkgHash = headerFileHash.getValue();
                            Log.d(LOG_TAG, "Header package_hash: " + mPkgHash);
                        } else {
                            Log.w(LOG_TAG, "Header package_hash not found");
                        }
                        Header headerLocation = response.getFirstHeader("Location");
                        if (headerLocation != null) {
                            Log.d(LOG_TAG, "Location :" + headerLocation.getValue());
                            mApkUrl = headerLocation.getValue();
                        } else {
                            Log.w(LOG_TAG, "Header Location not found");
                        }
                    }
                }
            }
        });

        HttpGet request = new HttpGet(pUrl);
        try {
            mHttpDefaultClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public class CETRequestTask extends AsyncTask<String, Void, Void> {

        private String oldApkHash = "";

        @Override
        protected Void doInBackground(String... params) {
            String url = params[0];
            if (!TextUtils.isEmpty(url)) {
                makeRequest(url);
            }
            
            File localApk = new File(mLocalApkFullPath);
            Log.d(LOG_TAG, "localApk file: " + localApk.getAbsolutePath());
            if (localApk.exists()) {
                // 计算md5
                oldApkHash = getFileMD5(localApk);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mFetchDownloadUrlCallback != null) {
                mFetchDownloadUrlCallback.onSuccess(oldApkHash, mPkgHash, mApkUrl);
            }
            super.onPostExecute(result);
        }
    }

    /**
     * 获取单个文件的MD5值！
     * 
     * @param file
     * @return
     */

    public static String getFileMD5(File file) {
        String md5 = "";
        if (file != null && file.exists()) {
            MessageDigest digest = null;
            FileInputStream in = null;
            byte buffer[] = new byte[1024];
            int len;
            try {
                digest = MessageDigest.getInstance("MD5");
                in = new FileInputStream(file);
                while ((len = in.read(buffer, 0, 1024)) != -1) {
                    digest.update(buffer, 0, len);
                }

                BigInteger bigInt = new BigInteger(1, digest.digest());
                md5 = bigInt.toString(16);

            } catch (Exception e) {
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        return md5;

    }

    public interface FetchDownloadUrlCallback {

        public void onSuccess(String oldApkHash, String pNewApkHash, String pApkUrl);

        public void onFailure(Throwable error, String content);
    }
}
