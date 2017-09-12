package com.haoyu.app.basehelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2016/12/7 on 9:37
 * 描述:
 * 作者:马飞奔 Administrator
 */
public abstract class BaseArrayRecyclerAdapter<T> extends BaseRecyclerAdapter {
    public  List<T> mDatas = new ArrayList<T>();

    public BaseArrayRecyclerAdapter(List<T> mDatas) {
        this.mDatas = mDatas;
    }

    public List<T> getData() {
        return mDatas;
    }

    public T getData(int position) {
        if (position < 0 || position >= mDatas.size()) return null;
        return mDatas.get(position);
    }


    public boolean bindData(boolean isRefresh, List<T> datas) {
        if (datas == null) return false;
        if (isRefresh) mDatas.clear();
        boolean b = mDatas.addAll(datas);
        notifyDataSetChanged();
        return b;
    }

    public void clearData() {
        mDatas.clear();
        notifyDataSetChanged();
    }

    @Override
    public T getItem(int position) {
        if (position < 0 || position >= mDatas.size()) return null;
        return mDatas.get(position);
    }

    public boolean addItem(int position, T t) {
        if (t == null) return false;
        if (position < 0 || position > mDatas.size()) return false;
        if (mDatas.contains(t)) return false;
        mDatas.add(position, t);
        notifyItemInserted(position);
        return true;
    }

    public boolean addItems(int pos, List<? extends T> datas) {
        if (datas == null) return false;
        if (datas.contains(datas)) return false;
        mDatas.addAll(pos, datas);
        notifyItemRangeInserted(pos, datas.size());
        return true;
    }

    public boolean addItems(List<? extends T> datas) {
        if (datas == null) return false;
        if (datas.contains(datas)) return false;
        mDatas.addAll(datas);
        notifyItemRangeInserted(getItemCount() - datas.size() >= 0 ? getItemCount() - datas.size() : 0, datas.size());
        return true;
    }

    public boolean addItem(T t) {
        if (t == null) return false;
        if (mDatas.contains(t)) return false;
        boolean b = mDatas.add(t);
        notifyItemInserted(mDatas.size() - 1);
        return b;
    }


    public boolean updateItem(int position) {
        if (position < 0 || position >= mDatas.size()) return false;
        notifyItemChanged(position);
        return true;
    }

    public boolean updateItem(T t) {
        if (t == null) return false;
        int index = mDatas.indexOf(t);
        if (index >= 0) {
            mDatas.set(index, t);
            notifyItemChanged(index);
            return true;
        }
        return false;
    }

    public boolean updateItem(int position, T t) {
        if (position < 0 || position >= mDatas.size()) return false;
        if (t == null) return false;
        mDatas.set(position, t);
        notifyItemChanged(position);
        return true;
    }

    public boolean removeItem(int position) {
        if (position < 0 || position >= mDatas.size()) return false;
        mDatas.remove(position);
        notifyItemRemoved(position);
        return true;
    }

    public boolean removeItem(T t) {
        if (t == null) return false;
        int index = mDatas.indexOf(t);
        if (index >= 0) {
            mDatas.remove(index);
            notifyItemRemoved(index);
            return true;
        }
        return false;
    }


    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        onBindHoder(holder, getData(position), position);
    }

    public abstract void onBindHoder(RecyclerHolder holder, T t, int position);

    @Override
    public int getItemCount() {
        return mDatas.size();
    }


}
