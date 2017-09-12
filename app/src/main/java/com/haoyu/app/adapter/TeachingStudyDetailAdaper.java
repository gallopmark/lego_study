package com.haoyu.app.adapter;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;

import com.haoyu.app.entity.EvaluateItemResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.StarBar;

import java.util.List;

/**
 * Created by acer1 on 2017/2/22.
 * 听课评课评评价内容列表
 */
public class TeachingStudyDetailAdaper extends BaseArrayRecyclerAdapter<EvaluateItemResult.EvaluateItemResponse> {
    public TeachingStudyDetailAdaper(List<EvaluateItemResult.EvaluateItemResponse> mDatas) {
        super(mDatas);
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, EvaluateItemResult.EvaluateItemResponse entity, int position) {
        TextView tv_content = holder.obtainView(R.id.tv_content);
        TextView tv_score = holder.obtainView(R.id.tv_score);
        RelativeLayout rl_view = holder.obtainView(R.id.rl_view);
        final StarBar ratingBar1 = holder.obtainView(R.id.ratingBar1);
        ratingBar1.setClickable(false);
        ratingBar1.setCanEdit(false);

        if (entity != null) {
            if (entity.getContent() != null) {
                tv_content.setText(entity.getContent());
            } else {
                tv_content.setText("暂无内容");
            }
            ratingBar1.setStarMark((float) entity.getAvgScore() / 20);
            tv_score.setText((int) entity.getAvgScore() + "分");
        } else {
            tv_score.setText("0分");
        }

    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.teaching_study_evaluate_content;
    }



}
