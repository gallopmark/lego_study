package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/3 on 14:10
 * 描述:
 * 作者:马飞奔 Administrator
 */

/**
 * id	阶段id	String	Y
 * title	标题	String	Y
 * timePeriod	开放时间	Object	Y
 */
public class MWorkshopSection implements Serializable, MultiItemEntity {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("timePeriod")
    private TimePeriod timePeriod;
    /*活动列表*/
    private List<MWorkshopActivity> activities = new ArrayList<>();
    private int position;
    private MWSActivityCrease crease;

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

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    public List<MWorkshopActivity> getActivities() {
        if (activities == null)
            return new ArrayList<>();
        return activities;
    }

    public void setActivities(List<MWorkshopActivity> activities) {
        this.activities = activities;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public MWSActivityCrease getCrease() {
        return crease;
    }

    public void setCrease(MWSActivityCrease crease) {
        this.crease = crease;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MWorkshopSection)) {
            return false;
        }
        MWorkshopSection section = (MWorkshopSection) obj;
        return this.id.equals(section.id);
    }

    @Override
    public int getItemType() {
        return 1;
    }
}
