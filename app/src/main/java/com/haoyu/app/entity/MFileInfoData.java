package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/10/13 on 16:22
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MFileInfoData {
    @Expose
    @SerializedName("mFileInfos")
    private List<MFileInfo> mFileInfos;
    @Expose
    @SerializedName("paginator")
    private Paginator paginator;

    public List<MFileInfo> getmFileInfos() {
        if (mFileInfos == null)
            mFileInfos = new ArrayList<>();
        return mFileInfos;
    }

    public void setmFileInfos(List<MFileInfo> mFileInfos) {
        this.mFileInfos = mFileInfos;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }
}
