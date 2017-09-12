package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CourseListResult {
	@SerializedName("responseCode")
	@Expose
	private String responseCode;
	@SerializedName("responseData")
	@Expose
	private List<CourseMobileEntity> responseData = new ArrayList<CourseMobileEntity>();
	@SerializedName("responseMsg")
	@Expose
	private String responseMsg;
	@SerializedName("success")
	@Expose
	private Boolean success;

	/**
	 *
	 * @return The responseCode
	 */
	public String getResponseCode() {
		return responseCode;
	}

	/**
	 *
	 * @param responseCode
	 *            The responseCode
	 */
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 *
	 * @return The responseData
	 */
	public List<CourseMobileEntity> getResponseData() {
		return responseData;
	}

	/**
	 *
	 * @param responseData
	 *            The responseData
	 */
	public void setResponseData(List<CourseMobileEntity> responseData) {
		this.responseData = responseData;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	/**
	 *
	 * @return The success
	 */
	public Boolean getSuccess() {
		return success;
	}

	/**
	 *
	 * @param success
	 *            The success
	 */
	public void setSuccess(Boolean success) {
		this.success = success;
	}
}
