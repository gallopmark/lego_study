package com.haoyu.app.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.FAQsEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2016/11/15 on 10:27
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class PageQuestionAdapter extends BaseArrayRecyclerAdapter<FAQsEntity> {
    private Context mContext;
    private int type;
    private CollectCallBack collectCallBack;
    private AnswerCallBack answerCallBack;

    public PageQuestionAdapter(Context context, List<FAQsEntity> mDatas, int type) {
        super(mDatas);
        this.mContext = context;
        this.type = type;
    }

    public void setCollectCallBack(CollectCallBack collectCallBack) {
        this.collectCallBack = collectCallBack;
    }

    public void setAnswerCallBack(AnswerCallBack answerCallBack) {
        this.answerCallBack = answerCallBack;
    }


    @Override
    public int bindView(int viewtype) {
        return R.layout.page_question_answer_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final FAQsEntity entity, final int position) {
        ImageView userIco = holder.obtainView(R.id.userIco);
        TextView userName = holder.obtainView(R.id.userName);
        ImageView ic_question = holder.obtainView(R.id.ic_question);
        TextView question = holder.obtainView(R.id.tv_question);
        ImageView ic_answer = holder.obtainView(R.id.ic_answer);
        TextView answer = holder.obtainView(R.id.tv_answerContent);
        TextView answerDate = holder.obtainView(R.id.tv_answerDate);
        final TextView tv_collection = holder.obtainView(R.id.tv_collection);
        final ImageView iv_collection = holder.obtainView(R.id.iv_collection);
        TextView ThumbCount = holder.obtainView(R.id.tv_ThumbCount);
        TextView tv_answer = holder.obtainView(R.id.tv_answer);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, (int) (0.22D * question.getLineHeight()), 0,
                0);
        ic_question.setLayoutParams(params);
        params.setMargins(0, (int) (0.22D * answer.getLineHeight()), 0, 0);
        ic_answer.setLayoutParams(params);
        if (entity.getCreator() != null && entity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(mContext, entity.getCreator().getAvatar()
                    , R.drawable.user_default, R.drawable.user_default, userIco);
        } else {
            userIco.setImageResource(R.drawable.user_default);
        }
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            userName.setText(entity.getCreator().getRealName());
        } else {
            userName.setText("匿名用户");
        }
        question.setText(entity.getContent());
        if (entity.getFaqAnswers() != null
                && (entity.getFaqAnswers().size() > 0)) {
            answer.setText(entity.getFaqAnswers().get(0).getContent());
        } else {
            answer.setText("暂无最佳答案");
        }
        if (entity.getCreateTime() != null) {
            answerDate.setText(TimeUtil.getSlashDate(entity
                    .getCreateTime().longValue()));
        }
        if (type == 1)
            tv_collection.setVisibility(View.VISIBLE);
        else
            tv_collection.setVisibility(View.GONE);
        if (type == 2)
            iv_collection.setVisibility(View.VISIBLE);
        else
            iv_collection.setVisibility(View.GONE);
        if (entity.getFollow() != null) {
            tv_collection.setText("取消收藏");
        } else {
            tv_collection.setText("收藏");
        }
        if (entity.getFollow() != null) {
            iv_collection.setImageResource(R.drawable.workshop_collect_press);
        } else {
            iv_collection.setImageResource(R.drawable.workshop_collect_default);
        }
        ThumbCount.setText(String.valueOf(entity.getFaqAnswerCount()));
        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tv_collection:
                        if (entity.getFollow() != null) {// 如果已经收藏，点击按钮则取消收藏
                            if (collectCallBack != null)
                                collectCallBack.cancelCollect(position, entity);
                        } else {  // 如果未收藏，则创建收藏
                            if (collectCallBack != null)
                                collectCallBack.collect(position, entity);
                        }
                        break;
                    case R.id.iv_collection:
                        if (entity.getFollow() != null) {// 如果已经收藏，点击按钮则取消收藏
                            if (collectCallBack != null)
                                collectCallBack.cancelCollect(position, entity);
                        } else {  // 如果未收藏，则创建收藏
                            if (collectCallBack != null)
                                collectCallBack.collect(position, entity);
                        }
                        break;
                    case R.id.tv_answer: // 点击我来回答
                        if (answerCallBack != null) {
                            answerCallBack.answer(position, entity);
                        }
                        break;
                }
            }
        };
        tv_collection.setOnClickListener(listener);
        iv_collection.setOnClickListener(listener);
        tv_answer.setOnClickListener(listener);
    }

    public interface CollectCallBack {
        void collect(int position, FAQsEntity entity);

        void cancelCollect(int position, FAQsEntity entity);
    }

    public interface AnswerCallBack {
        void answer(int position, FAQsEntity entity);
    }
}
