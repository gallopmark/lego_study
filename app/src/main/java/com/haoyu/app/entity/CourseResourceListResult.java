package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 创建日期：2017/1/7 on 16:40
 * 描述: 课程资源列表
 * 作者:马飞奔 Administrator
 */
public class CourseResourceListResult {
    @SerializedName("responseCode")
    @Expose
    private String responseCode;
    @SerializedName("responseData")
    @Expose
    private ResourceListResponseData responseData;
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

    public ResourceListResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(ResourceListResponseData responseData) {
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

    public class ResourceListResponseData {
        @SerializedName("resources")
        @Expose
        private List<ResourcesEntity> resources;
        @SerializedName("paginator")
        @Expose
        private Paginator paginator;

        public List<ResourcesEntity> getResources() {
            return resources;
        }

        public void setResources(List<ResourcesEntity> resources) {
            this.resources = resources;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
