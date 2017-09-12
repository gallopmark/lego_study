package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * 问答列表结果集
 */
public class FAQsListResult implements Serializable {
    private static final long serialVersionUID = 1L;
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private QuestionResponseData responseData;
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

    public QuestionResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(QuestionResponseData responseData) {
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

    public static class QuestionResponseData implements Serializable {
        @SerializedName("total_count")
        @Expose
        private int totalCount;
        @SerializedName("questions")
        @Expose
        private List<FAQsEntity> questions = null;
        @SerializedName("paginator")
        @Expose
        private Paginator paginator;

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public List<FAQsEntity> getQuestions() {
            return questions;
        }

        public void setQuestions(List<FAQsEntity> questions) {
            this.questions = questions;
        }
    }
}