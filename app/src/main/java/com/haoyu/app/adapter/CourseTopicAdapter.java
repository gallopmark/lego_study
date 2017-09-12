package com.haoyu.app.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.haoyu.app.basehelper.AppBaseAdapter;
import com.haoyu.app.basehelper.ViewHolder;
import com.haoyu.app.entity.CourseTopic;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.PixelFormat;

import java.util.List;

/**
 * 创建日期：2017/5/19 on 13:54
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CourseTopicAdapter extends AppBaseAdapter<CourseTopic> {
    private Context context;
    private int selectItem;

    public CourseTopicAdapter(Context context, List<CourseTopic> mDatas) {
        super(context, mDatas);
    }

    public CourseTopicAdapter(Context context, List<CourseTopic> mDatas, int selectItem) {
        super(context, mDatas);
        this.context = context;
        this.selectItem = selectItem;
    }

    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
        notifyDataSetChanged();
    }

    @Override
    public void convert(ViewHolder holder, CourseTopic entity, int position) {
        TextView tvDict = holder.getView(R.id.tvDict);
        tvDict.setText(entity.getTitle());
        Drawable select = ContextCompat.getDrawable(context,
                R.drawable.train_item_selected);
        select.setBounds(0, 0, select.getMinimumWidth(),
                select.getMinimumHeight());
        if (selectItem == position) {
            tvDict.setPressed(true);
            tvDict.setCompoundDrawables(null, null, select, null);
            tvDict.setCompoundDrawablePadding(PixelFormat.formatPxToDip(context, 10));
        } else {
            tvDict.setPressed(false);
            tvDict.setCompoundDrawables(null, null, null, null);
        }
    }

    @Override
    public int getmItemLayoutId() {
        return R.layout.popupwindow_dictionary_item;
    }
}
