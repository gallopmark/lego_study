package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 创建日期：2017/1/13 on 14:11
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CommentListResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private CommnetListData responseData;
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

    public CommnetListData getResponseData() {
        return responseData;
    }

    public void setResponseData(CommnetListData responseData) {
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

    public class CommnetListData{
        @Expose
        @SerializedName("mComments")
        private List<CommentEntity> mComments;
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<CommentEntity> getmComments() {
            return mComments;
        }

        public void setmComments(List<CommentEntity> mComments) {
            this.mComments = mComments;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
