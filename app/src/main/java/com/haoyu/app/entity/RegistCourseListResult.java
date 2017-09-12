package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/6 on 16:35
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class RegistCourseListResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private RegistCourseResponseData responseData;
    @Expose
    @SerializedName("responseMsg")
    private String responseMsg;
    @Expose
    @SerializedName("success")
    private Boolean success;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public RegistCourseResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(RegistCourseResponseData responseData) {
        this.responseData = responseData;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public class RegistCourseResponseData implements Serializable{
        @Expose
        @SerializedName("mCourses")
        List<CourseMobileEntity> mCourses = new ArrayList<>();
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<CourseMobileEntity> getmCourses() {
            return mCourses;
        }

        public void setmCourses(List<CourseMobileEntity> mCourses) {
            this.mCourses = mCourses;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
