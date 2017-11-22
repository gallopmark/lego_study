package com.haoyu.app.download;

public class DownloadEntity {
    private String url;
    private String saveDirPath;
    private String fileName;
    private long totalSize;
    private long completedSize;

    public DownloadEntity() {
    }

    public DownloadEntity(String url, String saveDirPath, String fileName, long totalSize, long completedSize) {
        this.url = url;
        this.saveDirPath = saveDirPath;
        this.fileName = fileName;
        this.totalSize = totalSize;
        this.completedSize = completedSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSaveDirPath() {
        return saveDirPath;
    }

    public void setSaveDirPath(String saveDirPath) {
        this.saveDirPath = saveDirPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getCompletedSize() {
        return completedSize;
    }

    public void setCompletedSize(long completedSize) {
        this.completedSize = completedSize;
    }
}
