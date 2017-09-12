package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 讨论列表结果集
 */
public class DiscussListResult implements Serializable {
    private static final long serialVersionUID = 1L;
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private DiscussionResponseData responseData;
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

    public DiscussionResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(DiscussionResponseData responseData) {
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

    public class DiscussionResponseData implements Serializable {
        @Expose
        @SerializedName("mDiscussions")
        private List<DiscussEntity> mDiscussions = new ArrayList<>();
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;
        @Expose
        @SerializedName("mLessons")
        private List<DiscussEntity> mLessons = new ArrayList<>();
        @Expose
        @SerializedName("mDiscussionPosts")
        private List<DiscussEntity> mDiscussionPosts = new ArrayList<>();
        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }

        public List<DiscussEntity> getmDiscussions() {
            return mDiscussions;
        }

        public void setmDiscussions(List<DiscussEntity> mDiscussions) {
            this.mDiscussions = mDiscussions;
        }

        public List<DiscussEntity> getmLessons() {
            return mLessons;
        }

        public void setmLessons(List<DiscussEntity> mLessons) {
            this.mLessons = mLessons;
        }

        public List<DiscussEntity> getmDiscussionPosts() {
            return mDiscussionPosts;
        }

        public void setmDiscussionPosts(List<DiscussEntity> mDiscussionPosts) {
            this.mDiscussionPosts = mDiscussionPosts;
        }
    }
}