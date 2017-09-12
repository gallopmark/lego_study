package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2017/2/16 on 16:50
 * 描述: 创建创课返回的结果集
 * 作者:马飞奔 Administrator
 */
public class TeachingResearchCreateCCResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    TeachingResearchCreateCCData responseData;
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

    public TeachingResearchCreateCCData getResponseData() {
        return responseData;
    }

    public void setResponseData(TeachingResearchCreateCCData responseData) {
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

    public class TeachingResearchCreateCCData {
        @Expose
        @SerializedName("mLesson")
        private TeachingLessonEntity mLesson;
        @Expose
        @SerializedName("mLessonAttribute")
        private TeachingLessonAttribute mLessonAttribute;

        public TeachingLessonEntity getmLesson() {
            return mLesson;
        }

        public void setmLesson(TeachingLessonEntity mLesson) {
            this.mLesson = mLesson;
        }

        public TeachingLessonAttribute getmLessonAttribute() {
            return mLessonAttribute;
        }

        public void setmLessonAttribute(TeachingLessonAttribute mLessonAttribute) {
            this.mLessonAttribute = mLessonAttribute;
        }
    }

    public class TeachingLessonAttribute {
        /**
         * id	ID	String	Y	即创课Id
         * stage	学段	String	Y
         * subject	学科	String	Y
         */
        @Expose
        @SerializedName("id")
        private String id;
        @Expose
        @SerializedName("stage")
        private String stage;
        @Expose
        @SerializedName("subject")
        private String subject;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStage() {
            return stage;
        }

        public void setStage(String stage) {
            this.stage = stage;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }
    }
}
