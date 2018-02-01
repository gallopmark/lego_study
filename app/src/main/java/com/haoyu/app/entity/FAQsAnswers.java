package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 回答列表结果集
 */
public class FAQsAnswers extends BaseResponseResult<FAQsAnswers.MData> {
    public class MData {
        @Expose
        @SerializedName("answers")
        private List<FAQsAnswerEntity> answers;
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<FAQsAnswerEntity> getAnswers() {
            if (answers == null) {
                return new ArrayList<>();
            }
            return answers;
        }

        public void setAnswers(List<FAQsAnswerEntity> answers) {
            this.answers = answers;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
