package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/3/15 on 14:27
 * 描述: 问卷调查问答题答案对象
 * 作者:马飞奔 Administrator
 */
public class SurveyAnswerSubmission implements Serializable{
    @Expose
    @SerializedName("response")
    private String response;
    @Expose
    @SerializedName("user")
    private MobileUser user;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public MobileUser getUser() {
        return user;
    }

    public void setUser(MobileUser user) {
        this.user = user;
    }
}
