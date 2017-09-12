package com.haoyu.app.entity;

import java.io.Serializable;

public class DownloadEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String url;
	private boolean isShow; // 是否显示CheckBox
	private boolean isChecked; // 是否选中CheckBox

	// public DownloadFileInfo getDownloadFileInfo() {
	// return downloadFileInfo;
	// }
	//
	// public void setDownloadFileInfo(DownloadFileInfo downloadFileInfo) {
	// this.downloadFileInfo = downloadFileInfo;
	// }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isShow() {
		return isShow;
	}

	public void setShow(boolean isShow) {
		this.isShow = isShow;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o.getClass() == DownloadEntity.class) {
			DownloadEntity n = (DownloadEntity) o;
			return n.url.equals(url);
		}
		return false;
	}
}
