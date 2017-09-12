package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FollowMobileEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	@Expose
	@SerializedName("followNum")
	private Integer followNum;
	@Expose
	@SerializedName("id")
	private String id;

	public Integer getFollowNum() {
		return followNum;
	}

	public void setFollowNum(Integer followNum) {
		this.followNum = followNum;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
