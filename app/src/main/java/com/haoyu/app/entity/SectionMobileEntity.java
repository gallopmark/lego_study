package com.haoyu.app.entity;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.adapter.CourseSectionAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SectionMobileEntity extends AbstractExpandableItem<ChildSectionMobileEntity> implements MultiItemEntity, Serializable {
    private static final long serialVersionUID = 1L;
    @Expose
    @SerializedName("childMSections")
    private List<ChildSectionMobileEntity> childMSections = new ArrayList<>();
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("title")
    private String title;
    private List<CourseSectionActivity> activities = new ArrayList<>();

    public List<ChildSectionMobileEntity> getChildSections() {
        return childMSections;
    }

    public void setChildSections(List<ChildSectionMobileEntity> childSections) {
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

    public List<CourseSectionActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<CourseSectionActivity> activities) {
        this.activities = activities;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getItemType() {
        return CourseSectionAdapter.TYPE_LEVEL_0;
    }
}