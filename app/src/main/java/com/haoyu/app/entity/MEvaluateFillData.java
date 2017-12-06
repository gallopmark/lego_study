package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by acer1 on 2017/2/22.
 * 填写听课评课
 */
public class MEvaluateFillData {
    @Expose
    @SerializedName("submissionId")
    private String submissionId;
    @Expose
    @SerializedName("submissionRelationId")
    private String submissionRelationId;
    @Expose
    @SerializedName("mEvaluateItems")
    private List<MEvaluateItem> mEvaluateItems;

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getSubmissionRelationId() {
        return submissionRelationId;
    }

    public void setSubmissionRelationId(String submissionRelationId) {
        this.submissionRelationId = submissionRelationId;
    }

    public List<MEvaluateItem> getmEvaluateItems() {
        if (mEvaluateItems == null)
            return new ArrayList<>();
        return mEvaluateItems;
    }

    public void setmEvaluateItems(List<MEvaluateItem> mEvaluateItems) {
        this.mEvaluateItems = mEvaluateItems;
    }
}
