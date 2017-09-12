package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2017/5/24 on 9:35
 * 描述:培训报名信息
 * 作者:马飞奔 Administrator
 * id	培训报名id	String	Y
 mUser
 chooseCourseState	选课提交状态	String	Y	submitted:已提交
 其他值为未提交

 */
public class MTrainRegister {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("chooseCourseState")
    private String chooseCourseState;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChooseCourseState() {
        return chooseCourseState;
    }

    public void setChooseCourseState(String chooseCourseState) {
        this.chooseCourseState = chooseCourseState;
    }
}
