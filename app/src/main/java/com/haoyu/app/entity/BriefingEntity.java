package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * id	研修简报ID
 * title	标题
 * content	内容
 * createTime	创建时间
 * <p/>
 * 作者:马飞奔 Administrator
 */
public class BriefingEntity implements Serializable {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("content")
    private String content;
    @Expose
    @SerializedName("createTime")
    private long createTime;
    @Expose
    @SerializedName("hadView")
    private boolean hadView;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public boolean isHadView() {
        return hadView;
    }

    public void setHadView(boolean hadView) {
        this.hadView = hadView;
    }
}
