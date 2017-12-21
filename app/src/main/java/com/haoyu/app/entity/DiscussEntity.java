package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 讨论对象类
 */
public class DiscussEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Expose
    @SerializedName("title")
    private String title; // 讨论标题
    @Expose
    @SerializedName("content")
    private String content; // 讨论内容
    @Expose
    @SerializedName("createTime")
    private long createTime; // 讨论创建时间
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
    @SerializedName("mainPostNum")
    private int mainPostNum;  //要求完成主回复数
    @Expose
    @SerializedName("subPostNum")
    private int subPostNum;  // 要求完成子回复数
    @Expose
    @SerializedName("mDiscussionRelations")
    private List<DiscussionRelation> mDiscussionRelations = new ArrayList<>(); // 讨论关联关系，用于获取点赞数
    private boolean isSupport;   //是否已经点赞
    @Expose
    @SerializedName("childPostCount")  //建议数
    private int childPostCount;

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

    public List<DiscussionRelation> getmDiscussionRelations() {
        if (mDiscussionRelations == null) {
            return new ArrayList<>();
        }
        return mDiscussionRelations;
    }

    public void setmDiscussionRelations(List<DiscussionRelation> mDiscussionRelations) {
        this.mDiscussionRelations = mDiscussionRelations;
    }

    public int getChildPostCount() {
        return childPostCount;
    }

    public void setChildPostCount(int childPostCount) {
        this.childPostCount = childPostCount;
    }

    public boolean isSupport() {
        return isSupport;
    }

    public void setSupport(boolean support) {
        isSupport = support;
    }

    public class DiscussionRelation implements Serializable {
        private static final long serialVersionUID = 1L;
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
            return this.id;
        }

        public int getReplyNum() {
            return this.replyNum;
        }

        public void setId(String id) {
            this.id = id;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj instanceof DiscussEntity) {
            return ((DiscussEntity) obj).id.equals(this.id);
        }
        return false;
    }
}
