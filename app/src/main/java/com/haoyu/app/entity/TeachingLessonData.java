package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/10/25 on 17:50
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class TeachingLessonData implements Serializable {
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
