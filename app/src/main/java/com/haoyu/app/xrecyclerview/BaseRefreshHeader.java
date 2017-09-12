package com.haoyu.app.xrecyclerview;

/**
 * Created by jianghejie on 15/11/22.
 */
interface BaseRefreshHeader {

	int STATE_NORMAL = 0;
	int STATE_RELEASE_TO_REFRESH = 1;
	int STATE_REFRESHING = 2;
	int STATE_DONE = 5;
	int STATE_SUCCESS = 3;
	int STATE_FRESH_FAILT = 4;

	void onMove(float delta);

	boolean releaseAction();

	void refreshComplete(boolean isSuccess);

}