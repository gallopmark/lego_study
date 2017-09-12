package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by acer1 on 2017/2/22.
 * 听课评课结果评价内容项
 */
public class EvaluateItemResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseMsg")
    private String responseMsg;
    @Expose
    @SerializedName("responseData")
    private List<EvaluateItemResponse> responseData;
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

    public List<EvaluateItemResponse> getResponseData() {
        return responseData;
    }

    public void setResponseData(List<EvaluateItemResponse> responseData) {
        this.responseData = responseData;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public class EvaluateItemResponse {
        @Expose
        @SerializedName("id")
        private String id;

        @Expose
        @SerializedName("content")
        private String content;
        @Expose
        @SerializedName("avgScore")
        private double avgScore;
        @Expose
        @SerializedName("creator")
        private MobileUser creator;

        public MobileUser getCreator() {
            return creator;
        }

        public void setCreator(MobileUser creator) {
            this.creator = creator;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public double getAvgScore() {
            return avgScore;
        }

        public void setAvgScore(double avgScore) {
            this.avgScore = avgScore;
        }
    }
}
