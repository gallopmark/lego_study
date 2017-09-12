package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2016/12/30 on 14:44
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MTextBookEntryEntity {
    @Expose
    @SerializedName("textBookValue")
    private String textBookValue;
    @Expose
    @SerializedName("textBookName")
    private String textBookName;

    public String getTextBookValue() {
        return textBookValue;
    }

    public void setTextBookValue(String textBookValue) {
        this.textBookValue = textBookValue;
    }

    public String getTextBookName() {
        return textBookName;
    }

    public void setTextBookName(String textBookName) {
        this.textBookName = textBookName;
    }
}
