package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/3 on 9:55
 * 描述:
 * 作者:马飞奔 Administrator
 */

/**
 * id	工作坊id	String	Y
 title	标题	String	Y
 summary	描述	String	N
 imageUrl	封面链接	String	N
 type	类型	String	Y	personal：个人工作坊 train：项目工作坊   template:示范性工作坊
 qualifiedPoint	工作坊达标分数	BigDecimal	N
 studentNum	学员数量	int	N
 memberNum	成员数量	Int	N
 activityNum	活动数量	int	N
 faqQuestionNum	问答数量	int	N
 resourceNum	资源数量	int	N
 studyHours	学时	Int	N
 mWorkshopSections	工作坊阶段列表	List	N
 timePeriod	工作坊开放时间	Object	N
 summaryExamine	学员考核说明	String	N
 trainName	所属培训	String	N
 createTime	创建时间	long	N
 creator	创建人	MUser	N
 masters	坊主集合	List<MUser>	N
 */
public class WorkShopMobileEntity implements Serializable{
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("summary")
    private String summary;
    @Expose
    @SerializedName("imageUrl")
    private String imageUrl;
    @Expose
    @SerializedName("type")
    private String type;
    @Expose
    @SerializedName("qualifiedPoint")
    private int qualifiedPoint;
    @Expose
    @SerializedName("studentNum")
    private int studentNum;
    @Expose
    @SerializedName("memberNum")
    private int memberNum;
    @Expose
    @SerializedName("activityNum")
    private int activityNum;
    @Expose
    @SerializedName("faqQuestionNum")
    private int faqQuestionNum;
    @Expose
    @SerializedName("resourceNum")
    private int resourceNum;
    @Expose
    @SerializedName("studyHours")
    private int studyHours;
    @Expose
    @SerializedName("timePeriod")
    private TimePeriod timePeriod;
    @Expose
    @SerializedName("mtimePeriod")
    private TimePeriod mTimePeriod;
    @Expose
    @SerializedName("relation")
    private Relation relation;
    @Expose
    @SerializedName("summaryExamine")
    private String summaryExamine;
    @Expose
    @SerializedName("trainName")
    private String trainName;
    @Expose
    @SerializedName("createTime")
    private long createTime;
    @Expose
    @SerializedName("creator")
    private MobileUser creator;
    @Expose
    @SerializedName("masters")
    private List<MobileUser> masters = new ArrayList<>();


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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQualifiedPoint() {
        return qualifiedPoint;
    }

    public void setQualifiedPoint(int qualifiedPoint) {
        this.qualifiedPoint = qualifiedPoint;
    }

    public int getStudentNum() {
        return studentNum;
    }

    public void setStudentNum(int studentNum) {
        this.studentNum = studentNum;
    }

    public int getMemberNum() {
        return memberNum;
    }

    public void setMemberNum(int memberNum) {
        this.memberNum = memberNum;
    }

    public int getActivityNum() {
        return activityNum;
    }

    public void setActivityNum(int activityNum) {
        this.activityNum = activityNum;
    }

    public int getFaqQuestionNum() {
        return faqQuestionNum;
    }

    public void setFaqQuestionNum(int faqQuestionNum) {
        this.faqQuestionNum = faqQuestionNum;
    }

    public int getResourceNum() {
        return resourceNum;
    }

    public void setResourceNum(int resourceNum) {
        this.resourceNum = resourceNum;
    }

    public int getStudyHours() {
        return studyHours;
    }

    public void setStudyHours(int studyHours) {
        this.studyHours = studyHours;
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

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public String getSummaryExamine() {
        return summaryExamine;
    }

    public void setSummaryExamine(String summaryExamine) {
        this.summaryExamine = summaryExamine;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public MobileUser getCreator() {
        return creator;
    }

    public void setCreator(MobileUser creator) {
        this.creator = creator;
    }

    public List<MobileUser> getMasters() {
        return masters;
    }

    public void setMasters(List<MobileUser> masters) {
        this.masters = masters;
    }
}
