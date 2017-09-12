package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2016/12/24 on 9:45
 * 描述: 问卷对象
 * 作者:马飞奔 Administrator
 */
public class CourseSurveyEntity implements Serializable {
    @Expose
    @SerializedName("id")
    private String id;
    private int type;  //題目类型
    @Expose
    @SerializedName("title")
    private String title;  //题目标题
    @Expose
    @SerializedName("description")
    private String description;  //描述
    @Expose
    @SerializedName("timePeriod")
    private TimePeriod timePeriod;
    private List<SurveyAnswer> surveyAnswers = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    public List<SurveyAnswer> getSurveyAnswers() {
        return surveyAnswers;
    }

    public void setSurveyAnswers(List<SurveyAnswer> surveyAnswers) {
        this.surveyAnswers = surveyAnswers;
    }
}
