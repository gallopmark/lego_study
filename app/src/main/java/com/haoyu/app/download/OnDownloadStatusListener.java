package com.haoyu.app.download;

/**
 * 创建日期：2017/11/17.
 * 描述:下载监听器
 * 作者:xiaoma
 */

public interface OnDownloadStatusListener {
    void onPreDownload(FileDownloadTask downloadTask);

    void onPrepared(FileDownloadTask downloadTask, long fileSize);

    /**
     * 通知当前的下载进度
     */
    void onProgress(FileDownloadTask downloadTask, long soFarBytes, long totalBytes);

    /**
     * 通知下载成功
     */
    void onSuccess(FileDownloadTask downloadTask, String savePath);

    /**
     * 通知下载失败
     */
    void onFailed(FileDownloadTask downloadTask);

    /**
     * 通知下载暂停
     */
    void onPaused(FileDownloadTask downloadTask);

    void onCancel(FileDownloadTask downloadTask);
}
