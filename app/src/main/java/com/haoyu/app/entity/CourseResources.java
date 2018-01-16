package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/7 on 16:40
 * 描述: 课程资源列表
 * 作者:马飞奔 Administrator
 */
public class CourseResources extends BaseResponseResult<CourseResources.MData> {

    public class MData {
        @SerializedName("resources")
        @Expose
        private List<ResourcesEntity> resources;
        @SerializedName("paginator")
        @Expose
        private Paginator paginator;

        public List<ResourcesEntity> getResources() {
            if(resources == null){
                return new ArrayList<>();
            }
            return resources;
        }

        public void setResources(List<ResourcesEntity> resources) {
            this.resources = resources;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
