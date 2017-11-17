package com.haoyu.app.download;

/**
 * 创建日期：2017/11/17.
 * 描述:下载监听器
 * 作者:xiaoma
 */

public interface OnDownloadStatusListener {
    void onPrepared(AndroidDownladTask downloadTask, long fileSize);

    /**
     * 通知当前的下载进度
     */
    void onProgress(AndroidDownladTask downloadTask, long soFarBytes, long totalBytes);

    /**
     * 通知下载成功
     */
    void onSuccess(AndroidDownladTask downloadTask, String savePath);

    /**
     * 通知下载失败
     */
    void onFailed(AndroidDownladTask downloadTask);

    /**
     * 通知下载暂停
     */
    void onPaused(AndroidDownladTask downloadTask);

    void onCancel(AndroidDownladTask downloadTask);
}
