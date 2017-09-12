package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by acer1 on 2017/2/23.
 * 听课评课的得分明细
 * 总提交人数
 */
public class ScoreDetailResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private ScoreResponse responseData;
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

    public ScoreResponse getResponseData() {
        return responseData;
    }

    public void setResponseData(ScoreResponse responseData) {
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

    public class ScoreResponse {
        @Expose
        @SerializedName("totalSubmission")
        private int totalSubmission;
        @Expose
        @SerializedName("scoreDetail")
        private List<Double> scoreDetail;

        public int getTotalSubmission() {
            return totalSubmission;
        }

        public void setTotalSubmission(int totalSubmission) {
            this.totalSubmission = totalSubmission;
        }

        public List<Double> getScoreDetail() {
            return scoreDetail;
        }

        public void setScoreDetail(List<Double> scoreDetail) {
            this.scoreDetail = scoreDetail;
        }
    }
}
