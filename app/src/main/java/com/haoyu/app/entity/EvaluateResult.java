package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by acer1 on 2017/2/22.
 * 填写听课评课
 */
public class EvaluateResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseMsg")
    private String responseMsg;
    @Expose
    @SerializedName("responseData")
    private EvaluateResponse responseData;
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

    public EvaluateResponse getResponseData() {
        return responseData;
    }

    public void setResponseData(EvaluateResponse responseData) {
        this.responseData = responseData;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public class EvaluateResponse {
        @Expose
        @SerializedName("submissionId")
        private String submissionId;
        @Expose
        @SerializedName("submissionRelationId")
        private String submissionRelationId;
        @Expose
        @SerializedName("mEvaluateItems")
        private List<MEvaluateItemSubmissions> mEvaluateItems;

        public String getSubmissionId() {
            return submissionId;
        }

        public void setSubmissionId(String submissionId) {
            this.submissionId = submissionId;
        }

        public String getSubmissionRelationId() {
            return submissionRelationId;
        }

        public void setSubmissionRelationId(String submissionRelationId) {
            this.submissionRelationId = submissionRelationId;
        }

        public List<MEvaluateItemSubmissions> getmEvaluateItems() {
            return mEvaluateItems;
        }

        public void setmEvaluateItems(List<MEvaluateItemSubmissions> mEvaluateItems) {
            this.mEvaluateItems = mEvaluateItems;
        }
    }
}
