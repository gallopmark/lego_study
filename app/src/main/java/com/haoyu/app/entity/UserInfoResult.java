package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2017/2/27 on 13:53
 * 描述: 用户信息结果集
 * 作者:马飞奔 Administrator
 */
public class UserInfoResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private MobileUser responseData;
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

    public MobileUser getResponseData() {
        return responseData;
    }

    public void setResponseData(MobileUser responseData) {
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
