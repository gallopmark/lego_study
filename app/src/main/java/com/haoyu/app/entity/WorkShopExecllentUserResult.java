package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/4 on 10:39
 * 描述: 工作坊优秀学员结果集
 * 作者:马飞奔 Administrator
 */
public class WorkShopExecllentUserResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private WorkShopExecllentUserResponseData responseData;
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

    public WorkShopExecllentUserResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(WorkShopExecllentUserResponseData responseData) {
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

    public class WorkShopExecllentUserResponseData implements Serializable{
        @Expose
        @SerializedName("mWorkshopUsers")
        private List<WorkShopMobileUser> mWorkshopUsers = new ArrayList<>();
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<WorkShopMobileUser> getmWorkshopUsers() {
            return mWorkshopUsers;
        }

        public void setmWorkshopUsers(List<WorkShopMobileUser> mWorkshopUsers) {
            this.mWorkshopUsers = mWorkshopUsers;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
