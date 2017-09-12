package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 笔记对象类
 */
public class NoteEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	@Expose
	@SerializedName("content")
	// 笔记内容
	private String content;
	@Expose
	@SerializedName("createTime")
	private long createTime; // 创建笔记的时间
	@Expose
	@SerializedName("id")
	private String id; // 笔记Id
	@Expose
	@SerializedName("relation")
	private Relation relation; // 笔记关联关系
	@Expose
	@SerializedName("creator")
	private MobileUser creator; // 笔记创建人

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	public MobileUser getCreator() {
		return creator;
	}

	public void setCreator(MobileUser creator) {
		this.creator = creator;
	}

}