package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2017/3/21 on 15:29
 * 描述:单位信息
 * 作者:马飞奔 Administrator
 */
public class MDepartment {
    /**
     * province	省编码	String	N
     * city	市编码	String	N
     * counties	区编码	String	N
     */
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("deptName")
    private String deptName;
    @Expose
    @SerializedName("province")
    private String province;
    @Expose
    @SerializedName("city")
    private String city;
    @Expose
    @SerializedName("counties")
    private String counties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounties() {
        return counties;
    }

    public void setCounties(String counties) {
        this.counties = counties;
    }
}
