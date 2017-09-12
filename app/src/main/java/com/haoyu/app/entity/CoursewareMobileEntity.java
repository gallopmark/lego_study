package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2016/12/28 on 17:53
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CoursewareMobileEntity implements Serializable {
    /**
     * id	课件ID	String	Y
     viewNum	已观看次数	Int	Y
     interval	计时间隔	int	Y	单位:秒(如12,则进入课件后过12秒才访问更新观看次数的url)
     type	课件类型	String	Y	file:pdf
     link:外链
     editor:文本
     photo:3D图形(先不做)
     content	内容或外链地址	String	Y	课件类型为link时一个地址
     课件类型为editor时返回一段html
     pdfUrl	Pdf文件地址	String	Y	课件类型为pdf时返回
     viewNum	要求观看次数	Int	Y

     */
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("viewNum")
    private int viewNum;
    @Expose
    @SerializedName("interval")
    private int interval;
    @Expose
    @SerializedName("type")
    private String type;
    @Expose
    @SerializedName("content")
    private String content;
    @Expose
    @SerializedName("pdfUrl")
    private String pdfUrl;
    @Expose
    @SerializedName("viewNumber")
    private int viewNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getViewNum() {
        return viewNum;
    }

    public void setViewNum(int viewNum) {
        this.viewNum = viewNum;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public int getViewNumber() {
        return viewNumber;
    }

    public void setViewNumber(int viewNumber) {
        this.viewNumber = viewNumber;
    }
}
