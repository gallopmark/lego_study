package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.adapter.CourseStudyAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CourseSectionEntity implements MultiItemEntity, Serializable {
    private static final long serialVersionUID = 1L;
    @Expose
    @SerializedName("childMSections")
    private List<CourseChildSectionEntity> childMSections = new ArrayList<>();
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("title")
    private String title;

    public List<CourseChildSectionEntity> getChildSections() {
        return childMSections;
    }

    public void setChildSections(List<CourseChildSectionEntity> childSections) {
        this.childMSections = childSections;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int getItemType() {
        return CourseStudyAdapter.TYPE_LEVEL_0;
    }
}