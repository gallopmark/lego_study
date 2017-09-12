package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by acer1 on 2017/1/6.
 */
public class ReceiveList {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("responseTime")
    private Long responseTime;
    @Expose
    @SerializedName("score")
    private double score;
    @Expose
    @SerializedName("state")
    private String state;
    @Expose
    @SerializedName("expiredDays")
    private int expiredDays;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }


    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return this.state;
    }

    public void setExpiredDays(int expiredDays) {
        this.expiredDays = expiredDays;
    }

    public int getExpiredDays() {
        return this.expiredDays;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }
}