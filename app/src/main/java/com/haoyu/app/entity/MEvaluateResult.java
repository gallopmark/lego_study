package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by acer1 on 2017/2/22.
 * 评课结果返回建议
 */
public class MEvaluateResult {
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
        @SerializedName("mEvaluateSubmissions")
        private List<MEvaluateEntity> mEvaluateSubmissions;
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<MEvaluateEntity> getmEvaluateSubmissions() {
            return mEvaluateSubmissions;
        }

        public void setmEvaluateSubmissions(List<MEvaluateEntity> mEvaluateSubmissions) {
            this.mEvaluateSubmissions = mEvaluateSubmissions;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }

}
