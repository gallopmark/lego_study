package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReplyListResult extends BaseResponseResult<ReplyListResult.MData> {

    public class MData implements Serializable {
        @Expose
        @SerializedName("mDiscussionPosts")
        private List<ReplyEntity> mDiscussionPosts = new ArrayList<>();
        @SerializedName("paginator")
        @Expose
        private Paginator paginator;

        public List<ReplyEntity> getmDiscussionPosts() {
            if (mDiscussionPosts == null) {
                return new ArrayList<>();
            }
            return mDiscussionPosts;
        }

        public void setmDiscussionPosts(List<ReplyEntity> mDiscussionPosts) {
            this.mDiscussionPosts = mDiscussionPosts;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}