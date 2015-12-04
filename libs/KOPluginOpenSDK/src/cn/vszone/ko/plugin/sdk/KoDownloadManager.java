package cn.vszone.ko.plugin.sdk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class KoDownloadManager {

    // ===========================================================
    // Constants
    // +==========================================================

    private static final int           HTTPDL_CONNECT_TIMEOUT   = 10000;
    private static final int           HTTPDL_READ_TIMEOUT      = 10000;
    private static final int           HTTPDL_BYTES_BUFFER_SIZE = 1024 * 8;
    private static final int           HTTPDL_MESSAGE_LOOP_TIME = 1500;

    public static final int            MODE_HTTP                = 0x001;

    public static final int            MODE_SYSTEM              = 0x002;

    public static final int            MODE_ENGINE              = 0x003;

    // TODO 代码在+-号之间编写
    // -==========================================================

    // ===========================================================
    // Fields
    // +==========================================================

    private static KoDownloadManager   mManager;

    private Context                    mContext;

    private Handler                    mNotifyHandler;

    private ThreadPoolExecutor         mDownloadThreadPool;

    private IDownloadListener          mDownloadListener;

    private List<HttpDownloadRunnable> mTasks                   = new ArrayList<HttpDownloadRunnable>();

    // -==========================================================

    // ===========================================================
    // Constructors
    // +==========================================================

    private KoDownloadManager() {
        mNotifyHandler = new Handler(new DownloadCallback());
        mDownloadThreadPool =
            new ThreadPoolExecutor(0, 10, 120, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000),
                new DownThreadFactory());
    }

    // -==========================================================

    // ===========================================================
    // Getter & Setter
    // +==========================================================
    public static KoDownloadManager getInstance() {
        if (mManager == null) {
            mManager = new KoDownloadManager();
        }
        return mManager;
    }

    public void setDownloadListener(IDownloadListener pDownloadListener) {
        this.mDownloadListener = pDownloadListener;
    }

    public void cancalDownload() {
        if (mDownloadThreadPool != null) {

            int count = mTasks.size();
            for (int i = count - 1; i >= 0; i--) {
                HttpDownloadRunnable task = mTasks.get(i);
                Task t = task.getTask();
                mTasks.remove(task);
                mDownloadThreadPool.remove(task);
                File file = new File(t.filePath, t.fileName + ".temp");
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    public void pauseTask(String pUrl) {
        if (mDownloadThreadPool != null) {
            int count = mTasks.size();
            for (int i = 0; i < count; i++) {
                HttpDownloadRunnable task = mTasks.get(i);
                Task t = task.getTask();
                if (pUrl.equals(t.url)) {
                    task.isCanceled = true;
                    mTasks.remove(task);
                    mDownloadThreadPool.remove(task);
                    break;
                }

            }
        }
    }

    // -==========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // +==========================================================

    // -==========================================================

    // ===========================================================
    // Methods
    // +==========================================================

    public void init(Context pContext) {
        this.mContext = pContext;
    }

    public void addDownloadTask(int pMode, Task pTask) {
        switch (pMode) {
            case MODE_HTTP:
                if (!mTasks.contains(pTask)) {
                    Log.d("TAG", "add task=" + pTask.url);
                    HttpDownloadRunnable downloadRunnable = new HttpDownloadRunnable(pTask);
                    mTasks.add(downloadRunnable);
                    mDownloadThreadPool.execute(downloadRunnable);
                }
            break;
            case MODE_SYSTEM:
            // TODO 使用系统下载模块下载
            break;
            case MODE_ENGINE:
            // TODO 使用下载引擎模块下载
            break;

            default:
            break;
        }
    }

    public static int getFileSize(URL url) {
        int filesize = -1;
        try {
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            filesize = http.getContentLength();
            http.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filesize;
    }

    // -==========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // +==========================================================

    public interface IDownloadListener {

        public void changeStatus(Task mTask);

        public void downloadFail(Task mTask);

    }

    class DownloadCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            Task task = (Task) msg.obj;
            switch (msg.what) {
                case DownloadMsgWhat.MSG_DOWNLOAD_SUCCESS:
                    Log.d("TAG", "MSG_DOWNLOAD_SUCCESS"+task.url);
                    mTasks.remove(task);
                    if (mDownloadListener != null) {
                        mDownloadListener.changeStatus(task);
                    }
                break;
                case DownloadMsgWhat.MSG_DOWNLOAD_PROGRESS:
                    Log.d("TAG", "MSG_DOWNLOAD_PROGRESS"+task.url);
                    if (mDownloadListener != null) {
                        mDownloadListener.changeStatus(task);
                    }
                break;
                case DownloadMsgWhat.MSG_DOWNLOAD_FAIL:
                    Log.d("TAG", "MSG_DOWNLOAD_FAIL"+task.url);
                    mTasks.remove(task);
                    if (mDownloadListener != null) {
                        mDownloadListener.downloadFail(task);
                    }
                default:
                break;
            }
            return true;
        }
    }

    public final class DownloadMsgWhat {

        private DownloadMsgWhat() {

        }

        public static final int MSG_DOWNLOAD_SUCCESS  = 0x001;

        public static final int MSG_DOWNLOAD_PROGRESS = 0x002;

        public static final int MSG_DOWNLOAD_FAIL     = 0x003;

    }

    public class DownThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable pRunnable) {
            return new Thread(pRunnable, "Download-Thread");
        }
    }

    private class HttpDownloadRunnable implements Runnable {

        private Task   mTask;
        public boolean isCanceled = false;

        public HttpDownloadRunnable(Task pTask) {
            mTask = pTask;
        }

        public Task getTask() {
            return mTask;
        }

        @Override
        public void run() {
            if (!NetWorkManager.hasNetWork(mContext.getApplicationContext())) {
                mTask.progress = Task.ERROR_IO_ERROR;
                mTask.status = Task.STATUS_DOWNLOADING;
                if (mNotifyHandler != null) {
                    mNotifyHandler.removeMessages(DownloadMsgWhat.MSG_DOWNLOAD_FAIL);
                    mNotifyHandler.obtainMessage(DownloadMsgWhat.MSG_DOWNLOAD_FAIL, mTask).sendToTarget();
                }
                return;
            }
            mTask.progress = 0;
            mTask.status = Task.STATUS_DOWNLOADING;
            if (mNotifyHandler != null) {
                mNotifyHandler.removeMessages(DownloadMsgWhat.MSG_DOWNLOAD_SUCCESS);
                mNotifyHandler.obtainMessage(DownloadMsgWhat.MSG_DOWNLOAD_SUCCESS, mTask).sendToTarget();
            }
            RandomAccessFile fos = null;
            InputStream inputStream = null;
            HttpURLConnection connection = null;
            File localFile = null;

            try {
                File localDir = new File(mTask.filePath);
                if (!localDir.exists()) {
                    localDir.mkdirs();
                }
                localFile = new File(localDir, mTask.fileName + ".temp");
                long mCurSize = localFile.length();

                URL url = new URL(mTask.url);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(HTTPDL_CONNECT_TIMEOUT);
                connection.setReadTimeout(HTTPDL_READ_TIMEOUT);
                String sProperty = "bytes=" + (mCurSize) + "-";
                connection.setRequestProperty("Range", sProperty);
                long filesize = connection.getContentLength();

                File apkFile = new File(localDir, mTask.fileName);
                if (filesize <= 0) {
                    if (apkFile.exists()) {
                        mTask.progress = 100;
                        mTask.status = Task.STATUS_DOWNLOADED;
                        if (mNotifyHandler != null) {
                            mNotifyHandler.removeMessages(DownloadMsgWhat.MSG_DOWNLOAD_SUCCESS);
                            mNotifyHandler.obtainMessage(DownloadMsgWhat.MSG_DOWNLOAD_SUCCESS, mTask).sendToTarget();
                        }
                    } else {
                        mTask.progress = Task.ERROR_URL_404;
                        mTask.status = Task.STATUS_DOWNLOADING;
                        if (mNotifyHandler != null) {
                            mNotifyHandler.removeMessages(DownloadMsgWhat.MSG_DOWNLOAD_FAIL);
                            mNotifyHandler.obtainMessage(DownloadMsgWhat.MSG_DOWNLOAD_FAIL, mTask).sendToTarget();
                        }
                    }
                    return;
                }
                if (apkFile.exists() && apkFile.length() == filesize) {
                    mTask.progress = 100;
                    mTask.status = Task.STATUS_DOWNLOADED;
                    if (mNotifyHandler != null) {
                        mNotifyHandler.removeMessages(DownloadMsgWhat.MSG_DOWNLOAD_SUCCESS);
                        mNotifyHandler.obtainMessage(DownloadMsgWhat.MSG_DOWNLOAD_SUCCESS, mTask).sendToTarget();
                    }
                    return;
                }

                fos = new RandomAccessFile(localFile, "rw");
                fos.seek(mCurSize);// fis.available();?
                filesize += mCurSize;

                int responseCode = connection.getResponseCode();
                if (responseCode == 200 || responseCode == 206) {
                    inputStream = connection.getInputStream();
                    inputStream = new BufferedInputStream(inputStream);
                    byte[] buffer = new byte[HTTPDL_BYTES_BUFFER_SIZE];
                    long downloaded = mCurSize;
                    long lastNotifyTS = 0;
                    int length = 0;
                    while ((length = inputStream.read(buffer)) != -1 && !isCanceled) {
                        fos.write(buffer, 0, length);
                        downloaded += length;
                        long curr = System.currentTimeMillis();
                        if ((curr - lastNotifyTS) > HTTPDL_MESSAGE_LOOP_TIME / 3) {
                            lastNotifyTS = curr;
                            float progress = downloaded * 100f / filesize;
                            mTask.progress = progress;
                            mTask.status = Task.STATUS_DOWNLOADING;
                            if (mNotifyHandler != null) {
                                mNotifyHandler.removeMessages(DownloadMsgWhat.MSG_DOWNLOAD_PROGRESS);
                                mNotifyHandler.obtainMessage(DownloadMsgWhat.MSG_DOWNLOAD_PROGRESS, mTask)
                                        .sendToTarget();
                            }
                        }
                    }
                    if (isCanceled) {

                    } else {
                        long tempFileLength = localFile.length();
                        if (tempFileLength != filesize) {
                            mTask.progress = Task.ERROR_DOWNLOADED_FILE_INVALID;
                            mTask.status = Task.STATUS_DOWNLOADING;
                            if (mNotifyHandler != null) {
                                mNotifyHandler.removeMessages(DownloadMsgWhat.MSG_DOWNLOAD_SUCCESS);
                                mNotifyHandler.obtainMessage(DownloadMsgWhat.MSG_DOWNLOAD_SUCCESS, mTask)
                                        .sendToTarget();
                            }
                            if (localFile != null && localFile.exists()) {
                                localFile.delete();
                            }
                            return;
                        }

                        mTask.progress = 100;
                        mTask.status = Task.STATUS_DOWNLOADED;
                        localFile.renameTo(new File(localDir, mTask.fileName));
                        if (mNotifyHandler != null) {
                            mNotifyHandler.removeMessages(DownloadMsgWhat.MSG_DOWNLOAD_SUCCESS);
                            mNotifyHandler.obtainMessage(DownloadMsgWhat.MSG_DOWNLOAD_SUCCESS, mTask).sendToTarget();
                        }
                    }
                } else {
                    mTask.progress = Task.ERROR_IO_ERROR;
                    mTask.status = Task.STATUS_DOWNLOADING;
                    if (mNotifyHandler != null) {
                        mNotifyHandler.removeMessages(DownloadMsgWhat.MSG_DOWNLOAD_FAIL);
                        mNotifyHandler.obtainMessage(DownloadMsgWhat.MSG_DOWNLOAD_FAIL, mTask).sendToTarget();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                mTask.progress = Task.ERROR_URL_INVALID;
                mTask.status = Task.STATUS_DOWNLOADING;
                if (mNotifyHandler != null) {
                    mNotifyHandler.removeMessages(DownloadMsgWhat.MSG_DOWNLOAD_FAIL);
                    mNotifyHandler.obtainMessage(DownloadMsgWhat.MSG_DOWNLOAD_FAIL, mTask).sendToTarget();
                }
                if (localFile != null && localFile.exists()) {
                    localFile.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
                mTask.progress = Task.ERROR_IO_ERROR;
                mTask.status = Task.STATUS_DOWNLOADING;
                if (mNotifyHandler != null) {
                    mNotifyHandler.removeMessages(DownloadMsgWhat.MSG_DOWNLOAD_FAIL);
                    mNotifyHandler.obtainMessage(DownloadMsgWhat.MSG_DOWNLOAD_FAIL, mTask).sendToTarget();
                }
                if (localFile != null && localFile.exists()) {
                    localFile.delete();
                }
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    public static class Task {
        public Task() {

        }

        /**
         * 下载链接无法访问
         */
        public static final int ERROR_URL_404                 = -1;
        /**
         * 断点续传有误
         */
        public static final int ERROR_DOWNLOADED_FILE_INVALID = -2;
        /**
         * MalformedURLException
         */
        public static final int ERROR_URL_INVALID             = -3;
        /**
         * IOException
         */
        public static final int ERROR_IO_ERROR                = -4;

        public static final int STATUS_DOWNLOADING            = 0x001;
        public static final int STATUS_DOWNLOADED             = 0x002;
        public String           id;
        public String           url;
        public String           filePath;
        public String           fileName;
        public String           cid;
        public int              status;
        public float            progress;

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof Task) {
                Task other = (Task) o;
                if (!TextUtils.isEmpty(url)) {
                    return url.equals(other.url);
                }
            }
            return super.equals(o);
        }
    }

    // -==========================================================
}
