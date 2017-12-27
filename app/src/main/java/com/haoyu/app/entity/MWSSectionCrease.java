package com.haoyu.app.entity;

/**
 * 创建日期：2017/12/22.
 * 描述:
 * 作者:xiaoma
 */

public class MWSSectionCrease implements MultiItemEntity {

    private MWorkshopSection tag;

    public MWorkshopSection getTag() {
        return tag;
    }

    public void setTag(MWorkshopSection tag) {
        this.tag = tag;
    }

    @Override
    public int getItemType() {
        return 4;
    }
}
