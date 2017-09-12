package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class AttitudeCountResult {
	@Expose
	@SerializedName("responseCode")
	private String responseCode;
	@Expose
	@SerializedName("responseData")
	private List<AttitudeCountEntity> responseData = new ArrayList<AttitudeCountEntity>();
	@Expose
	@SerializedName("responseMsg")
	private String respposeMsg;
	@Expose
	@SerializedName("success")
	private Boolean success;

	public String getResponseCode() {
		return this.responseCode;
	}

	public List<AttitudeCountEntity> getResponseData() {
		return this.responseData;
	}

	public String getRespposeMsg() {
		return this.respposeMsg;
	}

	public Boolean getSuccess() {
		return this.success;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public void setResponseData(List<AttitudeCountEntity> responseData) {
		this.responseData = responseData;
	}

	public void setRespposeMsg(String respposeMsg) {
		this.respposeMsg = respposeMsg;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
}
