package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 问答列表结果集
 */
public class FAQsListResult extends BaseResponseResult<FAQsListResult.MData> {

    public static class MData implements Serializable {
        @SerializedName("total_count")
        @Expose
        private int totalCount;
        @SerializedName("questions")
        @Expose
        private List<FAQsEntity> questions;
        @SerializedName("paginator")
        @Expose
        private Paginator paginator;

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public List<FAQsEntity> getQuestions() {
            if (questions == null) {
                return new ArrayList<>();
            }
            return questions;
        }

        public void setQuestions(List<FAQsEntity> questions) {
            this.questions = questions;
        }
    }
}