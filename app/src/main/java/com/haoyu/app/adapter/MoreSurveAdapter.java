package com.haoyu.app.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.SurveyAnswerSubmission;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.imageloader.GlideImgManager;

import java.util.List;

/**
 * Created by acer1 on 2017/3/21.
 */
public class MoreSurveAdapter extends BaseArrayRecyclerAdapter<SurveyAnswerSubmission> {
    private Context context;

    public MoreSurveAdapter(Context context, List<SurveyAnswerSubmission> mDatas) {
        super(mDatas);
        this.context = context;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, SurveyAnswerSubmission surveyAnswerSubmission, int position) {
        ImageView ic_user = holder.obtainView(R.id.ic_user);
        TextView tv_userName = holder.obtainView(R.id.tv_userName);
        TextView tv_content = holder.obtainView(R.id.tv_content);
        if (surveyAnswerSubmission.getResponse() != null) {
            tv_content.setText("\u3000\u3000" + surveyAnswerSubmission.getResponse());
            if (surveyAnswerSubmission.getUser() != null) {
                if (surveyAnswerSubmission.getUser().getRealName() != null) {
                    tv_userName.setText(surveyAnswerSubmission.getUser().getRealName());
                } else {
                    tv_userName.setText("匿名用户");
                }
                GlideImgManager.loadCircleImage(context, surveyAnswerSubmission.getUser().getAvatar(),
                        R.drawable.user_default, R.drawable.user_default, ic_user);
            }
        }
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.app_survey_submission_item;
    }
}
