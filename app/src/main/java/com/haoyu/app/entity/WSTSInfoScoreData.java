package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/12/6.
 * 描述:工作坊听课评课评价项得分明细
 * 作者:xiaoma
 */

public class WSTSInfoScoreData {
    @Expose
    @SerializedName("totalSubmission")
    private int totalSubmission;
    @Expose
    @SerializedName("scoreDetail")
    private List<Double> scoreDetail;

    public int getTotalSubmission() {
        return totalSubmission;
    }

    public void setTotalSubmission(int totalSubmission) {
        this.totalSubmission = totalSubmission;
    }

    public List<Double> getScoreDetail() {
        if (scoreDetail == null) {
            return new ArrayList<>();
        }
        return scoreDetail;
    }

    public void setScoreDetail(List<Double> scoreDetail) {
        this.scoreDetail = scoreDetail;
    }
}
