package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/11 on 19:09
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class TeachingLessonListResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private TeachingLessonData responseData;
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

    public TeachingLessonData getResponseData() {
        return responseData;
    }

    public void setResponseData(TeachingLessonData responseData) {
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

    public class TeachingLessonData {
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;
        @Expose
        @SerializedName("mLessons")
        private List<TeachingLessonEntity> mLessons = new ArrayList<>();

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }

        public List<TeachingLessonEntity> getmLessons() {
            return mLessons;
        }

        public void setmLessons(List<TeachingLessonEntity> mLessons) {
            this.mLessons = mLessons;
        }
    }

}
