package com.haoyu.app.basehelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * 创建日期：2016/12/5 on 10:53
 * 描述:
 * 作者:马飞奔 Administrator
 */
public abstract class AppBaseAdapter<T> extends BaseAdapter {
    protected LayoutInflater mInflater;
    protected Context mContext;
    protected List<T> mDatas;

    public AppBaseAdapter(Context context, List<T> mDatas) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDatas = mDatas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder = getViewHolder(position, convertView,
                parent);
        convert(viewHolder, getItem(position), position);
        return viewHolder.getConvertView();

    }

    public abstract void convert(ViewHolder holder, T entity, int position);

    private ViewHolder getViewHolder(int position, View convertView,
                                     ViewGroup parent) {
        return ViewHolder.get(mContext, convertView, parent, getmItemLayoutId(),
                position);
    }

    public abstract int getmItemLayoutId();
}
