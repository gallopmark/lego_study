package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DictEntryMobileEntity implements Serializable {
    @SerializedName("textBookValue")
    @Expose
    private String textBookValue;
    @SerializedName("textBookName")
    @Expose
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj.getClass() == DictEntryMobileEntity.class) {
            return ((DictEntryMobileEntity) obj).textBookValue.equals(this.textBookValue);
        }
        return false;
    }
}
