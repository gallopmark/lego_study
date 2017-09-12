package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AttitudeCountEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	@Expose
	@SerializedName("attitude")
	private String attitude;
	@Expose
	@SerializedName("participateNum")
	private Integer participateNum;
	@Expose
	@SerializedName("relation")
	private Relation relation;

	public String getAttitude() {
		return this.attitude;
	}

	public Integer getParticipateNum() {
		return this.participateNum;
	}

	public Relation getRelation() {
		return this.relation;
	}

	public void setAttitude(String attitude) {
		this.attitude = attitude;
	}

	public void setParticipateNum(Integer participateNum) {
		this.participateNum = participateNum;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}
}
