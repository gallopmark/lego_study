package com.haoyu.app.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.haoyu.app.basehelper.AppBaseAdapter;
import com.haoyu.app.basehelper.ViewHolder;
import com.haoyu.app.entity.MDepartment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.PixelFormat;

import java.util.List;

/**
 * 创建日期：2017/3/24 on 11:15
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class DepartmentAdapter extends AppBaseAdapter<MDepartment> {
    private Context context;
    private int selectItem = -1;

    public DepartmentAdapter(Context context, List<MDepartment> mDatas) {
        super(context, mDatas);
        this.context = context;
    }

    public DepartmentAdapter(Context context, List<MDepartment> mDatas, int selectItem) {
        super(context, mDatas);
        this.context = context;
        this.selectItem = selectItem;
    }

    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
        notifyDataSetChanged();
    }

    @Override
    public void convert(ViewHolder holder, MDepartment entity, int position) {
        TextView tvDict = holder.getView(R.id.tvDict);
        tvDict.setText(entity.getDeptName());
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
