package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 讨论列表结果集
 */
public class DiscussListResult extends BaseResponseResult<DiscussListResult.MData> {

    public class MData implements Serializable {
        @Expose
        @SerializedName("mDiscussions")
        private List<DiscussEntity> mDiscussions;
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
            if (mLessons == null) {
                return new ArrayList<>();
            }
            return mLessons;
        }

        public void setmLessons(List<DiscussEntity> mLessons) {
            this.mLessons = mLessons;
        }

        public List<DiscussEntity> getmDiscussionPosts() {
            if (mDiscussions == null) {
                return new ArrayList<>();
            }
            return mDiscussionPosts;
        }

        public void setmDiscussionPosts(List<DiscussEntity> mDiscussionPosts) {
            this.mDiscussionPosts = mDiscussionPosts;
        }
    }
}