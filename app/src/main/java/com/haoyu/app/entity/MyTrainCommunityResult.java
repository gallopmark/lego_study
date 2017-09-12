package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/1/7 on 11:34
 * 描述: 研修社区培训结果
 * 作者:马飞奔 Administrator
 * score	已获社区积分	Int	Y
 state	社区成绩	String	Y
 mCommunityRelation	社区关联对象	Map

 */
public class MyTrainCommunityResult implements Serializable{
    @Expose
    @SerializedName("score")
    private int score;
    @Expose
    @SerializedName("state")
    private String state;
    @Expose
    @SerializedName("mCommunityRelation")
    private CommunityRelation mCommunityRelation;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public CommunityRelation getmCommunityRelation() {
        return mCommunityRelation;
    }

    public void setmCommunityRelation(CommunityRelation mCommunityRelation) {
        this.mCommunityRelation = mCommunityRelation;
    }
}
