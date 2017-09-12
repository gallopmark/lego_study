package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/3 on 14:13
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class WorkShopSingleResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private WorkShopSingleResponseData responseData;
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

    public WorkShopSingleResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(WorkShopSingleResponseData responseData) {
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

    public class WorkShopSingleResponseData {
        /**
         * mWorkshop	工作坊	MWorkshop	Y
         mWorkshopUser	工作坊用户	MWorkshopUser	Y
         mWorkshopSections	阶段列表	List	Y
         */
        @Expose
        @SerializedName("mWorkshop")
        private WorkShopMobileEntity mWorkshop;
        @Expose
        @SerializedName("mWorkshopUser")
        private WorkShopMobileUser mWorkshopUser;
        @Expose
        @SerializedName("mWorkshopSections")
        private List<MWorkshopSection> mWorkshopSections = new ArrayList<>();

        public WorkShopMobileEntity getmWorkshop() {
            return mWorkshop;
        }

        public void setmWorkshop(WorkShopMobileEntity mWorkshop) {
            this.mWorkshop = mWorkshop;
        }

        public WorkShopMobileUser getmWorkshopUser() {
            return mWorkshopUser;
        }

        public void setmWorkshopUser(WorkShopMobileUser mWorkshopUser) {
            this.mWorkshopUser = mWorkshopUser;
        }

        public List<MWorkshopSection> getmWorkshopSections() {
            return mWorkshopSections;
        }

        public void setmWorkshopSections(List<MWorkshopSection> mWorkshopSections) {
            this.mWorkshopSections = mWorkshopSections;
        }
    }
}
