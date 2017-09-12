package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/1/7 on 9:18
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MyTrainMobileEntity implements Serializable {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("mTrainingTime")
    private TimePeriod mTrainingTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimePeriod getmTrainingTime() {
        return mTrainingTime;
    }

    public void setmTrainingTime(TimePeriod mTrainingTime) {
        this.mTrainingTime = mTrainingTime;
    }
}
