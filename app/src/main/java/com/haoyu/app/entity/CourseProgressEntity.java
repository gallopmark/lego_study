package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2016/12/24 on 16:46
 * 描述:
 * 作者:马飞奔 Administrator
 */

/**
 * score	课程得分	BigDecimal	Y
 * state	课程学习状态	String	Y	pass：合格，nopass:不合格，null：未登记
 * activityVideoNum	视频总数	int	Y
 * activityDiscussionNum	研讨总数	int	Y
 * activityTestNum	测试总数	int	Y
 * activityAssignmentNum	作业总数	int	Y
 * activityHtmlNum	课件总数	int	Y
 * activitySurveyNum	问卷总数	int	Y
 * completeVideoNum	已完成视频数	int	Y
 * completeDiscussionNum	已完成研讨数	int	Y
 * completeTestNum	已完成测试数	int	Y
 * completeAssignmentNum	已完成作业数	int	Y
 * completeHtmlNum	已完成课件数	int	Y
 * completeSurveyNum	已完成问卷数	int	Y
 * timePeriod	课程开放时间	Object	N
 */
public class CourseProgressEntity implements Serializable {
    @Expose
    @SerializedName("score")
    private double score; //课程得分
    @Expose
    @SerializedName("state")
    private String state; //课程学习状态	String	Y	pass：合格，nopass:不合格，null：未登记
    @Expose
    @SerializedName("activityVideoNum")
    private int activityVideoNum; //视频总数
    @Expose
    @SerializedName("activityDiscussionNum")
    private int activityDiscussionNum; //activityDiscussionNum
    @Expose
    @SerializedName("activityTestNum")
    private int activityTestNum; //测试总数
    @Expose
    @SerializedName("activityAssignmentNum")
    private int activityAssignmentNum; //作业总数
    @Expose
    @SerializedName("activityHtmlNum")
    private int activityHtmlNum;  //课件总数
    @Expose
    @SerializedName("activitySurveyNum")
    private int activitySurveyNum; //问卷总数
    @Expose
    @SerializedName("completeVideoNum")
    private int completeVideoNum;
    @Expose
    @SerializedName("completeDiscussionNum")
    private int completeDiscussionNum;
    @Expose
    @SerializedName("completeTestNum")
    private int completeTestNum;
    @Expose
    @SerializedName("completeAssignmentNum")
    private int completeAssignmentNum;  //已完成作业数
    @Expose
    @SerializedName("completeHtmlNum")
    private int completeHtmlNum;    //已完成课件数
    @Expose
    @SerializedName("completeSurveyNum")
    private int completeSurveyNum;    //已完成问卷数
    @Expose
    @SerializedName("timePeriod")
    private TimePeriod timePeriod;
    @Expose
    @SerializedName("mTimePeriod")
    private TimePeriod mTimePeriod;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getActivityVideoNum() {
        return activityVideoNum;
    }

    public void setActivityVideoNum(int activityVideoNum) {
        this.activityVideoNum = activityVideoNum;
    }

    public int getActivityDiscussionNum() {
        return activityDiscussionNum;
    }

    public void setActivityDiscussionNum(int activityDiscussionNum) {
        this.activityDiscussionNum = activityDiscussionNum;
    }

    public int getActivityTestNum() {
        return activityTestNum;
    }

    public void setActivityTestNum(int activityTestNum) {
        this.activityTestNum = activityTestNum;
    }

    public int getActivityAssignmentNum() {
        return activityAssignmentNum;
    }

    public void setActivityAssignmentNum(int activityAssignmentNum) {
        this.activityAssignmentNum = activityAssignmentNum;
    }

    public int getActivityHtmlNum() {
        return activityHtmlNum;
    }

    public void setActivityHtmlNum(int activityHtmlNum) {
        this.activityHtmlNum = activityHtmlNum;
    }

    public int getActivitySurveyNum() {
        return activitySurveyNum;
    }

    public void setActivitySurveyNum(int activitySurveyNum) {
        this.activitySurveyNum = activitySurveyNum;
    }

    public int getCompleteVideoNum() {
        return completeVideoNum;
    }

    public void setCompleteVideoNum(int completeVideoNum) {
        this.completeVideoNum = completeVideoNum;
    }

    public int getCompleteDiscussionNum() {
        return completeDiscussionNum;
    }

    public void setCompleteDiscussionNum(int completeDiscussionNum) {
        this.completeDiscussionNum = completeDiscussionNum;
    }

    public int getCompleteTestNum() {
        return completeTestNum;
    }

    public void setCompleteTestNum(int completeTestNum) {
        this.completeTestNum = completeTestNum;
    }

    public int getCompleteAssignmentNum() {
        return completeAssignmentNum;
    }

    public void setCompleteAssignmentNum(int completeAssignmentNum) {
        this.completeAssignmentNum = completeAssignmentNum;
    }

    public int getCompleteHtmlNum() {
        return completeHtmlNum;
    }

    public void setCompleteHtmlNum(int completeHtmlNum) {
        this.completeHtmlNum = completeHtmlNum;
    }

    public int getCompleteSurveyNum() {
        return completeSurveyNum;
    }

    public void setCompleteSurveyNum(int completeSurveyNum) {
        this.completeSurveyNum = completeSurveyNum;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    public TimePeriod getmTimePeriod() {
        return mTimePeriod;
    }

    public void setmTimePeriod(TimePeriod mTimePeriod) {
        this.mTimePeriod = mTimePeriod;
    }
}
