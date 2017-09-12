package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2017/5/23 on 16:13
 * 描述:选课情况
 * 作者:马飞奔 Administrator
 */
public class MCourseRegister {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("state")
    @Expose
    private String state;
    @Expose
    @SerializedName("mCourse")
    private CourseMobileEntity mCourse;
    @Expose
    @SerializedName("score")
    private double score;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public CourseMobileEntity getmCourse() {
        return mCourse;
    }

    public void setmCourse(CourseMobileEntity mCourse) {
        this.mCourse = mCourse;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj.getClass() == MCourseRegister.class)
            return ((MCourseRegister) obj).id.equals(this.id);
        return false;
    }
}
