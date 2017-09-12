package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/1/1.
 */
public class DetailMap implements Serializable {
    @Expose
    @SerializedName("mark_num")
    private int mark_num;
    @Expose
    @SerializedName("mark_score")
    private double mark_score;
    @Expose
    @SerializedName("response_score")
    private double response_score;
    @Expose
    @SerializedName("complete_pct")
    private double complete_pct;

    public int getMark_num() {
        return mark_num;
    }

    public void setMark_num(int mark_num) {
        this.mark_num = mark_num;
    }

    public double getMark_score() {
        return mark_score;
    }

    public void setMark_score(double mark_score) {
        this.mark_score = mark_score;
    }

    public double getResponse_score() {
        return response_score;
    }

    public void setResponse_score(double response_score) {
        this.response_score = response_score;
    }

    public double getComplete_pct() {
        return complete_pct;
    }

    public void setComplete_pct(double complete_pct) {
        this.complete_pct = complete_pct;
    }
}
