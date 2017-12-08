package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CourseDetailData {
    /**
     * noSubmit：未报读
     * submit：待审核
     * pass：审核通过
     * nopass：审核不通过
     */
    @SerializedName("registerState")
    @Expose
    private String registerState;
    @SerializedName("mCourseRegister")
    @Expose
    private MCourseRegister mCourseRegister;
    @SerializedName("teachers")
    @Expose
    private List<MobileUser> teachers;
    @SerializedName("mCourse")
    @Expose
    private CourseMobileEntity mCourse;

    public String getRegisterState() {
        return registerState;
    }

    public void setRegisterState(String registerState) {
        this.registerState = registerState;
    }

    public List<MobileUser> getTeachers() {
        if (teachers == null) {
            return new ArrayList<>();
        }
        return teachers;
    }

    public void setTeachers(List<MobileUser> teachers) {
        this.teachers = teachers;
    }

    public CourseMobileEntity getmCourse() {
        return mCourse;
    }

    public void setmCourse(CourseMobileEntity mCourse) {
        this.mCourse = mCourse;
    }

    public MCourseRegister getmCourseRegister() {
        return mCourseRegister;
    }

    public void setmCourseRegister(MCourseRegister mCourseRegister) {
        this.mCourseRegister = mCourseRegister;
    }
}
