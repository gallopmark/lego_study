package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/1/7 on 11:30
 * 描述: 工作坊培训结果
 * point	获得积分	Int	Y
 state	工作坊评价	String	Y	excellent:优秀
 qualified:合格
 fail:未达标
 null:未评价
 studyHours	学时	Int	Y
 mWorkshop	工作坊对象	Map	Y

 * 作者:马飞奔 Administrator
 */
public class MyTrainWorkShopResult implements Serializable{
    @Expose
    @SerializedName("point")
    private int point;
    @Expose
    @SerializedName("state")
    private String state;
    @Expose
    @SerializedName("studyHours")
    private int studyHours;
    @Expose
    @SerializedName("mWorkshop")
    private WorkShopMobileEntity mWorkshop;

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getStudyHours() {
        return studyHours;
    }

    public void setStudyHours(int studyHours) {
        this.studyHours = studyHours;
    }

    public WorkShopMobileEntity getmWorkshop() {
        return mWorkshop;
    }

    public void setmWorkshop(WorkShopMobileEntity mWorkshop) {
        this.mWorkshop = mWorkshop;
    }
}
