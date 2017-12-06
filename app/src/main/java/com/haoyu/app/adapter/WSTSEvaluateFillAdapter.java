package com.haoyu.app.adapter;

import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.MEvaluateItem;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.StarBar;

import java.util.List;

/**
 * 创建日期：2017/12/6.
 * 描述:工作坊听课评课评价项
 * 作者:xiaoma
 */

public class WSTSEvaluateFillAdapter extends BaseArrayRecyclerAdapter<MEvaluateItem> {
    private OnItemStarChangeListener onItemStarChangeListener;

    public WSTSEvaluateFillAdapter(List<MEvaluateItem> mDatas) {
        super(mDatas);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.wsts_evaluateitem;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final MEvaluateItem item, int position) {
        TextView tv_content = holder.obtainView(R.id.tv_content);
        final StarBar starBar = holder.obtainView(R.id.starBar);
        final TextView tv_score = holder.obtainView(R.id.tv_score);
        tv_content.setText(item.getContent());
        tv_score.setText((int) item.getAvgScore() + "分");
        tv_score.setCompoundDrawables(null, null, null, null);
        starBar.setOnStarChangeListener(new StarBar.OnStarChangeListener() {
            @Override
            public void onStarChange(float mark) {
                int score = (int) (mark * 20);
                tv_score.setText(score + "分");
                if (onItemStarChangeListener != null) {
                    onItemStarChangeListener.onItemStarChange(item.getId(), score);
                }
            }
        });
    }

    public interface OnItemStarChangeListener {
        void onItemStarChange(String id, int score);
    }

    public void setOnItemStarChangeListener(OnItemStarChangeListener onItemStarChangeListener) {
        this.onItemStarChangeListener = onItemStarChangeListener;
    }
}