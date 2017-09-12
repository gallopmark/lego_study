package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 回答列表结果集
 */
public class FAQsAnswerListResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private AnswerListResponseData responseData;
    @Expose
    @SerializedName("responseMsg")
    private String responseMsg;
    @Expose
    @SerializedName("success")
    private Boolean success;

    public String getResponseCode() {
        return responseCode;
    }

    public AnswerListResponseData getResponseData() {
        return responseData;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseData(AnswerListResponseData responseData) {
        this.responseData = responseData;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public class AnswerDetailEntity {
        @Expose
        @SerializedName("content")
        private String content;
        @Expose
        @SerializedName("createTime")
        private Long createTime;
        @Expose
        @SerializedName("creator")
        private MobileUser creator;
        @Expose
        @SerializedName("faqAnswers")
        private List<FAQsAnswerEntity> faqAnswers = new ArrayList<FAQsAnswerEntity>();
        @Expose
        @SerializedName("id")
        private String id;

        public AnswerDetailEntity() {
        }

        public String getContent() {
            return this.content;
        }

        public Long getCreateTime() {
            return this.createTime;
        }

        public MobileUser getCreator() {
            return this.creator;
        }

        public List<FAQsAnswerEntity> getFaqAnswers() {
            return this.faqAnswers;
        }

        public String getId() {
            return this.id;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setCreateTime(Long createTime) {
            this.createTime = createTime;
        }

        public void setCreator(MobileUser creator) {
            this.creator = creator;
        }

        public void setFaqAnswers(List<FAQsAnswerEntity> faqAnswers) {
            this.faqAnswers = faqAnswers;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public class AnswerListResponseData implements Serializable {
        @Expose
        @SerializedName("answers")
        private List<FAQsAnswerEntity> answers = new ArrayList<>();
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<FAQsAnswerEntity> getAnswers() {
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
