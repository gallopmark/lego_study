package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CollectionEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	@SerializedName("followEntity")
	private FollowEntity followEntity;
	@Expose
	@SerializedName("id")
	private String id;

	public FollowEntity getFollowEntity() {
		return followEntity;
	}

	public void setFollowEntity(FollowEntity followEntity) {
		this.followEntity = followEntity;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public class FollowEntity {
		@Expose
		@SerializedName("id")
		private String id;
		@Expose
		@SerializedName("type")
		private String type;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}
}
