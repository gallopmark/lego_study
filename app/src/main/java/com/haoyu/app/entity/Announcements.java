package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Announcements {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private ResponseData responseData;
    @Expose
    @SerializedName("responseMsg")
    private String responseMsg;
    @Expose
    @SerializedName("success")
    private Boolean success;

    public String getResponseCode() {
        return this.responseCode;
    }

    public ResponseData getResponseData() {
        return this.responseData;
    }

    public String getResponseMsg() {
        return this.responseMsg;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseData(ResponseData responseData) {
        this.responseData = responseData;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public class ResponseData implements Serializable {
        @Expose
        @SerializedName("announcements")
        private List<Announcement> announcements = new ArrayList<>();
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<Announcement> getAnnouncements() {
            return announcements;
        }

        public void setAnnouncements(List<Announcement> announcements) {
            this.announcements = announcements;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
