package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2016/12/23 on 11:03
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class TimePeriod implements Serializable{
    @Expose
    @SerializedName("startTime")
    private long startTime;
    @Expose
    @SerializedName("endTime")
    private long endTime;
    @Expose
    @SerializedName("minutes")
    private long minutes;
    @Expose
    @SerializedName("state")
    private String state;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getMinutes() {
        return minutes;
    }

    public void setMinutes(long minutes) {
        this.minutes = minutes;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
