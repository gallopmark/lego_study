package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2016/12/12 on 17:05
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AttitudeUserMobileEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Expose
    @SerializedName("attitude")
    private String attitude;
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("participateNum")
    private Integer participateNum;
    @Expose
    @SerializedName("relation")
    private Relation relation;

    public String getAttitude() {
        return attitude;
    }

    public void setAttitude(String attitude) {
        this.attitude = attitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getParticipateNum() {
        return participateNum;
    }

    public void setParticipateNum(Integer participateNum) {
        this.participateNum = participateNum;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }
}
