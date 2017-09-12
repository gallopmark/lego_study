package com.haoyu.app.adapter;

import android.view.View;
import android.widget.TextView;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;

import com.haoyu.app.entity.MEvaluateItemSubmissions;

import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.StarBar;

import java.util.List;

/**
 * Created by acer1 on 2017/2/21.
 * <p>
 * 填写听课评课
 */
public class TeachStudyEvaluateAdapter extends BaseArrayRecyclerAdapter<MEvaluateItemSubmissions> {
    public TeachStudyEvaluateAdapter(List<MEvaluateItemSubmissions> mDatas) {
        super(mDatas);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.teaching_study_evaluate_content;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final MEvaluateItemSubmissions entity, int position) {
        TextView tv_content = holder.obtainView(R.id.tv_content);
        final TextView tv_score = holder.obtainView(R.id.tv_score);
        final StarBar ratingBar1 = holder.obtainView(R.id.ratingBar1);

        if (entity != null) {
            if (entity.getContent() != null) {
                tv_content.setText(entity.getContent());
            } else {
                tv_content.setText("暂无内容");
            }

        }
        //获取评价得到的分数
        ratingBar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_score.setText(String.valueOf((int) (ratingBar1.getStarMark() * 20) + "分"));
                evaluateItem.evaluateItem(entity.getId(), ratingBar1.getStarMark() * 20);
            }
        });


    }
    public interface EvaluateItem {
        void evaluateItem(String id, double score);
    }

    public EvaluateItem evaluateItem;

    public void setEvaluateItem(EvaluateItem evaluateItem) {
        this.evaluateItem = evaluateItem;
    }

}
