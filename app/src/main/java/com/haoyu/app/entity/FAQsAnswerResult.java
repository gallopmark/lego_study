package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FAQsAnswerResult {
	@Expose
	@SerializedName("responseCode")
	private String responseCode;
	@Expose
	@SerializedName("responseData")
	private FAQsAnswerEntity responseData;
	@Expose
	@SerializedName("responseMsg")
	private String responseMsg;
	@Expose
	@SerializedName("success")
	private Boolean success;

	public String getResponseCode() {
		return this.responseCode;
	}

	public FAQsAnswerEntity getResponseData() {
		return this.responseData;
	}

	public Boolean getSuccess() {
		return this.success;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public void setResponseData(FAQsAnswerEntity responseData) {
		this.responseData = responseData;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
}
