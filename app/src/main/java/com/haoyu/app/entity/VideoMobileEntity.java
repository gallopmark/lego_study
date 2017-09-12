package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2016/12/22 on 15:29
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class VideoMobileEntity implements Serializable {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("viewTime")
    private double viewTime;
    @Expose
    @SerializedName("interval")
    private int interval;
    @Expose
    @SerializedName("type")
    private String type;
    @Expose
    @SerializedName("allowDownload")
    private String allowDownload;
    @Expose
    @SerializedName("summary")
    private String summary;
    @Expose
    @SerializedName("urls")
    private String urls;
    @Expose
    @SerializedName("videoFiles")
    private List<MFileInfo> videoFiles = new ArrayList<>();
    @Expose
    @SerializedName("attchFiles")
    private List<MFileInfo> attchFiles = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getViewTime() {
        return viewTime;
    }

    public void setViewTime(double viewTime) {
        this.viewTime = viewTime;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getInterval() {
        return interval;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAllowDownload() {
        return allowDownload;
    }

    public void setAllowDownload(String allowDownload) {
        this.allowDownload = allowDownload;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUrls() {
        return urls;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }

    public List<MFileInfo> getVideoFiles() {
        return videoFiles;
    }

    public void setVideoFiles(List<MFileInfo> videoFiles) {
        this.videoFiles = videoFiles;
    }

    public List<MFileInfo> getAttchFiles() {
        return attchFiles;
    }

    public void setAttchFiles(List<MFileInfo> attchFiles) {
        this.attchFiles = attchFiles;
    }
}
