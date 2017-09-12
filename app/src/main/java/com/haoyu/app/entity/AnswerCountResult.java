package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AnswerCountResult {
	@Expose
	@SerializedName("responseCode")
	private String responseCode;
	@Expose
	@SerializedName("responseData")
	private ResponeCount responseData;
	@Expose
	@SerializedName("responseMsg")
	private String responseMsg;
	@Expose
	@SerializedName("success")
	private Boolean success;

	public String getResponseCode() {
		return this.responseCode;
	}

	public ResponeCount getResponseData() {
		return this.responseData;
	}

	public String getResponseMsg() {
		return this.responseMsg;
	}

	public Boolean getSuccess() {
		return this.success;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public void setResponseData(ResponeCount responseData) {
		this.responseData = responseData;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public class ResponeCount {
		@Expose
		@SerializedName("count")
		private Integer count;

		public ResponeCount() {
		}

		public Integer getCount() {
			return count;
		}

		public void setCount(Integer count) {
			this.count = count;
		}
	}
}

/*
 * Location: E:\workspace\com.haoyu.app-2_dex2jar.jar
 * 
 * Qualified Name: com.haoyu.app.entity.AnswerCountResult
 * 
 * JD-Core Version: 0.7.0.1
 */