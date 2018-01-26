package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

/**
 * 创建日期：2017/1/4 on 10:25
 * 描述: 工作坊简介结果集
 * 作者:马飞奔 Administrator
 */
public class WSDetailResult extends BaseResponseResult<WSDetailResult.MData> {

    public class MData {
        @Expose
        @SerializedName("mWorkshop")
        private WorkShopMobileEntity mWorkshop;
        @Expose
        @SerializedName("mFileInfo")
        private MFileInfo mFileInfo;

        public WorkShopMobileEntity getmWorkshop() {
            return mWorkshop;
        }

        public void setmWorkshop(WorkShopMobileEntity mWorkshop) {
            this.mWorkshop = mWorkshop;
        }

        public MFileInfo getmFileInfo() {
            return mFileInfo;
        }

        public void setmFileInfo(MFileInfo mFileInfo) {
            this.mFileInfo = mFileInfo;
        }
    }
}
