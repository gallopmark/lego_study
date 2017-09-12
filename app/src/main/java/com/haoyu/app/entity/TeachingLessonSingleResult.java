package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2017/1/11 on 19:22
 * 描述: 单个创课结果集
 * 作者:马飞奔 Administrator
 */
public class TeachingLessonSingleResult {
    @Expose
    @SerializedName("responseCode")
    private String responseCode;
    @Expose
    @SerializedName("responseData")
    private TeachingLessonSingleData responseData;
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

    public TeachingLessonSingleData getResponseData() {
        return responseData;
    }

    public void setResponseData(TeachingLessonSingleData responseData) {
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

    public class TeachingLessonSingleData {
        @Expose
        @SerializedName("mLesson")
        private TeachingLessonEntity mLesson;
        @Expose
        @SerializedName("mLessonAttribute")
        private LessonAttribute mLessonAttribute;

        public TeachingLessonEntity getmLesson() {
            return mLesson;
        }

        public void setmLesson(TeachingLessonEntity mLesson) {
            this.mLesson = mLesson;
        }

        public LessonAttribute getmLessonAttribute() {
            return mLessonAttribute;
        }

        public void setmLessonAttribute(LessonAttribute mLessonAttribute) {
            this.mLessonAttribute = mLessonAttribute;
        }
    }

    public class LessonAttribute {
        /**
         * id	ID
         mStage	学段
         mSubject	学科
         */
        @Expose
        @SerializedName("id")
        private String id;
        @Expose
        @SerializedName("mStage")
        private MTextBookEntryEntity mStage;
        @Expose
        @SerializedName("mSubject")
        private MTextBookEntryEntity mSubject;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public MTextBookEntryEntity getmStage() {
            return mStage;
        }

        public void setmStage(MTextBookEntryEntity mStage) {
            this.mStage = mStage;
        }

        public MTextBookEntryEntity getmSubject() {
            return mSubject;
        }

        public void setmSubject(MTextBookEntryEntity mSubject) {
            this.mSubject = mSubject;
        }
    }
}
