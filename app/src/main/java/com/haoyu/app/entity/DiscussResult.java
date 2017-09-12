package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建讨论，修改讨论返回的结果集
 */
public class DiscussResult {
	@Expose
	@SerializedName("responseCode")
	private String responseCode;
	@Expose
	@SerializedName("responseData")
	private DiscussEntity responseData;
	@Expose
	@SerializedName("responseMsg")
	private String responseMsg;
	@Expose
	@SerializedName("success")
	private Boolean success;

	public String getResponseCode() {
		return responseCode;
	}

	public DiscussEntity getResponseData() {
		return responseData;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public void setResponseData(DiscussEntity responseData) {
		this.responseData = responseData;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
}
