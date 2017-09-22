package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CourseMobileEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("title")
    @Expose
    private String title; // 课程标题
    @SerializedName("organization")
    @Expose
    private String organization; // 主办方
    @SerializedName("studyHours")
    @Expose
    private double studyHours;// 学时
    @SerializedName("image")
    @Expose
    private String image;// 课程封面图片地址
    @SerializedName("registerNum")
    @Expose
    private int registerNum;  //报读数
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("termNo")
    @Expose
    private String termNo;
    @SerializedName("intro")
    @Expose
    private String intro;
    @SerializedName("mTimePeriod")
    @Expose
    private TimePeriod mTimePeriod;
    @SerializedName("mSections")
    @Expose
    private List<CourseSectionEntity> mSections = new ArrayList<>();

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The organization
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * @param organization The organization
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * @return The studyHours
     */
    public int getStudyHours() {
        try {
            return Integer.parseInt(new DecimalFormat("0").format(studyHours));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return (int) studyHours;
    }

    /**
     * @param studyHours The studyHours
     */
    public void setStudyHours(double studyHours) {
        this.studyHours = studyHours;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<CourseSectionEntity> getmSections() {
        return mSections;
    }

    public void setmSections(List<CourseSectionEntity> mSections) {
        this.mSections = mSections;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public int getRegisterNum() {
        return registerNum;
    }

    public void setRegisterNum(int registerNum) {
        this.registerNum = registerNum;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTermNo() {
        return termNo;
    }

    public void setTermNo(String termNo) {
        this.termNo = termNo;
    }

    public TimePeriod getmTimePeriod() {
        return mTimePeriod;
    }

    public void setmTimePeriod(TimePeriod mTimePeriod) {
        this.mTimePeriod = mTimePeriod;
    }
}
