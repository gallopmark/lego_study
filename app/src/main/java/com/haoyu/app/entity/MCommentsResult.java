package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;

import java.io.Serializable;
import java.util.List;

/**
 * Created by acer1 on 2017/1/11.
 */
public class MCommentsResult implements Serializable {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseMsg")
    private String responseMsg;
    @Expose
    @SerializedName("responseData")
    private CommunicationResponseData responseData;

    private boolean success;

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseCode() {
        return this.responseCode;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public String getResponseMsg() {
        return this.responseMsg;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean getSuccess() {
        return this.success;
    }

    public CommunicationResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(CommunicationResponseData responseData) {
        this.responseData = responseData;
    }

    public boolean isSuccess() {
        return success;
    }

    public class CommunicationResponseData {
        @Expose
        @SerializedName("mComments")
        private List<MobileUser> mComments;

        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public void setMComments(List<MobileUser> mComments) {
            this.mComments = mComments;
        }

        public List<MobileUser> getMComments() {
            return this.mComments;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }

        public Paginator getPaginator() {
            return this.paginator;
        }
    }

}