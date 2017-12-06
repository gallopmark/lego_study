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

public class WSTSEvaluateAdapter extends BaseArrayRecyclerAdapter<MEvaluateItem> {

    public WSTSEvaluateAdapter(List<MEvaluateItem> mDatas) {
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
        starBar.setEnabled(false);
        starBar.setCanEdit(false);
        tv_content.setText(item.getContent());
        starBar.setStarMark((float) (item.getAvgScore() / 20));
        tv_score.setText((int) item.getAvgScore() + "分");
    }
}