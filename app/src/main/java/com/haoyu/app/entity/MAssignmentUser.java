package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.entity.MAssignmentEntity;
import com.haoyu.app.entity.MFileInfo;

import java.util.List;

/**
 * Created by acer1 on 2017/1/7.
 */
public class MAssignmentUser {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("mAssignment")
    private MAssignmentEntity mAssignment;
    @Expose
    @SerializedName("state")
    private String state;
    @Expose
    @SerializedName("responseTime")
    private Long responseTime;
    @Expose
    @SerializedName("responseScore")
    private double responseScore;
    @Expose
    @SerializedName("markNum")
    private int markNum;
    @Expose
    @SerializedName("markScore")
    private double markScore;
    @Expose
    @SerializedName("markedNum")
    private int markedNum;
    @Expose
    @SerializedName("assignmentRelationId")
    private String assignmentRelationId;
    @Expose
    @SerializedName("mFileInfos")
    private List<MFileInfo> mFileInfos;
    @Expose
    @SerializedName("mUser")
    private String mUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MAssignmentEntity getmAssignment() {
        return mAssignment;
    }

    public void setmAssignment(MAssignmentEntity mAssignment) {
        this.mAssignment = mAssignment;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public double getResponseScore() {
        return responseScore;
    }

    public void setResponseScore(double responseScore) {
        this.responseScore = responseScore;
    }

    public int getMarkNum() {
        return markNum;
    }

    public void setMarkNum(int markNum) {
        this.markNum = markNum;
    }

    public double getMarkScore() {
        return markScore;
    }

    public void setMarkScore(double markScore) {
        this.markScore = markScore;
    }

    public int getMarkedNum() {
        return markedNum;
    }

    public void setMarkedNum(int markedNum) {
        this.markedNum = markedNum;
    }

    public String getAssignmentRelationId() {
        return assignmentRelationId;
    }

    public void setAssignmentRelationId(String assignmentRelationId) {
        this.assignmentRelationId = assignmentRelationId;
    }

    public List<MFileInfo> getmFileInfos() {
        return mFileInfos;
    }

    public void setmFileInfos(List<MFileInfo> mFileInfos) {
        this.mFileInfos = mFileInfos;
    }

    public String getmUser() {
        return mUser;
    }

    public void setmUser(String mUser) {
        this.mUser = mUser;
    }
}