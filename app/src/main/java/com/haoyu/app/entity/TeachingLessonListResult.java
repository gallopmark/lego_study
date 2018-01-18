package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/11 on 19:09
 * 描述:教研创课列表
 * 作者:马飞奔 Administrator
 */
public class TeachingLessonListResult extends BaseResponseResult<TeachingLessonListResult.MData> {

    public class MData {
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;
        @Expose
        @SerializedName("mLessons")
        private List<TeachingLessonEntity> mLessons = new ArrayList<>();

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }

        public List<TeachingLessonEntity> getmLessons() {
            if (mLessons == null) {
                return new ArrayList<>();
            }
            return mLessons;
        }

        public void setmLessons(List<TeachingLessonEntity> mLessons) {
            this.mLessons = mLessons;
        }
    }

}
