package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2017/12/5.
 * 描述:听课评课-评分项设置
 * 作者:xiaoma
 */

public class MEvaluateItem {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("content")
    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
