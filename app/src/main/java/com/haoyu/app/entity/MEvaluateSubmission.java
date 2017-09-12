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