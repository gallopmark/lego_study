package com.haoyu.app.download;

import java.util.HashMap;
import java.util.Map;

/**
 * 下载管理器，断点续传
 */
public class DownloadManager {

    private Map<String, DownloadTask> mDownloadTasks;//文件下载任务索引，String为url,用来唯一区别并操作下载的文件
    private static volatile DownloadManager mInstance;

    public void pause(String url) {
        if (mDownloadTasks.containsKey(url)) {
            mDownloadTasks.get(url).pause();
        }
    }

    /**
     * 暂停
     */
    public void pause(String... urls) {
        //单任务暂停或多任务暂停下载
        for (int i = 0, length = urls.length; i < length; i++) {
            String url = urls[i];
            if (mDownloadTasks.containsKey(url)) {
                mDownloadTasks.get(url).pause();
            }
        }
    }

    public void cancel(String url) {
        if (mDownloadTasks.containsKey(url)) {
            mDownloadTasks.get(url).cancel();
        }
    }

    /**
     * 取消下载
     */
    public void cancel(String... urls) {
        //单任务取消或多任务取消下载
        for (int i = 0, length = urls.length; i < length; i++) {
            String url = urls[i];
            if (mDownloadTasks.containsKey(url)) {
                mDownloadTasks.get(url).cancel();
            }
        }
    }

    public DownloadTask create(String url) {
        DownloadTask task;
        if (mDownloadTasks.get(url) != null)
            task = mDownloadTasks.get(url);
        else
            task = new DownloadTask(url);
        mDownloadTasks.put(url, task);
        return task;
    }

    public static DownloadManager getInstance() {//管理器初始化
        if (mInstance == null) {
            synchronized (DownloadManager.class) {
                if (mInstance == null) {
                    mInstance = new DownloadManager();
                }
            }
        }
        return mInstance;
    }

    private DownloadManager() {
        mDownloadTasks = new HashMap<>();
    }

}
