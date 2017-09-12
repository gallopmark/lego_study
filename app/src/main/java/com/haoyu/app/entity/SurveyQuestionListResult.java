package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2016/12/24 on 15:06
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class SurveyQuestionListResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private List<SurveyAnswer> responseData = new ArrayList<>();
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

    public List<SurveyAnswer> getResponseData() {
        return responseData;
    }

    public void setResponseData(List<SurveyAnswer> responseData) {
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
