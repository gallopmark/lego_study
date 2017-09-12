package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * 笔记列表结果集
 */
public class NoteListResult {
	@Expose
	@SerializedName("responseCode")
	private String responseCode;
	@Expose
	@SerializedName("responseData")
	private List<NoteEntity> responseData = new ArrayList<NoteEntity>();
	@Expose
	@SerializedName("responseMsg")
	private String responseMsg;
	@Expose
	@SerializedName("success")
	private Boolean success;

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public List<NoteEntity> getResponseData() {
		return responseData;
	}

	public void setResponseData(List<NoteEntity> responseData) {
		this.responseData = responseData;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

}
