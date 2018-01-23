package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/10/25 on 17:50
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CmtsLessonData implements Serializable {
    @Expose
    @SerializedName("mLesson")
    private CmtsLessonEntity mLesson;
    @Expose
    @SerializedName("mLessonAttribute")
    private CmtsLessonAttribute mLessonAttribute;

    public CmtsLessonEntity getmLesson() {
        return mLesson;
    }

    public void setmLesson(CmtsLessonEntity mLesson) {
        this.mLesson = mLesson;
    }

    public CmtsLessonAttribute getmLessonAttribute() {
        return mLessonAttribute;
    }

    public void setmLessonAttribute(CmtsLessonAttribute mLessonAttribute) {
        this.mLessonAttribute = mLessonAttribute;
    }
}
