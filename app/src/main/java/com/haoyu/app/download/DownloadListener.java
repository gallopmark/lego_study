package com.haoyu.app.download;

/**
 * 创建日期：2017/8/8 on 13:43
 * 描述:
 * 作者:马飞奔 Administrator
 */
public interface DownloadListener {
    /**
     * 通知当前的下载进度
     */
    void onProgress(DownloadTask downloadTask, long soFarBytes, long totalBytes);

    /**
     * 通知下载成功
     */
    void onSuccess(DownloadTask downloadTask, String savePath);

    /**
     * 通知下载失败
     */
    void onFailed(DownloadTask downloadTask);

    /**
     * 通知下载暂停
     */
    void onPaused(DownloadTask downloadTask);

    void onCancel(DownloadTask downloadTask);
}
