package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * 创建日期：2017/1/7 on 11:14
 * 描述: 个人培训信息
 * 作者:马飞奔 Administrator
 */
public class MobileUserTrainInfoResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private TrainInfoResponseData responseData;
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

    public TrainInfoResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(TrainInfoResponseData responseData) {
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

    public class TrainInfoResponseData implements Serializable{
        @Expose
        @SerializedName("trainResult")
        private MyTrainResultEntity trainResult;
        @Expose
        @SerializedName("mCourseRegisters")
        private List<MyTrainCourseResult> mCourseRegisters;
        @Expose
        @SerializedName("mWorkshopUsers")
        private List<MyTrainWorkShopResult> mWorkshopUsers;
        @Expose
        @SerializedName("mCommunityResult")
        private MyTrainCommunityResult mCommunityResult;

        public MyTrainResultEntity getTrainResult() {
            return trainResult;
        }

        public void setTrainResult(MyTrainResultEntity trainResult) {
            this.trainResult = trainResult;
        }

        public List<MyTrainCourseResult> getmCourseRegisters() {
            return mCourseRegisters;
        }

        public void setmCourseRegisters(List<MyTrainCourseResult> mCourseRegisters) {
            this.mCourseRegisters = mCourseRegisters;
        }

        public List<MyTrainWorkShopResult> getmWorkshopUsers() {
            return mWorkshopUsers;
        }

        public void setmWorkshopUsers(List<MyTrainWorkShopResult> mWorkshopUsers) {
            this.mWorkshopUsers = mWorkshopUsers;
        }

        public MyTrainCommunityResult getmCommunityResult() {
            return mCommunityResult;
        }

        public void setmCommunityResult(MyTrainCommunityResult mCommunityResult) {
            this.mCommunityResult = mCommunityResult;
        }
    }

}
