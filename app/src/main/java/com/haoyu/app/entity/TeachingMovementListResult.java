package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 创建日期：2017/1/11 on 10:56
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class TeachingMovementListResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private MovementResponseData responseData;
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

    public MovementResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(MovementResponseData responseData) {
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

    public class MovementResponseData {
        /*mMovements	活动List	List	Y
        paginator	分页信息	Object	Y	paginator详见公共对象
        */
        @Expose
        @SerializedName("mMovements")
        private List<TeachingMovementEntity> mMovements;
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }

        public List<TeachingMovementEntity> getmMovements() {
            return mMovements;
        }

        public void setmMovements(List<TeachingMovementEntity> mMovements) {
            this.mMovements = mMovements;
        }
    }
}
