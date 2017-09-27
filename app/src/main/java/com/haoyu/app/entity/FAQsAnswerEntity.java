package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 问题的回答对象
 */
public class FAQsAnswerEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	@Expose
	@SerializedName("content")
	private String content;		//回答内容
	@Expose
	@SerializedName("createTime")
	private long createTime;		//回答时间
	@Expose
	@SerializedName("creator")
	private MobileUser creator;		//回答人
	@Expose
	@SerializedName("id")
	private String id;		//回答Id

	public String getContent() {
		return this.content;
	}

	public long getCreateTime() {
		return this.createTime;
	}

	public MobileUser getCreator() {
		return this.creator;
	}

	public String getId() {
		return this.id;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public void setCreator(MobileUser creator) {
		this.creator = creator;
	}

	public void setId(String id) {
		this.id = id;
	}
}
