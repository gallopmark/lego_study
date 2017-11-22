package com.haoyu.app.download;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

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
import okhttp3.ResponseBody;

/**
 * 创建日期：2017/11/17.
 * 描述:
 * 作者:xiaoma
 */

public class FileDownladTask extends Thread {
    private OnDownloadStatusListener mListner;//下载回调监听
    private OkHttpClient mHttpClient;
    private Context context;
    private int retryTime = 1;
    private String url;    //文件链接
    private String filePath;  //文件路径
    private String fileName;
    private Headers headers;
    private boolean isDownloading = false;
    private DownloadDao downloadDao;
    private DownloadEntity entity;
    private long totalSize;
    private long completedSize;
    private RandomAccessFile mDownLoadFile;
    private static String FILE_MODE = "rwd";
    private final int CODE_PREDOWNLOAD = 0;
    private final int CODE_PREPARED = 1;
    private final int CODE_DOWNLOADING = 2;
    private final int CODE_PAUSE = 3;
    private final int CODE_COMPLETED = 4;
    private final int CODE_ERROR = 5;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_PREDOWNLOAD:
                    if (mListner != null) {
                        mListner.onPreDownload(FileDownladTask.this);
                    }
                    break;
                case CODE_PREPARED:
                    if (mListner != null) {
                        mListner.onPrepared(FileDownladTask.this, totalSize);
                    }
                    break;
                case CODE_DOWNLOADING:
                    Bundle bundle = msg.getData();
                    long progress = bundle.getLong("progress");
                    long contentLength = bundle.getLong("fileSize");
                    if (mListner != null) {
                        mListner.onProgress(FileDownladTask.this, progress, contentLength);
                    }
                    break;
                case CODE_PAUSE:
                    if (mListner != null) {
                        mListner.onPaused(FileDownladTask.this);
                    }
                    mListner = null;
                    removeCallbacksAndMessages(null);
                    break;
                case CODE_COMPLETED:
                    isDownloading = false;
                    String savePath = (String) msg.obj;
                    if (mListner != null) {
                        mListner.onSuccess(FileDownladTask.this, savePath);
                    }
                    break;
                case CODE_ERROR:
                    isDownloading = false;
                    if (mListner != null) {
                        mListner.onFailed(FileDownladTask.this);
                    }
                    break;
            }
        }
    };

    public FileDownladTask(Builder builder) {
        mHttpClient = new OkHttpClient.Builder()
                .readTimeout(builder.READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(builder.WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(builder.CONNECT_TIMEOUT, TimeUnit.SECONDS)//设置连接超时时间
                .build();
        if (builder.context == null) {
            throw new IllegalArgumentException("context is null");
        }
        if (builder.url == null) {
            throw new IllegalArgumentException("url is null");
        }
        this.context = builder.context;
        this.url = builder.url;
        this.retryTime = builder.retryTime;
        this.filePath = builder.filePath == null ? Constants.fileDownDir : builder.filePath;
        this.fileName = builder.fileName == null ? getFileName() : builder.fileName;
        this.headers = builder.headers;
        this.mListner = builder.mListner;
        downloadDao = new DownloadDao(context);
    }

    private String getFileName() {
        if (!TextUtils.isEmpty(url) && url.lastIndexOf("/") > 0) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        return System.currentTimeMillis() + "";
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    @Override
    public void run() {
        try {
            // 获得文件路径
            entity = downloadDao.load(url);
            if (entity != null) {
                completedSize = entity.getCompletedSize();
                totalSize = entity.getTotalSize();
            }
            // 获得下载保存文件
            mDownLoadFile = new RandomAccessFile(getAbsolutePath(), FILE_MODE);
            long fileLength = mDownLoadFile.length();
            if (fileLength != completedSize) {
                completedSize = mDownLoadFile.length();
            }
            // 下载完成，更新数据库数据
            if (fileLength > 0 && totalSize > 0 && fileLength >= totalSize) {
                totalSize = completedSize = fileLength;
                entity = new DownloadEntity(url, filePath, fileName, totalSize, totalSize);
                downloadDao.insertOrReplace(entity);
                String savePath = getAbsolutePath();
                Message msg = handler.obtainMessage(CODE_COMPLETED);
                msg.obj = savePath;
                handler.sendMessage(msg);
                return;
            }
            handler.sendEmptyMessage(CODE_PREDOWNLOAD);
            if (!isDownloading) isDownloading = true;
            download();
        } catch (Exception e) {
            e.printStackTrace();
            handler.sendEmptyMessage(CODE_ERROR);
        } finally {  // 执行finally中的回调
            close(mDownLoadFile);
        }
    }

    private String getAbsolutePath() {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File targetFile = new File(filePath, fileName);
        return targetFile.getAbsolutePath();
    }

    private boolean isDownloadFinish() {
        boolean finish = false;
        if (totalSize > 0 && completedSize > 0 && totalSize == completedSize) {
            finish = true;
        }
        return finish;
    }

    private void download() {
        int retryCount = 0;
        InputStream is = null;
        try {
            Request request;
            if (headers != null) {
                request = new Request.Builder().headers(headers)
                        .header("RANGE", "bytes=" + completedSize + "-")
                        .url(url)
                        .build();
            } else {
                request = new Request.Builder().header("RANGE", "bytes=" + completedSize + "-")
                        .url(url)
                        .build();
            }
            // 文件跳转到指定位置开始写入
            mDownLoadFile.seek(completedSize);
            Response response = mHttpClient.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                if (totalSize <= 0) {
                    totalSize = responseBody.contentLength();
                }
                handler.sendEmptyMessage(CODE_PREPARED);
                // 获得文件流
                is = responseBody.byteStream();
                byte[] buffer = new byte[1024 * 2];
                long sum = completedSize;
                int len;
                while ((len = is.read(buffer)) != -1) {
                    if (!isDownloading) {
                        handler.sendEmptyMessage(CODE_PAUSE);
                        close(is);
                        close(mDownLoadFile);
                        return;
                    }
                    sum += len;
                    mDownLoadFile.write(buffer, 0, len);
                    completedSize = sum;
                    Message msg = handler.obtainMessage(CODE_DOWNLOADING);
                    Bundle bundle = new Bundle();
                    bundle.putLong("progress", completedSize);
                    bundle.putLong("fileSize", totalSize);
                    msg.setData(bundle);
                    handler.sendMessageAtTime(msg, 500);
                }
                String savePath = getAbsolutePath();
                Message msg = handler.obtainMessage(CODE_COMPLETED);
                msg.obj = savePath;
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (isDownloading) {
                if (retryCount < retryTime) {
                    retryCount++;
                    download();
                } else {
                    handler.sendEmptyMessage(CODE_ERROR);
                }
            } else {
                handler.sendEmptyMessage(CODE_PAUSE);
            }
            handler.sendEmptyMessage(CODE_ERROR);
        } finally {
            // 回收资源
            close(is);
            close(mDownLoadFile);
            // 最终都会执行，数据库更新
            insertOrUpdateDB();
        }
    }

    /**
     * 更新数据库操作
     */
    private void insertOrUpdateDB() {
        if (entity == null) {
            entity = new DownloadEntity(url, filePath, fileName, totalSize, completedSize);
        } else {
            entity.setCompletedSize(completedSize);
        }
        downloadDao.insertOrReplace(entity);
    }

    public void pause() {
        isDownloading = false;
    }

    /**
     * 取消
     */
    public void cancel() {
        if (isDownloading) isDownloading = false;
        handler.removeCallbacksAndMessages(null);
        cleanFile(new File(getAbsolutePath()));
        if (mListner != null) {
            mListner.onCancel(FileDownladTask.this);
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

    public static class Builder {
        private Context context;
        private int retryTime = 1;
        private String url;    //文件链接
        private String filePath;  //文件路径
        private String fileName;
        private Headers headers;
        private int CONNECT_TIMEOUT = 60;
        private int READ_TIMEOUT = 100;
        private int WRITE_TIMEOUT = 60;
        private OnDownloadStatusListener mListner;

        public Builder(Context context) {
            this.context = context;
        }

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

        public FileDownladTask build() {
            return new FileDownladTask(this);
        }
    }

}
