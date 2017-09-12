package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/1/8 on 14:53
 * 描述: 消息实体类
 * id	消息id	String	Y
 * title	标题	String	Y
 * content	内容	String	Y
 * type	类型	String	Y	系统消息：“system_message”
 * 小纸条：“user_message”
 * sender	发送人	MUser	Y	MUser详见公共对象
 * receiver	接收人	MUser	Y	MUser详见公共对象
 * <p>
 * <p>
 * 作者:马飞奔 Administrator
 */
public class Message implements Serializable {
    public static String TYPE_SYSTEM = "system_message";
    public static String TYPE_USER = "user_message";
    public static String TYPE_DAILY_WARN = "study_daily_warn";
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("content")
    private String content;
    @Expose
    @SerializedName("type")
    private String type;
    @Expose
    @SerializedName("createTime")
    private long createTime;
    @Expose
    @SerializedName("sender")
    private MobileUser sender;
    @Expose
    @SerializedName("receiver")
    private MobileUser receiver;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MobileUser getSender() {
        return sender;
    }

    public void setSender(MobileUser sender) {
        this.sender = sender;
    }

    public MobileUser getReceiver() {
        return receiver;
    }

    public void setReceiver(MobileUser receiver) {
        this.receiver = receiver;
    }
}
