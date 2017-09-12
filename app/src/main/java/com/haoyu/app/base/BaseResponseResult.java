package com.haoyu.app.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class BaseResponseResult<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	@Expose
	@SerializedName("responseCode")
	public String responseCode;
	@Expose
	@SerializedName("responseData")
	public T responseData;
	@Expose
	@SerializedName("responseMsg")
	public String responseMsg;
	@Expose
	@SerializedName("success")
	public Boolean success;

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public T getResponseData() {
		return responseData;
	}

	public void setResponseData(T responseData) {
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

	public String toString() {
		return "responseCode:" + this.responseCode + "\tsuccess:"
				+ this.success + "\tresponseData:" + this.responseData;
	}
}
