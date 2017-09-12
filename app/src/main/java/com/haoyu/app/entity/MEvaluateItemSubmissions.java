package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by acer1 on 2017/1/7.
 * 学员互评，评价项
 */
public class MEvaluateItemSubmissions {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("content")
    private String content;
    @Expose
    @SerializedName("score")
    private double score;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
