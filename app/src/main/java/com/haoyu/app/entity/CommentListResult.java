package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/13 on 14:11
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CommentListResult extends BaseResponseResult<CommentListResult.MData> {

    public class MData {
        @Expose
        @SerializedName("mComments")
        private List<CommentEntity> mComments;
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;
        @Expose
        @SerializedName("id")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<CommentEntity> getmComments() {
            if (mComments == null) {
                return new ArrayList<>();
            }
            return mComments;
        }

        public void setmComments(List<CommentEntity> mComments) {
            this.mComments = mComments;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
