package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/13 on 14:09
 * 描述: 评论实体类
 * 作者:马飞奔 Administrator
 * id	ID	String	Y
 * createTime	创建时间	long	Y
 * creator	创建人	MUser	Y
 * content	内容	String	Y
 * childNum	回复数	int	Y
 */
public class CommentEntity implements Serializable {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("createTime")
    private long createTime;
    @Expose
    @SerializedName("creator")
    private MobileUser creator;
    @Expose
    @SerializedName("content")
    private String content;
    @Expose
    @SerializedName("childNum")
    private int childNum;
    @Expose
    @SerializedName("supportNum")
    private int supportNum;

    private List<CommentEntity> childList = new ArrayList<>();  //子评论列表

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getChildNum() {
        return childNum;
    }

    public void setChildNum(int childNum) {
        this.childNum = childNum;
    }

    public int getSupportNum() {
        return supportNum;
    }

    public void setSupportNum(int supportNum) {
        this.supportNum = supportNum;
    }

    public List<CommentEntity> getChildList() {
        return childList;
    }

    public void setChildList(List<CommentEntity> childList) {
        this.childList = childList;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj.getClass() == CommentEntity.class) {
            return ((CommentEntity) obj).id.equals(this.id);
        }
        return false;
    }
}
