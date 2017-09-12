package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FaqAnswerMobileEntity implements Serializable {
	private static final long serialVersionUID = 1L;
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
	@SerializedName("entityId")
	private String entityId;
	@Expose
	@SerializedName("updateTime")
	private Long updateTime;
	@Expose
	@SerializedName("version")
	private Integer version;

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

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}