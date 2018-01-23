package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/10/26 on 10:24
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CmtsLessonAttribute implements Serializable{
    /**
     * id	ID
     * mStage	学段
     * mSubject	学科
     */
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("stage")
    private String stage;
    @Expose
    @SerializedName("subject")
    private String subject;
    @Expose
    @SerializedName("topic")   //主题
    private String topic;
    @Expose
    @SerializedName("topicSummary")  //主题描述
    private String topicSummary;
    @Expose
    @SerializedName("realia")   //乐高教具
    private String realia;
    @Expose
    @SerializedName("realiaSummary")    //教具介绍
    private String realiaSummary;
    @Expose
    @SerializedName("realiaUseReason")   //教具使用理由
    private String realiaUseReason;
    @Expose
    @SerializedName("topicBase")  //课标依据
    private String topicBase;
    @Expose
    @SerializedName("learnDetail")  //如何学习
    private String learnDetail;
    @Expose
    @SerializedName("designPrinciple")  //设计原则
    private String designPrinciple;
    @Expose
    @SerializedName("stemElement")  //Stem元素
    private String stemElement;
    @Expose
    @SerializedName("examples")   //联系
    private String examples;
    @Expose
    @SerializedName("models")  //建构
    private String models;
    @Expose
    @SerializedName("rethink")  //反思
    private String rethink;
    @Expose
    @SerializedName("expand")  //拓展
    private String expand;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopicSummary() {
        return topicSummary;
    }

    public void setTopicSummary(String topicSummary) {
        this.topicSummary = topicSummary;
    }

    public String getRealia() {
        return realia;
    }

    public void setRealia(String realia) {
        this.realia = realia;
    }

    public String getRealiaSummary() {
        return realiaSummary;
    }

    public void setRealiaSummary(String realiaSummary) {
        this.realiaSummary = realiaSummary;
    }

    public String getRealiaUseReason() {
        return realiaUseReason;
    }

    public void setRealiaUseReason(String realiaUseReason) {
        this.realiaUseReason = realiaUseReason;
    }

    public String getTopicBase() {
        return topicBase;
    }

    public void setTopicBase(String topicBase) {
        this.topicBase = topicBase;
    }

    public String getLearnDetail() {
        return learnDetail;
    }

    public void setLearnDetail(String learnDetail) {
        this.learnDetail = learnDetail;
    }

    public String getDesignPrinciple() {
        return designPrinciple;
    }

    public void setDesignPrinciple(String designPrinciple) {
        this.designPrinciple = designPrinciple;
    }

    public String getStemElement() {
        return stemElement;
    }

    public void setStemElement(String stemElement) {
        this.stemElement = stemElement;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }

    public String getModels() {
        return models;
    }

    public void setModels(String models) {
        this.models = models;
    }

    public String getRethink() {
        return rethink;
    }

    public void setRethink(String rethink) {
        this.rethink = rethink;
    }

    public String getExpand() {
        return expand;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }
}
