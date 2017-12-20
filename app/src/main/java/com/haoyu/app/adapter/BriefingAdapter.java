package com.haoyu.app.adapter;

import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.BriefingEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2017/1/5 on 14:31
 * 描述: 工作坊简报适配器
 * 作者:马飞奔 Administrator
 */
public class BriefingAdapter extends BaseArrayRecyclerAdapter<BriefingEntity> {

    public BriefingAdapter(List<BriefingEntity> mDatas) {
        super(mDatas);
    }


    @Override
    public void onBindHoder(RecyclerHolder holder, final BriefingEntity entity, final int position) {
        TextView tv_title = holder.obtainView(R.id.tv_title);
        TextView tv_time = holder.obtainView(R.id.tv_time);
        tv_title.setText(entity.getTitle());
        tv_time.setText(TimeUtil.getSlashDate(entity.getCreateTime()));
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.brief_item;
    }

}
