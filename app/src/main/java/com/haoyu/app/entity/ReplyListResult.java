package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReplyListResult {
	@Expose
	@SerializedName("responseCode")
	private String responseCode;
	@Expose
	@SerializedName("responseData")
	private MainReplyListData responseData;
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

	public MainReplyListData getResponseData() {
		return responseData;
	}

	public void setResponseData(MainReplyListData responseData) {
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

	public class MainReplyListData implements Serializable{
		@Expose
		@SerializedName("mDiscussionPosts")
		private List<ReplyEntity> mDiscussionPosts = new ArrayList<>();
		@SerializedName("paginator")
		@Expose
		private Paginator paginator;

		public List<ReplyEntity> getmDiscussionPosts() {
			return mDiscussionPosts;
		}

		public void setmDiscussionPosts(List<ReplyEntity> mDiscussionPosts) {
			this.mDiscussionPosts = mDiscussionPosts;
		}

		public Paginator getPaginator() {
			return paginator;
		}

		public void setPaginator(Paginator paginator) {
			this.paginator = paginator;
		}
	}
}