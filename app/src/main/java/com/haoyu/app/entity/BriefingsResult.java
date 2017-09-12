package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.io.Serializable;
import java.util.List;

/**
 * 创建日期：2017/1/5 on 14:11
 * 描述: 工作坊简介简报列表结果集
 * 作者:马飞奔 Administrator
 */
public class BriefingsResult extends BaseResponseResult<BriefingsResult.WorkShopBriefResponseData> {

    public class WorkShopBriefResponseData implements Serializable {
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;
        @Expose
        @SerializedName("announcements")
        private List<BriefingEntity> announcements;

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }

        public List<BriefingEntity> getAnnouncements() {
            return announcements;
        }

        public void setAnnouncements(List<BriefingEntity> announcements) {
            this.announcements = announcements;
        }
    }
}
