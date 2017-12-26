package com.haoyu.app.entity;

/**
 * 创建日期：2017/12/22.
 * 描述:工作坊添加阶段任务
 * 作者:xiaoma
 */

public class MWSActivityCrease implements MultiItemEntity {
    private MWorkshopSection tag;
    private boolean visible;

    public MWorkshopSection getTag() {
        return tag;
    }

    public void setTag(MWorkshopSection tag) {
        this.tag = tag;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public int getItemType() {
        return 3;
    }
}
