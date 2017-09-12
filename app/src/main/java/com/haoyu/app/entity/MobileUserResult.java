package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by acer1 on 2017/2/16.
 */
public class MobileUserResult {

    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseMsg")
    private String responseMsg;
    @Expose
    @SerializedName("responseData")
    private MobileUserResponseData responseData;

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

    public MobileUserResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(MobileUserResponseData responseData) {
        this.responseData = responseData;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Expose
    @SerializedName("success")
    private boolean success;

    public class MobileUserResponseData {
        @Expose
        @SerializedName("mUsers")
        private List<MobileUser> mUsers;
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<MobileUser> getmUsers() {
            return mUsers;
        }

        public void setmUsers(List<MobileUser> mUsers) {
            this.mUsers = mUsers;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }

}
