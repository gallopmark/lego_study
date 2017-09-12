package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class DictEntryResult {
	@SerializedName("responseCode")
	@Expose
	private String responseCode;
	@SerializedName("responseData")
	@Expose
	private List<DictEntryMobileEntity> responseData = new ArrayList<DictEntryMobileEntity>();
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
	public List<DictEntryMobileEntity> getResponseData() {
		return responseData;
	}

	/**
	 * 
	 * @param responseData
	 *            The responseData
	 */
	public void setResponseData(List<DictEntryMobileEntity> responseData) {
		this.responseData = responseData;
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
