package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2016/12/24 on 16:46
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CourseProgressResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private CourseProgressEntity responseData;
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

    public CourseProgressEntity getResponseData() {
        return responseData;
    }

    public void setResponseData(CourseProgressEntity responseData) {
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
}
