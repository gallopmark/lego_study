package com.haoyu.app.download.db;

/**
 * 创建日期：2017/5/18 on 17:34
 * 描述: 下载的文件信息
 * 作者:马飞奔 Administrator
 */
public class DownloadFileInfo {
    private Long id;
    private String url;    //文件链接
    private String filePath;  //文件路径
    private int soFarBytes;   //文件已下载长度
    private int totalBytes;     //文件总长度
    private String fileName;

    public DownloadFileInfo(String url, String filePath, String fileName) {
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public DownloadFileInfo(Long id, String url, String filePath, int soFarBytes,
                            int totalBytes, String fileName) {
        this.id = id;
        this.url = url;
        this.filePath = filePath;
        this.soFarBytes = soFarBytes;
        this.totalBytes = totalBytes;
        this.fileName = fileName;
    }

    public DownloadFileInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getSoFarBytes() {
        return soFarBytes;
    }

    public void setSoFarBytes(int soFarBytes) {
        this.soFarBytes = soFarBytes;
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(int totalBytes) {
        this.totalBytes = totalBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
