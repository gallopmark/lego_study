package com.haoyu.app.download;

import android.content.Context;
import android.os.Environment;

import com.haoyu.app.download.db.DownloadDBManager;
import com.haoyu.app.download.db.DownloadFileInfo;
import com.haoyu.app.utils.Constants;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask {
    private File mTmpFile;//临时占位文件
    private boolean isDownloading = false;
    private DownloadListener mListner;//下载回调监听
    private OkHttpClient mHttpClient;
    private int CONNECT_TIMEOUT = 60;
    private int READ_TIMEOUT = 100;
    private int WRITE_TIMEOUT = 60;
    private int retryTime = 1;
    private String url;    //文件链接
    private String filePath;  //文件路径
    private String fileName;
    private Headers headers;
    private Disposable disposable;
    private DownloadDBManager dbManager;

    public DownloadTask(Context context, String url) {
        this.url = url;
        mHttpClient = new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)//设置连接超时时间
                .build();
        dbManager = new DownloadDBManager(context);
    }

    public DownloadTask setRetryTime(int retryTime) {
        this.retryTime = retryTime;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public DownloadTask setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public DownloadTask setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public String getFileName() {
        if (fileName == null) {
            if (url != null && url.lastIndexOf("/") != -1) {
                fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
            }
        }
        return fileName;
    }

    public DownloadTask setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public DownloadTask addHeaders(Map<String, String> headersParams) {
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

    public DownloadTask addListener(DownloadListener listener) {
        this.mListner = listener;
        return this;
    }

    public synchronized void start() {
        if (isDownloading) return;
        isDownloading = true;
        disposable = Flowable.just(url).map(new Function<String, Long>() {
            @Override
            public Long apply(String url) throws Exception {
                return getContentLength(url);
            }
        }).map(new Function<Long, String>() {
            @Override
            public String apply(Long contentLength) throws Exception {
                if (contentLength == -1) {
                    Flowable.just(DownloadTask.this).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<DownloadTask>() {
                        @Override
                        public void accept(DownloadTask downloadTask) throws Exception {
                            if (mListner != null)
                                mListner.onFailed(downloadTask);
                        }
                    });
                    return null;
                }
                return download(contentLength);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String savePath) throws Exception {
                resetStutus();
                if (savePath != null) {
                    dbManager.save(new DownloadFileInfo(url, filePath, fileName));
                    if (mListner != null)
                        mListner.onSuccess(DownloadTask.this, savePath);
                } else {
                    if (mListner != null)
                        mListner.onFailed(DownloadTask.this);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                resetStutus();
                if (mListner != null)
                    mListner.onFailed(DownloadTask.this);
            }
        });
    }

    /**
     * 得到下载内容的大小
     *
     * @param url
     * @return
     */
    private long getContentLength(String url) {
        int retryCount = 0;
        Request request;
        if (headers != null) {
            request = new Request.Builder().url(url).headers(headers).build();
        } else
            request = new Request.Builder().url(url).build();
        try {
            Response response = mHttpClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.body().close();
                return contentLength;
            }
        } catch (Exception e) {
            if (e.getCause().equals(SocketTimeoutException.class) && retryCount < retryTime) {//如果超时并未超过指定次数，则重新连接
                retryCount++;
                getContentLength(url);
            } else {
                return -1;
            }
        }
        return -1;
    }

    private String download(long contentLength) {
        int retryCount = 0;
        long downloadLength = 0;   //记录已经下载的文件长度
        filePath = filePath == null ? Constants.fileDownDir : filePath;
        fileName = fileName == null ? getFileName() : fileName;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {   //如果sd卡不可用则返回
            Flowable.just(DownloadTask.this).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<DownloadTask>() {
                @Override
                public void accept(DownloadTask downloadTask) throws Exception {
                    if (mListner != null)
                        mListner.onFailed(downloadTask);
                }
            });
            return null;
        }
        File fileDir = new File(filePath);
        if (!fileDir.exists())
            fileDir.mkdir();
        mTmpFile = new File(filePath, fileName + ".tmp");
        if (mTmpFile.exists()) {
            downloadLength = mTmpFile.length();
        }
        Request request;
        if (headers != null) {
            request = new Request.Builder().headers(headers)
                    .header("RANGE", "bytes=" + downloadLength + "-")
                    .url(url)
                    .build();
        } else
            request = new Request.Builder().header("RANGE", "bytes=" + downloadLength + "-")
                    .url(url)
                    .build();
        InputStream is = null;
        RandomAccessFile savedFile = null;
        try {
            Response response = mHttpClient.newCall(request).execute();
            is = response.body().byteStream();
            savedFile = new RandomAccessFile(mTmpFile, "rw");
            savedFile.seek(downloadLength);//跳过已经下载的字节
            byte[] b = new byte[1024];
            int total = 0;
            int len;
            while ((len = is.read(b)) != -1) {
                total += len;
                savedFile.write(b, 0, len);
                Flowable.just(new long[]{(total + downloadLength), contentLength}).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<long[]>() {
                    @Override
                    public void accept(long[] params) throws Exception {
                        if (mListner != null)
                            mListner.onProgress(DownloadTask.this, params[0], params[1]);
                    }
                });
            }
            response.body().close();
            if (!mTmpFile.exists())
                return null; //重命名文件不存在
            File targetFile = new File(filePath, fileName);
            if (targetFile.exists())
                return targetFile.getAbsolutePath();
            else
                mTmpFile.renameTo(targetFile);//下载完毕后，重命名目标文件
            return targetFile.getAbsolutePath();
        } catch (Exception e) {
            if (e.getCause().equals(SocketTimeoutException.class) && retryCount < retryTime) {
                retryCount++;
                return download(contentLength);
            } else
                return null;
        } finally {
            close(is);
            close(savedFile);
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
     * 暂停
     */
    public void pause() {
        resetStutus();
        if (disposable != null)
            disposable.dispose();
        if (mListner != null)
            mListner.onPaused(DownloadTask.this);
    }

    /**
     * 取消
     */
    public void cancel() {
        resetStutus();
        if (disposable != null)
            disposable.dispose();
        cleanFile(mTmpFile);
        if (!isDownloading) {
            if (null != mListner) {
                resetStutus();
                mListner.onCancel(DownloadTask.this);
            }
        }
    }

    /**
     * 重置下载状态
     */
    private void resetStutus() {
        isDownloading = false;
    }

    public boolean isDownloading() {
        return isDownloading;
    }
}
