package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by acer1 on 2017/2/22.
 * 听课评课评价及总结建议
 */
public class MEvaluateEntity {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("comment")
    private String comment;
    @Expose
    @SerializedName("creator")
    private MobileUser creator;
    @Expose
    @SerializedName("createTime")
    private long createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public MobileUser getCreator() {
        return creator;
    }

    public void setCreator(MobileUser creator) {
        this.creator = creator;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
