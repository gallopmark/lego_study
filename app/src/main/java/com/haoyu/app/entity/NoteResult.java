package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 创建笔记，修改笔记返回的结果集
 */
public class NoteResult {
	@Expose
	@SerializedName("responseCode")
	private String responseCode;
	@Expose
	@SerializedName("responseData")
	private NoteEntity responseData;
	@Expose
	@SerializedName("responseMsg")
	private String responseMsg;
	@Expose
	@SerializedName("success")
	private Boolean success;

	public String getResponseCode() {
		return responseCode;
	}

	public NoteEntity getResponseData() {
		return responseData;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public void setResponseData(NoteEntity responseData) {
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
