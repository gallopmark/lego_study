package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by acer1 on 2017/3/21.
 * 问卷调查查看问答答案
 */
public class SurveyQuestionResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseMsg")
    private String responseMsg;
    @Expose
    @SerializedName("responseData")
    private SurveyQuestionResponse responseData;
    @Expose
    @SerializedName("success")
    private boolean success;

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

    public SurveyQuestionResponse getResponseData() {
        return responseData;
    }

    public void setResponseData(SurveyQuestionResponse responseData) {
        this.responseData = responseData;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public class SurveyQuestionResponse {
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
