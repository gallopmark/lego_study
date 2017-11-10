package com.haoyu.app.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.TeachingLessonEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.RoundRectProgressBar;

import java.util.List;

/**
 * 创建日期：2017/1/11 on 9:35
 * 描述: 社区创课列表适配器
 * 作者:马飞奔 Administrator
 */
public class CtmsLessonAdapter extends BaseArrayRecyclerAdapter<TeachingLessonEntity> {
    private Context mContext;
    private RequestClickCallBack requestClickCallBack;

    public CtmsLessonAdapter(Context context, List<TeachingLessonEntity> mDatas) {
        super(mDatas);
        this.mContext = context;
    }

    public void setRequestClickCallBack(RequestClickCallBack requestClickCallBack) {
        this.requestClickCallBack = requestClickCallBack;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final TeachingLessonEntity entity, final int position) {
        TextView tv_title = holder.obtainView(R.id.tv_title);
        TextView tv_period = holder.obtainView(R.id.tv_period);
        TextView tv_content = holder.obtainView(R.id.tv_content);
        ImageView ic_user = holder.obtainView(R.id.ic_user);
        TextView tv_userName = holder.obtainView(R.id.tv_userName);
        TextView tv_createTime = holder.obtainView(R.id.tv_createTime);
        TextView tv_Heat = holder.obtainView(R.id.tv_Heat);
        View ll_support = holder.obtainView(R.id.ll_support);
        final TextView tv_support = holder.obtainView(R.id.tv_support);
        View ll_advise = holder.obtainView(R.id.ll_advise);
        final TextView tv_advise = holder.obtainView(R.id.tv_advise);
        RoundRectProgressBar mRrogressBar = holder.obtainView(R.id.mRrogressBar);
        TextView tv_day = holder.obtainView(R.id.tv_day);
        tv_createTime.setText(TimeUtil.converTime(entity.getCreateTime()));
        tv_title.setText(entity.getTitle());
        if (entity.getContent() != null) {
            Spanned spanned = Html.fromHtml(entity.getContent());
            tv_content.setText(spanned);
        } else {
            tv_content.setText(null);
        }
        if (entity.getCreator() != null && entity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(mContext, entity.getCreator().getAvatar()
                    , R.drawable.user_default, R.drawable.user_default, ic_user);
        } else {
            ic_user.setImageResource(R.drawable.user_default);
        }
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            tv_userName.setText(entity.getCreator().getRealName());
        } else {
            tv_userName.setText("");
        }
        if (entity.getmDiscussionRelations() != null && entity.getmDiscussionRelations().size() > 0) {
            tv_Heat.setText(String.valueOf(entity.getmDiscussionRelations().get(0).getBrowseNum()));
            tv_support.setText(String.valueOf(entity.getmDiscussionRelations().get(0).getSupportNum()));
            tv_advise.setText(String.valueOf(entity.getmDiscussionRelations().get(0).getReplyNum()));
        } else {
            tv_Heat.setText(String.valueOf(0));
            tv_support.setText(String.valueOf(0));
            tv_advise.setText(String.valueOf(0));
        }
        mRrogressBar.setMax(60);
        mRrogressBar.setProgress(60 - entity.getRemainDay());
        if (entity.getRemainDay() <= 0) {
            tv_day.setText("已结束");
        } else {
            tv_day.setText("还剩" + entity.getRemainDay() + "天");
        }
        if (entity.getRemainDay() <= 0) {
            tv_period.setVisibility(View.VISIBLE);
        } else {
            tv_period.setVisibility(View.GONE);
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.ll_support:
                        if (requestClickCallBack != null)
                            requestClickCallBack.support(entity, position);
                        break;
                    case R.id.ll_advise:
                        if (requestClickCallBack != null)
                            requestClickCallBack.giveAdvice(entity, position);
                        break;
                }
            }
        };
        ll_support.setOnClickListener(listener);
        ll_advise.setOnClickListener(listener);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.teaching_research_class_item;
    }

    public interface RequestClickCallBack {
        void support(TeachingLessonEntity entity, int position);

        void giveAdvice(TeachingLessonEntity entity, int position);
    }
}
