package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/3/23 on 17:58
 * 描述:区域实体类
 * regionsCode	编码	String	Y
 * regionsName	名称	String	Y
 * 作者:马飞奔 Administrator
 */
public class RegionModule implements Serializable{
    @Expose
    @SerializedName("regionsCode")
    private String regionsCode;
    @Expose
    @SerializedName("regionsName")
    private String regionsName;

    public String getRegionsCode() {
        return regionsCode;
    }

    public void setRegionsCode(String regionsCode) {
        this.regionsCode = regionsCode;
    }

    public String getRegionsName() {
        return regionsName;
    }

    public void setRegionsName(String regionsName) {
        this.regionsName = regionsName;
    }
}
