package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/6/9 on 18:04
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CourseLearnData {
    @Expose
    @SerializedName("mCourse")
    private CourseMobileEntity mCourse;
    @Expose
    @SerializedName("mActivities")
    private List<CourseSectionActivity> mActivities = new ArrayList<>();

    public CourseMobileEntity getmCourse() {
        return mCourse;
    }

    public void setmCourse(CourseMobileEntity mCourse) {
        this.mCourse = mCourse;
    }

    public List<CourseSectionActivity> getmActivities() {
        return mActivities;
    }

    public void setmActivities(List<CourseSectionActivity> mActivities) {
        this.mActivities = mActivities;
    }
}
