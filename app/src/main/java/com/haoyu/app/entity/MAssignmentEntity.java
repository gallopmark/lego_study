package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.entity.*;

import java.util.List;

/**
 * Created by acer1 on 2016/12/29.
 */
public class MAssignmentEntity {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("markType")
    private String markType;
    @Expose
    @SerializedName("markScorePct")
    private double markScorePct;
    @Expose
    @SerializedName("markNum")
    private int markNum;
    @Expose
    @SerializedName("content")
    private String content;
    @Expose
    @SerializedName("mFileInfos")
    private List<MFileInfo> mFileInfos ;
    @Expose
    @SerializedName("fileTypes")
    private String fileTypes;
    @Expose
    @SerializedName("fileSize")
    private double fileSize;
    @Expose
    @SerializedName("inResponseTime")
    private boolean inResponseTime;
    @Expose
    @SerializedName("inMarkTime")
    private boolean inMarkTime;
    @Expose
    @SerializedName("responseTime")
    private TimePeriod responseTime;
    @Expose
    @SerializedName("markTime")
    private TimePeriod markTime;

    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setMarkType(String markType){
        this.markType = markType;
    }
    public String getMarkType(){
        return this.markType;
    }
    public void setMarkScorePct(double markScorePct){
        this.markScorePct = markScorePct;
    }
    public double getMarkScorePct(){
        return this.markScorePct;
    }
    public void setMarkNum(int markNum){
        this.markNum = markNum;
    }
    public int getMarkNum(){
        return this.markNum;
    }
    public void setContent(String content){
        this.content = content;
    }
    public String getContent(){
        return this.content;
    }
    public void setMFileInfos(List<MFileInfo> mFileInfos){
        this.mFileInfos = mFileInfos;
    }
    public List<MFileInfo> getMFileInfos(){
        return this.mFileInfos;
    }
    public void setFileTypes(String fileTypes){
        this.fileTypes = fileTypes;
    }
    public String getFileTypes(){
        return this.fileTypes;
    }
    public void setFileSize(double fileSize){
        this.fileSize = fileSize;
    }
    public double getFileSize(){
        return this.fileSize;
    }
    public void setInResponseTime(boolean inResponseTime){
        this.inResponseTime = inResponseTime;
    }
    public boolean getInResponseTime(){
        return this.inResponseTime;
    }
    public void setInMarkTime(boolean inMarkTime){
        this.inMarkTime = inMarkTime;
    }
    public boolean getInMarkTime(){
        return this.inMarkTime;
    }
    public void setResponseTime(TimePeriod responseTime){
        this.responseTime = responseTime;
    }
    public TimePeriod getResponseTime(){
        return this.responseTime;
    }
    public void setMarkTime(TimePeriod markTime){
        this.markTime = markTime;
    }
    public TimePeriod getMarkTime(){
        return this.markTime;
    }

}