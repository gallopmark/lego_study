package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/11 on 19:10
 * 描述:教研创课实体类
 * 作者:马飞奔 Administrator
 */
public class TeachingLessonEntity implements Serializable {
    @Expose
    @SerializedName("title")
    private String title; // 讨论标题
    @Expose
    @SerializedName("content")
    private String content; // 讨论内容
    @Expose
    @SerializedName("createTime")
    private Long createTime; // 讨论创建时间
    @Expose
    @SerializedName("creator")
    private MobileUser creator; // 讨论创建人
    @Expose
    @SerializedName("id")
    private String id; // 讨论Id
    @Expose
    @SerializedName("attitudeUser")
    private AttitudeUserMobileEntity attitudeUser;
    @Expose
    @SerializedName("mFileInfos")
    private List<MFileInfo> mFileInfos = new ArrayList<>();  //附件列表  集合可能为空
    @Expose
    @SerializedName("remainDay")  //剩余天数
    private int remainDay;
    @Expose
    @SerializedName("mainPostNum")
    private int mainPostNum;  //要求完成主回复数
    @Expose
    @SerializedName("subPostNum")
    private int subPostNum;  // 要求完成子回复数
    @Expose
    @SerializedName("mDiscussionRelations")
    private List<LessonRelation> mDiscussionRelations = new ArrayList<>(); // 讨论关联关系，用于获取点赞数

    private boolean isSupport;  //是否已经点赞

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public MobileUser getCreator() {
        return creator;
    }

    public void setCreator(MobileUser creator) {
        this.creator = creator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AttitudeUserMobileEntity getAttitudeUser() {
        return attitudeUser;
    }

    public void setAttitudeUser(AttitudeUserMobileEntity attitudeUser) {
        this.attitudeUser = attitudeUser;
    }

    public List<MFileInfo> getmFileInfos() {
        return mFileInfos;
    }

    public void setmFileInfos(List<MFileInfo> mFileInfos) {
        this.mFileInfos = mFileInfos;
    }

    public int getRemainDay() {
        return remainDay;
    }

    public void setRemainDay(int remainDay) {
        this.remainDay = remainDay;
    }

    public int getMainPostNum() {
        return mainPostNum;
    }

    public void setMainPostNum(int mainPostNum) {
        this.mainPostNum = mainPostNum;
    }

    public int getSubPostNum() {
        return subPostNum;
    }

    public void setSubPostNum(int subPostNum) {
        this.subPostNum = subPostNum;
    }

    public List<LessonRelation> getmDiscussionRelations() {
        return mDiscussionRelations;
    }

    public void setmDiscussionRelations(List<LessonRelation> mDiscussionRelations) {
        this.mDiscussionRelations = mDiscussionRelations;
    }

    public boolean isSupport() {
        return isSupport;
    }

    public void setSupport(boolean support) {
        isSupport = support;
    }

    public class LessonRelation {
        @Expose
        @SerializedName("id")
        private String id;
        @Expose
        @SerializedName("replyNum")
        private int replyNum;     //回复数
        @Expose
        @SerializedName("supportNum")
        private int supportNum;  //点赞数
        @Expose
        @SerializedName("participateNum")
        private int participateNum;  //参与数
        @Expose
        @SerializedName("browseNum")   //总浏览数
        private int browseNum;
        @Expose
        @SerializedName("followNum")  //收藏数
        private int followNum;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getReplyNum() {
            return replyNum;
        }

        public void setReplyNum(int replyNum) {
            this.replyNum = replyNum;
        }

        public int getSupportNum() {
            return supportNum;
        }

        public void setSupportNum(int supportNum) {
            this.supportNum = supportNum;
        }

        public int getParticipateNum() {
            return participateNum;
        }

        public void setParticipateNum(int participateNum) {
            this.participateNum = participateNum;
        }

        public int getBrowseNum() {
            return browseNum;
        }

        public void setBrowseNum(int browseNum) {
            this.browseNum = browseNum;
        }

        public int getFollowNum() {
            return followNum;
        }

        public void setFollowNum(int followNum) {
            this.followNum = followNum;
        }
    }
}
