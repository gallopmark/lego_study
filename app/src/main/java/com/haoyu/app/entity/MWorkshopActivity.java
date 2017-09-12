package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/1/3 on 20:30
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MWorkshopActivity implements Serializable {
    /**
     * id	活动ID	String	Y
     * title	活动标题	String	Y
     * type	活动类型	String	Y	discussion:主题研讨
     * lessonPlan:集体备课
     * lcec:听课评课
     * test:测验
     * debate:辩论
     * survey:调查问卷
     * video:音视频
     * completeState	完成状态	String	Y	未参与
     * 已完成
     * 进行中
     */
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("type")
    private String type;
    @Expose
    @SerializedName("completeState")
    private String completeState;
    @Expose
    @SerializedName("timePeriod")
    private TimePeriod timePeriod;
    @Expose
    @SerializedName("inCurrentDate")
    private boolean inCurrentDate;
    //当前位置
    @Expose
    @SerializedName("position")
    private int position;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCompleteState() {
        return completeState;
    }

    public void setCompleteState(String completeState) {
        this.completeState = completeState;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    public boolean isInCurrentDate() {
        return inCurrentDate;
    }

    public void setInCurrentDate(boolean inCurrentDate) {
        this.inCurrentDate = inCurrentDate;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
