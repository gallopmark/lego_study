package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/4 on 10:39
 * 描述: 工作坊优秀学员结果集
 * 作者:马飞奔 Administrator
 */
public class WSExecllentUsers extends BaseResponseResult<WSExecllentUsers.MData> {

    public class MData {
        @Expose
        @SerializedName("mWorkshopUsers")
        private List<WorkShopMobileUser> mWorkshopUsers;
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<WorkShopMobileUser> getmWorkshopUsers() {
            if (mWorkshopUsers == null) {
                return new ArrayList<>();
            }
            return mWorkshopUsers;
        }

        public void setmWorkshopUsers(List<WorkShopMobileUser> mWorkshopUsers) {
            this.mWorkshopUsers = mWorkshopUsers;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
