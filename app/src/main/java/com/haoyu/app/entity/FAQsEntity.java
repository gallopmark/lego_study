package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 问答类对象
 */
public class FAQsEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private int collectCount;
    @Expose
    @SerializedName("content")
    private String content; // 问答内容
    @Expose
    @SerializedName("createTime")
    private long createTime; // 创建时间
    @Expose
    @SerializedName("creator")
    private MobileUser creator; // 创建人
    @Expose
    @SerializedName("faqAnswerCount")
    private int faqAnswerCount; // 回答数
    @Expose
    @SerializedName("faqAnswers")
    private List<FaqAnswerMobileEntity> faqAnswers = new ArrayList<FaqAnswerMobileEntity>(); // 回答列表
    @Expose
    @SerializedName("follow")
    private FollowMobileEntity follow;      //收藏实体类
    @Expose
    @SerializedName("id")
    private String id;  //问答Id
    @Expose
    @SerializedName("relation")
    private Relation relation;  //问答关联关系(关联课程)

    public int getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(int collectCount) {
        this.collectCount = collectCount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public int getFaqAnswerCount() {
        return faqAnswerCount;
    }

    public void setFaqAnswerCount(int faqAnswerCount) {
        this.faqAnswerCount = faqAnswerCount;
    }

    public List<FaqAnswerMobileEntity> getFaqAnswers() {
        return faqAnswers;
    }

    public void setFaqAnswers(List<FaqAnswerMobileEntity> faqAnswers) {
        this.faqAnswers = faqAnswers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FollowMobileEntity getFollow() {
        return follow;
    }

    public void setFollow(FollowMobileEntity follow) {
        this.follow = follow;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj.getClass() == FAQsEntity.class) {
            return ((FAQsEntity) obj).id.equals(this.id);
        }
        return false;
    }
}