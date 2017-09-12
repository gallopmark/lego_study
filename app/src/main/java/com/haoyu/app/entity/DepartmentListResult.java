package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/3/24 on 11:07
 * 描述:单位信息列表结果集
 * 作者:马飞奔 Administrator
 */
public class DepartmentListResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseMsg")
    private String responseMsg;
    @Expose
    @SerializedName("responseData")
    private List<MDepartment> responseData = new ArrayList<>();

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public List<MDepartment> getResponseData() {
        return responseData;
    }

    public void setResponseData(List<MDepartment> responseData) {
        this.responseData = responseData;
    }
}
