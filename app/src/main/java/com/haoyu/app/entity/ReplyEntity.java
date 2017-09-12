package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReplyEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Expose
    @SerializedName("childPostCount")
    private int childPostCount;
    @Expose
    @SerializedName("content")
    private String content;
    @Expose
    @SerializedName("createTime")
    private long createTime;
    @Expose
    @SerializedName("creator")
    private MobileUser creator;
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("supportNum")
    private int supportNum;
    private List<ReplyEntity> childReplyEntityList = new ArrayList<>();  //自回复list

    public int getChildPostCount() {
        return childPostCount;
    }

    public void setChildPostCount(int childPostCount) {
        this.childPostCount = childPostCount;
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

    public MobileUser getCreator() {
        return creator;
    }

    public void setCreator(MobileUser creator) {
        this.creator = creator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSupportNum() {
        return supportNum;
    }

    public void setSupportNum(int supportNum) {
        this.supportNum = supportNum;
    }

    public List<ReplyEntity> getChildReplyEntityList() {
        return childReplyEntityList;
    }

    public void setChildReplyEntityList(List<ReplyEntity> childReplyEntityList) {
        this.childReplyEntityList = childReplyEntityList;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj.getClass() == ReplyEntity.class) {
            return ((ReplyEntity) obj).id.equals(this.id);
        }
        return false;
    }
}