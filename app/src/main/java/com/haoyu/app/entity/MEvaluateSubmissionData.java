package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MEvaluateSubmissionData {
    @Expose
    @SerializedName("mEvaluateSubmissions")
    private List<MEvaluateSubmission> mEvaluateSubmissions;
    @Expose
    @SerializedName("paginator")
    private Paginator paginator;

    public List<MEvaluateSubmission> getmEvaluateSubmissions() {
        if (mEvaluateSubmissions == null)
            return new ArrayList<>();
        return mEvaluateSubmissions;
    }

    public void setmEvaluateSubmissions(List<MEvaluateSubmission> mEvaluateSubmissions) {
        this.mEvaluateSubmissions = mEvaluateSubmissions;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }
}