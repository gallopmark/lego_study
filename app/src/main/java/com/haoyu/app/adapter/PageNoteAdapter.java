package com.haoyu.app.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.NoteEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2016/11/15 on 10:20
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class PageNoteAdapter extends BaseArrayRecyclerAdapter<NoteEntity> {

    public PageNoteAdapter(List<NoteEntity> dataList) {
        super(dataList);
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, NoteEntity entity, int position) {
        TextView topLine = holder.obtainView(R.id.topLine);
        TextView tv_time = holder.obtainView(R.id.time);
        TextView tv_date = holder.obtainView(R.id.date);
        ImageView ic_circle = holder.obtainView(R.id.ic_circle);
        TextView tv_content = holder.obtainView(R.id.content);
        TextView tv_detail = holder.obtainView(R.id.detail);
        if(position == 0){
            topLine.setVisibility(View.INVISIBLE);
        } else {
            topLine.setVisibility(View.VISIBLE);
        }
        tv_content.setText(entity.getContent());
        tv_time.setText(TimeUtil.getTime(entity.getCreateTime()));
        tv_date.setText(TimeUtil.getDate(entity.getCreateTime()));
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.note_item;
    }
}
