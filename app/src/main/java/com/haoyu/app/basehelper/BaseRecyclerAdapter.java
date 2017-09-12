package com.haoyu.app.basehelper;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 创建日期：2016/12/7 on 9:34
 * 描述:
 * 作者:马飞奔 Administrator
 */
public abstract class BaseRecyclerAdapter extends RecyclerView.Adapter<BaseRecyclerAdapter.RecyclerHolder> {


    public BaseRecyclerAdapter() {
    }

    public abstract int bindView(int viewtype);

    public Object getItem(int position) {
        return null;
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        return new RecyclerHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(bindView(viewtype), viewGroup, false));
    }

    public class RecyclerHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private SparseArray<View> holder = null;

        public RecyclerHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        /**
         * 获取子控件
         *
         * @param id
         * @param <T>
         * @return
         */
        public <T extends View> T obtainView(int id) {
            if (null == holder) holder = new SparseArray<>();
            View view = holder.get(id);
            if (null != view) return (T) view;
            view = itemView.findViewById(id);
            if (null == view) return null;
            holder.put(id, view);
            return (T) view;
        }


        public <T> T obtainView(int id, Class<T> viewClazz) {
            View view = obtainView(id);
            if (null == view) return null;
            return (T) view;
        }


        public boolean bindChildClick(int id) {
            View view = obtainView(id);
            if (view == null) return false;
            view.setOnClickListener(this);
            return true;
        }

        /**
         * 子控件绑定局部点击事件
         *
         * @param v
         * @return
         */
        public boolean bindChildClick(View v) {
            if (v == null) return false;
            if (obtainView(v.getId()) == null)
                return false;
            v.setOnClickListener(this);
            return true;
        }


        public boolean bindChildLongClick(int id) {
            View view = obtainView(id);
            if (view == null) return false;
            view.setOnLongClickListener(this);
            return true;
        }

        public boolean bindChildLongClick(View v) {
            if (v == null) return false;
            if (obtainView(v.getId()) == null)
                return false;
            v.setOnLongClickListener(this);
            return true;
        }

        /**
         * 文本控件赋值
         *
         * @param id
         * @param text
         */
        public void setText(int id, CharSequence text) {
            View view = obtainView(id);
            if (view instanceof TextView) {
                ((TextView) view).setText(text);
            }
        }


        @Override
        public boolean onLongClick(View v) {
            if (onItemLongClickListener != null && v.getId() == this.itemView.getId()) {
                onItemLongClickListener.onItemLongClick(BaseRecyclerAdapter.this, this, v, getAdapterPosition());
                return true;
            } else if (onItemChildLongClickListener != null && v.getId() != this.itemView.getId()) {
                onItemChildLongClickListener.onItemChildLongClick(BaseRecyclerAdapter.this, this, v, getAdapterPosition());
                return true;
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null && v.getId() == this.itemView.getId()) {
                onItemClickListener.onItemClick(BaseRecyclerAdapter.this, this, v, getAdapterPosition());
            } else if (onItemChildClickListener != null && v.getId() != this.itemView.getId()) {
                onItemChildClickListener.onItemChildClick(BaseRecyclerAdapter.this, this, v, getAdapterPosition());
            }
        }
    }

    protected OnItemClickListener onItemClickListener;

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    protected OnItemLongClickListener onItemLongClickListener;

    public OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }


    public interface OnItemClickListener {
        void onItemClick(BaseRecyclerAdapter adapter, RecyclerHolder holder, View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(BaseRecyclerAdapter adapter, RecyclerHolder holder, View view, int position);
    }


    public interface OnItemChildClickListener {
        void onItemChildClick(BaseRecyclerAdapter adapter, RecyclerHolder holder, View view, int position);
    }

    public interface OnItemChildLongClickListener {
        void onItemChildLongClick(BaseRecyclerAdapter adapter, RecyclerHolder holder, View view, int position);
    }


    protected OnItemChildClickListener onItemChildClickListener;
    protected OnItemChildLongClickListener onItemChildLongClickListener;

    public OnItemChildLongClickListener getOnItemChildLongClickListener() {
        return onItemChildLongClickListener;
    }

    public void setOnItemChildLongClickListener(OnItemChildLongClickListener onItemChildLongClickListener) {
        this.onItemChildLongClickListener = onItemChildLongClickListener;
    }

    public OnItemChildClickListener getOnItemChildClickListener() {
        return onItemChildClickListener;
    }

    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        this.onItemChildClickListener = onItemChildClickListener;
    }

}
