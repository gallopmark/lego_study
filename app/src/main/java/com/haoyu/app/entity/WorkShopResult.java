package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/3 on 14:13
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class WorkShopResult extends BaseResponseResult<WorkShopResult.MData> {

    public class MData {
        /**
         * mWorkshop	工作坊	MWorkshop	Y
         * mWorkshopUser	工作坊用户	MWorkshopUser	Y
         * mWorkshopSections	阶段列表	List	Y
         */
        @Expose
        @SerializedName("mWorkshop")
        private WorkShopMobileEntity mWorkshop;
        @Expose
        @SerializedName("mWorkshopUser")
        private WorkShopMobileUser mWorkshopUser;
        @Expose
        @SerializedName("mWorkshopSections")
        private List<MWorkshopSection> mWorkshopSections = new ArrayList<>();

        public WorkShopMobileEntity getmWorkshop() {
            return mWorkshop;
        }

        public void setmWorkshop(WorkShopMobileEntity mWorkshop) {
            this.mWorkshop = mWorkshop;
        }

        public WorkShopMobileUser getmWorkshopUser() {
            return mWorkshopUser;
        }

        public void setmWorkshopUser(WorkShopMobileUser mWorkshopUser) {
            this.mWorkshopUser = mWorkshopUser;
        }

        public List<MWorkshopSection> getmWorkshopSections() {
            if (mWorkshopSections == null) {
                return new ArrayList<>();
            }
            return mWorkshopSections;
        }

        public void setmWorkshopSections(List<MWorkshopSection> mWorkshopSections) {
            this.mWorkshopSections = mWorkshopSections;
        }
    }
}
