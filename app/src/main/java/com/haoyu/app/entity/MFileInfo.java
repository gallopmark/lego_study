package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2016/12/23 on 11:07
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MFileInfo implements Serializable {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("fileName")
    private String fileName;
    @Expose
    @SerializedName("url")
    private String url;
    @Expose
    @SerializedName("fileSize")
    private long fileSize;
    @Expose
    @SerializedName("relativeUrl")
    private String relativeUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public void setRelativeUrl(String relativeUrl) {
        this.relativeUrl = relativeUrl;
    }
}
