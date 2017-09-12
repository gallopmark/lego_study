package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/1/4 on 10:25
 * 描述: 工作坊简介结果集
 * 作者:马飞奔 Administrator
 */
public class WorkShopDetailResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private WorkShopDetailResponseData responseData;
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

    public WorkShopDetailResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(WorkShopDetailResponseData responseData) {
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

    public class WorkShopDetailResponseData implements Serializable{
        @Expose
        @SerializedName("mWorkshop")
        private WorkShopMobileEntity mWorkshop;
        @Expose
        @SerializedName("mFileInfo")
        private MFileInfo mFileInfo;

        public WorkShopMobileEntity getmWorkshop() {
            return mWorkshop;
        }

        public void setmWorkshop(WorkShopMobileEntity mWorkshop) {
            this.mWorkshop = mWorkshop;
        }

        public MFileInfo getmFileInfo() {
            return mFileInfo;
        }

        public void setmFileInfo(MFileInfo mFileInfo) {
            this.mFileInfo = mFileInfo;
        }
    }
}
