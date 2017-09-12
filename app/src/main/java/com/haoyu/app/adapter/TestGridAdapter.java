package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.ScreenUtils;

/**
 * 创建日期：2016/12/1 on 17:05
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class TestGridAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private Context context;
    private ArrayMap<Integer, Boolean> arrayMap;

    public TestGridAdapter(Context context, ArrayMap<Integer, Boolean> arrayMap) {
        this.context = context;
        this.arrayMap = arrayMap;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return arrayMap.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayMap.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.test_menu_grid_item, viewGroup, false);
            holder.tv = view.findViewById(R.id.tv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (arrayMap.get(i) == true) {
            holder.tv.setBackgroundResource(R.drawable.test_page_done);
        } else {
            holder.tv.setBackgroundResource(R.drawable.test_page_undone);
        }
        holder.tv.setText(String.valueOf(i + 1));
        holder.tv.setWidth(ScreenUtils.getScreenWidth(context) / 7);
        holder.tv.setHeight(ScreenUtils.getScreenWidth(context) / 7);
        return view;
    }

    static class ViewHolder {
        TextView tv;
    }
}
