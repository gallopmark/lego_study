package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2017/1/7 on 11:37
 * 描述: 社区关联对象
 * score	社区积分	Int	Y
 studyHours	社区学时	Int	Y
 timePeriod	工作坊起止时间	TimePeriod	N

 * 作者:马飞奔 Administrator
 */
public class CommunityRelation {
    @Expose
    @SerializedName("score")
    private int score;
    @Expose
    @SerializedName("studyHours")
    private int studyHours;
    @Expose
    @SerializedName("timePeriod")
    private TimePeriod timePeriod;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getStudyHours() {
        return studyHours;
    }

    public void setStudyHours(int studyHours) {
        this.studyHours = studyHours;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }
}
