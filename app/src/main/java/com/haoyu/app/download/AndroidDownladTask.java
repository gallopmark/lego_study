package com.haoyu.app.download;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.haoyu.app.utils.Constants;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 创建日期：2017/11/17.
 * 描述:
 * 作者:xiaoma
 */

public class AndroidDownladTask extends Thread {
    private File mTmpFile;//临时占位文件
    private OnDownloadStatusListener mListner;//下载回调监听
    private OkHttpClient mHttpClient;
    private int retryTime = 1;
    private String url;    //文件链接
    private String filePath;  //文件路径
    private String fileName;
    private Headers headers;
    private boolean isDownloading = false;
    private final int CODE_PREPARED = 1;
    private final int CODE_DOWNLOADING = 2;
    private final int CODE_PAUSE = 3;
    private final int CODE_COMPLETED = 4;
    private final int CODE_ERROR = 5;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_PREPARED:
                    long fileSize = (long) msg.obj;
                    if (mListner != null) {
                        mListner.onPrepared(AndroidDownladTask.this, fileSize);
                    }
                    break;
                case CODE_DOWNLOADING:
                    Bundle bundle = msg.getData();
                    long progress = bundle.getLong("progress");
                    long contentLength = bundle.getLong("fileSize");
                    if (mListner != null) {
                        mListner.onProgress(AndroidDownladTask.this, progress, contentLength);
                    }
                    break;
                case CODE_PAUSE:
                    if (mListner != null) {
                        mListner.onPaused(AndroidDownladTask.this);
                    }
                    break;
                case CODE_COMPLETED:
                    isDownloading = false;
                    String savePath = (String) msg.obj;
                    if (mListner != null) {
                        mListner.onSuccess(AndroidDownladTask.this, savePath);
                    }
                    break;
                case CODE_ERROR:
                    isDownloading = false;
                    if (mListner != null) {
                        mListner.onFailed(AndroidDownladTask.this);
                    }
                    break;
            }
        }
    };

    public static class Builder {
        private int retryTime = 1;
        private String url;    //文件链接
        private String filePath;  //文件路径
        private String fileName;
        private Headers headers;
        private int CONNECT_TIMEOUT = 60;
        private int READ_TIMEOUT = 100;
        private int WRITE_TIMEOUT = 60;
        private OnDownloadStatusListener mListner;

        public Builder setRetryTime(int retryTime) {
            this.retryTime = retryTime;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setHeaders(Map<String, String> headersParams) {
            Headers.Builder builder = new Headers.Builder();
            if (headersParams != null) {
                Iterator<String> iterator = headersParams.keySet().iterator();
                String key;
                while (iterator.hasNext()) {
                    key = iterator.next().toString();
                    builder.add(key, headersParams.get(key));
                }
            }
            this.headers = builder.build();
            return this;
        }

        public Builder setWRITE_TIMEOUT(int WRITE_TIMEOUT) {
            this.WRITE_TIMEOUT = WRITE_TIMEOUT;
            return this;
        }

        public Builder setCONNECT_TIMEOUT(int CONNECT_TIMEOUT) {
            this.CONNECT_TIMEOUT = CONNECT_TIMEOUT;
            return this;
        }

        public Builder setREAD_TIMEOUT(int READ_TIMEOUT) {
            this.READ_TIMEOUT = READ_TIMEOUT;
            return this;
        }

        public Builder setmListner(OnDownloadStatusListener mListner) {
            this.mListner = mListner;
            return this;
        }

        public AndroidDownladTask build() {
            return new AndroidDownladTask(this);
        }
    }

    public AndroidDownladTask(Builder builder) {
        mHttpClient = new OkHttpClient.Builder()
                .readTimeout(builder.READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(builder.WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(builder.CONNECT_TIMEOUT, TimeUnit.SECONDS)//设置连接超时时间
                .build();
        this.url = builder.url;
        this.retryTime = builder.retryTime;
        this.filePath = builder.filePath == null ? Constants.fileDownDir : builder.filePath;
        this.fileName = builder.fileName == null ? getFileName() : builder.fileName;
        this.headers = builder.headers;
        this.mListner = builder.mListner;
    }

    private String getFileName() {
        if (fileName == null) {
            if (url != null && url.lastIndexOf("/") != -1) {
                fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
            }
        }
        return fileName;
    }

    public AndroidDownladTask addHeaders(Map<String, String> headersParams) {
        Headers.Builder builder = new Headers.Builder();
        if (headersParams != null) {
            Iterator<String> iterator = headersParams.keySet().iterator();
            String key;
            while (iterator.hasNext()) {
                key = iterator.next().toString();
                builder.add(key, headersParams.get(key));
            }
        }
        this.headers = builder.build();
        return this;
    }

    public AndroidDownladTask addListener(OnDownloadStatusListener listener) {
        this.mListner = listener;
        return this;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    @Override
    public void run() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {   //如果sd卡不可用则返回
            handler.sendEmptyMessage(CODE_ERROR);
            return;
        }
        File fileDir = new File(filePath);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        mTmpFile = new File(filePath, fileName + ".temp");
        if (!isDownloading) isDownloading = true;
        long fileSize = getContentLength(url);
        if (fileSize > 0) {
            download(fileSize);
        }
    }

    /**
     * 得到下载内容的大小
     *
     * @param url
     * @return
     */
    private long getContentLength(String url) {
        int retryCount = 0;
        try {
            Request request;
            if (headers != null) {
                request = new Request.Builder().url(url).headers(headers).build();
            } else {
                request = new Request.Builder().url(url).build();
            }
            Response response = mHttpClient.newCall(request).execute();
            long contentLength = 0;
            if (response != null && response.isSuccessful()) {
                contentLength = response.body().contentLength();
                response.body().close();
            }
            Message msg = handler.obtainMessage(CODE_PREPARED);
            msg.obj = contentLength;
            handler.sendMessage(msg);
            return contentLength;
        } catch (Exception e) {
            if (isDownloading) {
                if (retryCount < retryTime) {
                    retryCount++;
                    return getContentLength(url);
                } else {
                    handler.sendEmptyMessage(CODE_ERROR);
                    return -1;
                }
            } else {
                handler.sendEmptyMessage(CODE_PAUSE);
                return 0;
            }
        }
    }

    private void download(long contentLength) {
        int retryCount = 0;
        long downloadLength = 0;   //记录已经下载的文件长度
        if (mTmpFile.exists()) {
            downloadLength = mTmpFile.length();
        }
        Request request;
        if (headers != null) {
            request = new Request.Builder().headers(headers)
                    .header("RANGE", "bytes=" + downloadLength + "-")
                    .url(url)
                    .build();
        } else {
            request = new Request.Builder().header("RANGE", "bytes=" + downloadLength + "-")
                    .url(url)
                    .build();
        }
        InputStream is = null;
        RandomAccessFile savedFile = null;
        try {
            Response response = mHttpClient.newCall(request).execute();
            is = response.body().byteStream();
            savedFile = new RandomAccessFile(mTmpFile, "rw");
            savedFile.seek(downloadLength);//跳过已经下载的字节
            byte[] buffer = new byte[1024 * 4];
            int total = 0;
            int len;
            while ((len = is.read(buffer)) != -1) {
                if (!isDownloading) {
                    handler.sendEmptyMessage(CODE_PAUSE);
                    close(is);
                    close(savedFile);
                    return;
                }
                total += len;
                savedFile.write(buffer, 0, len);
                long progress = total + downloadLength;
                Message msg = handler.obtainMessage(CODE_DOWNLOADING);
                Bundle bundle = new Bundle();
                bundle.putLong("progress", progress);
                bundle.putLong("fileSize", contentLength);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
            response.body().close();
            File targetFile = new File(filePath, fileName);
            if (!targetFile.exists()) {
                mTmpFile.renameTo(targetFile);//下载完毕后，重命名目标文件
            }
            String savePath = targetFile.getAbsolutePath();
            Message msg = handler.obtainMessage(CODE_COMPLETED);
            msg.obj = savePath;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
            if (isDownloading) {
                if (retryCount < retryTime) {
                    retryCount++;
                    download(contentLength);
                } else {
                    handler.sendEmptyMessage(CODE_ERROR);
                }
            } else {
                handler.sendEmptyMessage(CODE_PAUSE);
            }
        } finally {
            close(is);
            close(savedFile);
        }
    }

    public void pause() {
        if (isDownloading) isDownloading = false;
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * 取消
     */
    public void cancel() {
        if (isDownloading) isDownloading = false;
        handler.removeCallbacksAndMessages(null);
        cleanFile(mTmpFile);
        if (mListner != null) {
            mListner.onCancel(AndroidDownladTask.this);
        }
    }

    /**
     * 删除临时文件
     */
    private void cleanFile(File... files) {
        for (int i = 0, length = files.length; i < length; i++) {
            if (null != files[i])
                files[i].delete();
        }
    }


    /**
     * 关闭资源
     *
     * @param closeables
     */
    private void close(Closeable... closeables) {
        int length = closeables.length;
        try {
            for (int i = 0; i < length; i++) {
                Closeable closeable = closeables[i];
                if (null != closeable)
                    closeables[i].close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < length; i++) {
                closeables[i] = null;
            }
        }
    }
}
