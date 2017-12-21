package com.haoyu.app.entity;

import com.haoyu.app.base.BaseResponseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/1/3 on 20:51
 * 描述:工作坊活动列表
 * 作者:马飞奔 Administrator
 */
public class WSActivities extends BaseResponseResult<List<MWorkshopActivity>> {

    @Override
    public List<MWorkshopActivity> getResponseData() {
        if (responseData == null) {
            return new ArrayList<>();
        }
        return responseData;
    }

    public void setResponseData(List<MWorkshopActivity> responseData) {
        this.responseData = responseData;
    }
}
