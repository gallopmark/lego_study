package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * 创建日期：2016/11/29 on 17:12
 * 描述:id	资源id	String	Y
 * title	资源标题	String	Y
 * fileInfos	资源文件列表	List<MFileInfo>	Y	MFileInfo详见公共对象
 * 作者:马飞奔 Administrator
 */
public class ResourcesEntity implements Serializable {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("fileInfos")
    private List<MFileInfo> fileInfos;
    @Expose
    @SerializedName("mFileInfos")
    private List<MFileInfo> mFileInfos;

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

    public List<MFileInfo> getFileInfos() {
        return fileInfos;
    }

    public void setFileInfos(List<MFileInfo> fileInfos) {
        this.fileInfos = fileInfos;
    }

    public List<MFileInfo> getmFileInfos() {
        return mFileInfos;
    }

    public void setmFileInfos(List<MFileInfo> mFileInfos) {
        this.mFileInfos = mFileInfos;
    }
}
