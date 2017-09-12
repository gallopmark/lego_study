package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2016/12/22 on 15:22
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CourseActivityListResult {
    @SerializedName("responseCode")
    @Expose
    private String responseCode;
    @SerializedName("responseData")
    @Expose
    private List<CourseSectionActivity> responseData = new ArrayList<CourseSectionActivity>();
    @SerializedName("responseMsg")
    @Expose
    private String responseMsg;
    @SerializedName("success")
    @Expose
    private Boolean success;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public List<CourseSectionActivity> getResponseData() {
        return responseData;
    }

    public void setResponseData(List<CourseSectionActivity> responseData) {
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
