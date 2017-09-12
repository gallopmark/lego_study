package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/1/7 on 11:27
 * 描述: 课程培训结果
 state	状态	String	Y
 mCourse	课程对象	Map	Y
 * 作者:马飞奔 Administrator
 */
public class MyTrainCourseResult implements Serializable{
    @Expose
    @SerializedName("state")
    private String state;
    @Expose
    @SerializedName("mCourse")
    private CourseMobileEntity mCourse;

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
}
