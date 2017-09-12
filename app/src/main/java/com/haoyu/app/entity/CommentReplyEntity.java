package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CommentReplyEntity {
	@Expose
	@SerializedName("childPostCount")
	private Integer childPostCount;
	@Expose
	@SerializedName("content")
	private String content;
	@Expose
	@SerializedName("createTime")
	private Long createTime;
	@Expose
	@SerializedName("creator")
	private MobileUser creator;
	@Expose
	@SerializedName("discussionUser")
	private DiscussionUser discussionUser;
	@Expose
	@SerializedName("id")
	private String id;
	@Expose
	@SerializedName("isDeleted")
	private String isDeleted;
	@Expose
	@SerializedName("isEssence")
	private String isEssence;
	@Expose
	@SerializedName("isTop")
	private String isTop;
	@Expose
	@SerializedName("score")
	private Double score;
	@Expose
	@SerializedName("supportNum")
	private Integer supportNum;
	@Expose
	@SerializedName("title")
	private String title;
	@Expose
	@SerializedName("updateTime")
	private Long updateTime;
	@Expose
	@SerializedName("version")
	private Long version;

	public Integer getChildPostCount() {
		return childPostCount;
	}

	public void setChildPostCount(Integer childPostCount) {
		this.childPostCount = childPostCount;
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

	public DiscussionUser getDiscussionUser() {
		return discussionUser;
	}

	public void setDiscussionUser(DiscussionUser discussionUser) {
		this.discussionUser = discussionUser;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getIsEssence() {
		return isEssence;
	}

	public void setIsEssence(String isEssence) {
		this.isEssence = isEssence;
	}

	public String getIsTop() {
		return isTop;
	}

	public void setIsTop(String isTop) {
		this.isTop = isTop;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Integer getSupportNum() {
		return supportNum;
	}

	public void setSupportNum(Integer supportNum) {
		this.supportNum = supportNum;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public class DiscussionRelation {
		@Expose
		@SerializedName("browseNum")
		private Integer browseNum;
		@Expose
		@SerializedName("collectNum")
		private Integer collectNum;
		@Expose
		@SerializedName("createTime")
		private Long createTime;
		@Expose
		@SerializedName("creator")
		private MobileUser creator;
		@Expose
		@SerializedName("followNum")
		private Integer followNum;
		@Expose
		@SerializedName("id")
		private String id;
		@Expose
		@SerializedName("isEssence")
		private String isEssence;
		@Expose
		@SerializedName("isTop")
		private String isTop;
		@Expose
		@SerializedName("lastPost")
		private LastPost lastPost;
		@Expose
		@SerializedName("participateNum")
		private Integer participateNum;
		@Expose
		@SerializedName("replyNum")
		private Integer replyNum;
		@Expose
		@SerializedName("supportNum")
		private Integer supportNum;
		@Expose
		@SerializedName("timePeriod")
		private TimePeriod timePeriod;
		@Expose
		@SerializedName("updateTime")
		private Long updateTime;
		@Expose
		@SerializedName("version")
		private Long version;

		public DiscussionRelation() {
		}

		public Integer getBrowseNum() {
			return this.browseNum;
		}

		public Integer getCollectNum() {
			return this.collectNum;
		}

		public Long getCreateTime() {
			return this.createTime;
		}

		public MobileUser getCreator() {
			return this.creator;
		}

		public Integer getFollowNum() {
			return this.followNum;
		}

		public String getId() {
			return this.id;
		}

		public String getIsEssence() {
			return this.isEssence;
		}

		public String getIsTop() {
			return this.isTop;
		}

		public LastPost getLastPost() {
			return this.lastPost;
		}

		public Integer getParticipateNum() {
			return this.participateNum;
		}

		public Integer getReplyNum() {
			return this.replyNum;
		}

		public Integer getSupportNum() {
			return this.supportNum;
		}

		public TimePeriod getTimePeriod() {
			return this.timePeriod;
		}

		public Long getUpdateTime() {
			return this.updateTime;
		}

		public Long getVersion() {
			return this.version;
		}

		public void setBrowseNum(Integer paramInteger) {
			this.browseNum = paramInteger;
		}

		public void setCollectNum(Integer paramInteger) {
			this.collectNum = paramInteger;
		}

		public void setCreateTime(Long paramLong) {
			this.createTime = paramLong;
		}

		public void setCreator(MobileUser paramMobileUser) {
			this.creator = paramMobileUser;
		}

		public void setFollowNum(Integer paramInteger) {
			this.followNum = paramInteger;
		}

		public void setId(String paramString) {
			this.id = paramString;
		}

		public void setIsEssence(String paramString) {
			this.isEssence = paramString;
		}

		public void setIsTop(String paramString) {
			this.isTop = paramString;
		}

		public void setLastPost(LastPost paramLastPost) {
			this.lastPost = paramLastPost;
		}

		public void setParticipateNum(Integer paramInteger) {
			this.participateNum = paramInteger;
		}

		public void setReplyNum(Integer paramInteger) {
			this.replyNum = paramInteger;
		}

		public void setSupportNum(Integer paramInteger) {
			this.supportNum = paramInteger;
		}

		public void setTimePeriod(TimePeriod paramTimePeriod) {
			this.timePeriod = paramTimePeriod;
		}

		public void setUpdateTime(Long paramLong) {
			this.updateTime = paramLong;
		}

		public void setVersion(Long paramLong) {
			this.version = paramLong;
		}
	}

	public class DiscussionUser {
		@Expose
		@SerializedName("createTime")
		private Long createTime;
		@Expose
		@SerializedName("creator")
		private MobileUser creator;
		@Expose
		@SerializedName("discussionRelation")
		private DiscussionRelation discussionRelation;
		@Expose
		@SerializedName("id")
		private String id;
		@Expose
		@SerializedName("isDeleted")
		private String isDeleted;
		@Expose
		@SerializedName("updateTime")
		private Long updateTime;
		@Expose
		@SerializedName("version")
		private Long version;

		public DiscussionUser() {
		}

		public Long getCreateTime() {
			return this.createTime;
		}

		public MobileUser getCreator() {
			return this.creator;
		}

		public DiscussionRelation getDiscussionRelation() {
			return this.discussionRelation;
		}

		public String getId() {
			return this.id;
		}

		public String getIsDeleted() {
			return this.isDeleted;
		}

		public Long getUpdateTime() {
			return this.updateTime;
		}

		public Long getVersion() {
			return this.version;
		}

		public void setCreateTime(Long paramLong) {
			this.createTime = paramLong;
		}

		public void setCreator(MobileUser paramMobileUser) {
			this.creator = paramMobileUser;
		}

		public void setDiscussionRelation(
				DiscussionRelation paramDiscussionRelation) {
			this.discussionRelation = paramDiscussionRelation;
		}

		public void setId(String paramString) {
			this.id = paramString;
		}

		public void setIsDeleted(String paramString) {
			this.isDeleted = paramString;
		}

		public void setUpdateTime(Long paramLong) {
			this.updateTime = paramLong;
		}

		public void setVersion(Long paramLong) {
			this.version = paramLong;
		}
	}

	public class LastPost {
		@Expose
		@SerializedName("childPostCount")
		private Integer childPostCount;
		@Expose
		@SerializedName("createTime")
		private Long createTime;
		@Expose
		@SerializedName("creator")
		private MobileUser creator;
		@Expose
		@SerializedName("isEssence")
		private String isEssence;
		@Expose
		@SerializedName("isTop")
		private String isTop;
		@Expose
		@SerializedName("score")
		private Double score;
		@Expose
		@SerializedName("supportNum")
		private Integer supportNum;
		@Expose
		@SerializedName("updateTime")
		private Long updateTime;
		@Expose
		@SerializedName("version")
		private Long version;

		public LastPost() {
		}

		public Integer getChildPostCount() {
			return childPostCount;
		}

		public void setChildPostCount(Integer childPostCount) {
			this.childPostCount = childPostCount;
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

		public String getIsEssence() {
			return isEssence;
		}

		public void setIsEssence(String isEssence) {
			this.isEssence = isEssence;
		}

		public String getIsTop() {
			return isTop;
		}

		public void setIsTop(String isTop) {
			this.isTop = isTop;
		}

		public Double getScore() {
			return score;
		}

		public void setScore(Double score) {
			this.score = score;
		}

		public Integer getSupportNum() {
			return supportNum;
		}

		public void setSupportNum(Integer supportNum) {
			this.supportNum = supportNum;
		}

		public Long getUpdateTime() {
			return updateTime;
		}

		public void setUpdateTime(Long updateTime) {
			this.updateTime = updateTime;
		}

		public Long getVersion() {
			return version;
		}

		public void setVersion(Long version) {
			this.version = version;
		}
	}

}
