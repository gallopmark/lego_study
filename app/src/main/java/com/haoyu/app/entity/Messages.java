package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/8 on 15:42
 * 描述: 消息列表结果集
 * 作者:马飞奔 Administrator
 */
public class Messages extends BaseResponseResult<Messages.MData> {

    public class MData {
        @Expose
        @SerializedName("mMessages")
        private List<Message> mMessages = new ArrayList<>();
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<Message> getmMessages() {
            return mMessages;
        }

        public void setmMessages(List<Message> mMessages) {
            this.mMessages = mMessages;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
