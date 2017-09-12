package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.adapter.CourseStudyAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CourseChildSectionEntity implements MultiItemEntity, Serializable {
    @Expose
    @SerializedName("activities")
    private List<CourseSectionActivity> activities = new ArrayList<>();
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("state")
    private String state;
    @Expose
    @SerializedName("completeState")
    private String completeState;
    @Expose
    @SerializedName("title")
    private String title;

    public List<CourseSectionActivity> getActivities() {
        if (activities == null)
            return new ArrayList<>();
        return activities;
    }

    public void setActivities(List<CourseSectionActivity> activities) {
        this.activities = activities;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompleteState() {
        return completeState;
    }

    public void setCompleteState(String completeState) {
        this.completeState = completeState;
    }

    @Override
    public int getItemType() {
        return CourseStudyAdapter.TYPE_LEVEL_1;
    }

}