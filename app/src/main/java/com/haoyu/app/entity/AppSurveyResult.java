package com.haoyu.app.entity;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建日期：2016/12/28 on 10:46
 * 描述: 问卷结果
 * 作者:马飞奔 Administrator
 */
public class AppSurveyResult {
    @SerializedName("responseCode")
    @Expose
    private String responseCode;
    @SerializedName("responseData")
    @Expose
    private SurveyResultData responseData;
    @SerializedName("responseMsg")
    @Expose
    private String responseMsg;
    @SerializedName("success")
    @Expose
    private Boolean success;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public SurveyResultData getResponseData() {
        return responseData;
    }

    public void setResponseData(SurveyResultData responseData) {
        this.responseData = responseData;
    }

    public class SurveyResultData implements Serializable {
        @SerializedName("mSurveyQuestions")
        @Expose
        private List<SurveyAnswer> mSurveyQuestions = new ArrayList<>();
        @SerializedName("participateNum")
        @Expose
        private int participateNum;
        @SerializedName("choiceInteractionResults")
        @Expose
        private Object choiceInteractionResults;
        @SerializedName("mySubmission")
        @Expose
        private Object mySubmission;


        public List<SurveyAnswer> getmSurveyQuestions() {
            return mSurveyQuestions;
        }

        public void setmSurveyQuestions(List<SurveyAnswer> mSurveyQuestions) {
            this.mSurveyQuestions = mSurveyQuestions;
        }

        public Map<String, Map<String, Integer>> getChoiceInteractionResults() {
            try {
                Gson gson = new Gson();
                String toJson = gson.toJson(choiceInteractionResults);
                Map<String, Map<String, Integer>> dataMap = gson.fromJson(toJson, new TypeToken<Map<String, Map<String, Integer>>>() {
                }.getType());
                return dataMap;
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                return new HashMap<>();
            }
        }

        public void setChoiceInteractionResults(Object choiceInteractionResults) {
            this.choiceInteractionResults = choiceInteractionResults;
        }

        public Object getMySubmission() {
            return mySubmission;
        }

        public void setMySubmission(Object mySubmission) {
            this.mySubmission = mySubmission;
        }

        public int getParticipateNum() {
            return participateNum;
        }

        public void setParticipateNum(int participateNum) {
            this.participateNum = participateNum;
        }
    }
}
