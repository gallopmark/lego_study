package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2017/2/21 on 13:50
 * 描述:扫一扫返回的结果集
 * 作者:马飞奔 Administrator
 */
public class CaptureResult {
    @Expose
    @SerializedName("qtId")
    private String qtId;
    @Expose
    @SerializedName("service")
    private String service;

    public String getQtId() {
        return qtId;
    }

    public void setQtId(String qtId) {
        this.qtId = qtId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
