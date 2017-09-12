package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2017/1/4 on 10:46
 * 描述: 工作坊研修情况结果集
 * 作者:马飞奔 Administrator
 */
public class WorkShopResearchResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private WorkShopResearchResponseData responseData;
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

    public WorkShopResearchResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(WorkShopResearchResponseData responseData) {
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

    public class WorkShopResearchResponseData{
        @Expose
        @SerializedName("mWorkshopUser")
        private WorkShopMobileUser mWorkshopUser;

        public WorkShopMobileUser getmWorkshopUser() {
            return mWorkshopUser;
        }

        public void setmWorkshopUser(WorkShopMobileUser mWorkshopUser) {
            this.mWorkshopUser = mWorkshopUser;
        }
    }
}
