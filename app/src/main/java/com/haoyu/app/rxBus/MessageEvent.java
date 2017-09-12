package com.haoyu.app.rxBus;

import android.os.Bundle;

/**
 * EventBus消息类
 */
public class MessageEvent {
	public String action = "";
	public int arg1;
	public int arg2;
	public int msgType;
	public static final int SUCCESS = 1;
	public static final int FAILURE = 2;
	public Object obj;
	private Bundle bundle;

	public String getAction() {
		return this.action;
	}

	public int getArg1() {
		return this.arg1;
	}

	public int getArg2() {
		return this.arg2;
	}

	public int getMsgType() {
		return this.msgType;
	}

	public Object getObj() {
		return this.obj;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setArg1(int arg1) {
		this.arg1 = arg1;
	}

	public void setArg2(int arg2) {
		this.arg2 = arg2;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public Bundle getBundle() {
		return bundle;
	}

	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}
}
