package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by acer1 on 2017/1/7.
 */
public class MEvaluateSubmission {
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
    @Expose
    @SerializedName("evaluateRelationId")
    private String evaluateRelationId;
    @Expose
    @SerializedName("mEvaluateItemSubmissions")
    private List<MEvaluateItemSubmissions> mEvaluateItemSubmissions;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
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

    public List<MEvaluateItemSubmissions> getmEvaluateItemSubmissions() {
        return mEvaluateItemSubmissions;
    }

    public void setmEvaluateItemSubmissions(List<MEvaluateItemSubmissions> mEvaluateItemSubmissions) {
        this.mEvaluateItemSubmissions = mEvaluateItemSubmissions;
    }

    public void setEvaluateRelationId(String evaluateRelationId) {
        this.evaluateRelationId = evaluateRelationId;
    }

    public String getEvaluateRelationId() {
        return this.evaluateRelationId;
    }

    public void setMEvaluateItemSubmissions(List<MEvaluateItemSubmissions> mEvaluateItemSubmissions) {
        this.mEvaluateItemSubmissions = mEvaluateItemSubmissions;
    }

    public List<MEvaluateItemSubmissions> getMEvaluateItemSubmissions() {
        return this.mEvaluateItemSubmissions;
    }

}