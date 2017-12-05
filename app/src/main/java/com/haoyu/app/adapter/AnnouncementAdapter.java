package com.haoyu.app.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.Announcement;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2017/1/5 on 17:36
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AnnouncementAdapter extends BaseArrayRecyclerAdapter<Announcement> {
    public AnnouncementAdapter(List<Announcement> mDatas) {
        super(mDatas);
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, Announcement announcementEntity, int position) {
        ImageView ic_consult = holder.obtainView(R.id.ic_consult);
        TextView tv_title = holder.obtainView(R.id.tv_title);
        TextView tv_createDate = holder.obtainView(R.id.tv_createDate);
        if (announcementEntity.isHadView()) {
            ic_consult.setVisibility(View.GONE);
        } else {
            ic_consult.setVisibility(View.VISIBLE);
        }
        tv_title.setText(announcementEntity.getTitle());
        tv_createDate.setText(TimeUtil.getSlashDate(announcementEntity.getCreateTime()));
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.announcements_item;
    }
}
