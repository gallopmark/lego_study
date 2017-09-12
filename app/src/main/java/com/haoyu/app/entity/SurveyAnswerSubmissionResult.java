package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/3/15 on 14:25
 * 描述: 问卷调查问答题答案列表结果集
 * 作者:马飞奔 Administrator
 */
public class SurveyAnswerSubmissionResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private SurveyAnswerSubmissionData responseData;
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

    public SurveyAnswerSubmissionData getResponseData() {
        return responseData;
    }

    public void setResponseData(SurveyAnswerSubmissionData responseData) {
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

    public class SurveyAnswerSubmissionData {
        @Expose
        @SerializedName("mSubmissions")
        private List<SurveyAnswerSubmission> mSubmissions = new ArrayList<>();
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<SurveyAnswerSubmission> getmSubmissions() {
            return mSubmissions;
        }

        public void setmSubmissions(List<SurveyAnswerSubmission> mSubmissions) {
            this.mSubmissions = mSubmissions;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
