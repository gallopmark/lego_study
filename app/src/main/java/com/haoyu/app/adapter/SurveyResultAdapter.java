package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.SurveyAnswer;
import com.haoyu.app.entity.SurveyAnswerSubmission;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.RoundRectProgressBar;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

/**
 * 创建日期：2016/12/28 on 13:37
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class SurveyResultAdapter extends BaseArrayRecyclerAdapter<SurveyAnswer> {
    private Context context;
    private int participateNum;
    private Map<String, Map<String, Integer>> dataMap;
    private ExpandAnswerCallBack answerCallBack;

    public SurveyResultAdapter(Context context, List<SurveyAnswer> mDatas, Map<String, Map<String, Integer>> dateMap) {
        super(mDatas);
        this.context = context;
        this.dataMap = dateMap;
    }

    public int getParticipateNum() {
        return participateNum;
    }

    public void setParticipateNum(int participateNum) {
        this.participateNum = participateNum;
    }

    public void setAnswerCallBack(ExpandAnswerCallBack answerCallBack) {
        this.answerCallBack = answerCallBack;
    }

    public void addAll(List<SurveyAnswer> mDatas, Map<String, Map<String, Integer>> dateMap) {
        this.mDatas.addAll(mDatas);
        this.dataMap.putAll(dateMap);
        notifyDataSetChanged();
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final SurveyAnswer surveyAnswer, final int position) {
        TextView survey_position = holder.obtainView(R.id.survey_position);
        TextView test_type = holder.obtainView(R.id.test_type);
        TextView test_title = holder.obtainView(R.id.test_title);
        RecyclerView recyclerView = holder.obtainView(R.id.recyclerView);
        Button bt_expand = holder.obtainView(R.id.bt_expand);
        survey_position.setText(String.valueOf(position + 1));
        recyclerView.setNestedScrollingEnabled(false);
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        String msg;
        if (surveyAnswer.getType() != null && surveyAnswer.getType().equals(SurveyAnswer.singleChoice)) {
            msg = "单选题";
        } else if (surveyAnswer.getType() != null && surveyAnswer.getType().equals(SurveyAnswer.multipleChoice)) {
            msg = "多选题";
        } else if (surveyAnswer.getType() != null && surveyAnswer.getType().equals(SurveyAnswer.trueOrFalse)) {
            msg = "是非题";
        } else {
            msg = "问答题";
        }
        test_type.setText(msg);
        test_title.setText("\u3000\u3000\u3000\u2000" + surveyAnswer.getTitle());
        if (surveyAnswer.getType() != null && surveyAnswer.getType().equals(SurveyAnswer.textEntry)) {
            MSubmissionAdapter adapter = new MSubmissionAdapter(surveyAnswer.getAnswerSubmissions());
            recyclerView.setAdapter(adapter);
        } else {
            SurveyResultListAdapter adapter = new SurveyResultListAdapter(context, surveyAnswer.getmChoices(), surveyAnswer.getId());
            recyclerView.setAdapter(adapter);
        }
        if (surveyAnswer.getType() != null && surveyAnswer.getType().equals(SurveyAnswer.textEntry)
                && surveyAnswer.getAnswerSubmissions().size() >= 2) {
            bt_expand.setVisibility(View.VISIBLE);
        } else {
            bt_expand.setVisibility(View.GONE);
        }
        /*展开更多答案*/
        bt_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (answerCallBack != null) {
                    answerCallBack.expand(surveyAnswer.getId(), position);
                }
            }
        });
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.course_survey_result_item;
    }

    class SurveyResultListAdapter extends BaseArrayRecyclerAdapter<SurveyAnswer.MChoices> {

        private int[] colors;
        private String questionId;

        public SurveyResultListAdapter(Context context, List<SurveyAnswer.MChoices> mDatas, String questionId) {
            super(mDatas);
            this.questionId = questionId;
            colors = new int[]{ContextCompat.getColor(context, R.color.defaultColor),
                    ContextCompat.getColor(context, R.color.course_progress),
                    ContextCompat.getColor(context, R.color.blue),
                    ContextCompat.getColor(context, R.color.purple),
                    ContextCompat.getColor(context, R.color.aqua),
                    ContextCompat.getColor(context, R.color.orange),
                    ContextCompat.getColor(context, R.color.pink)
            };
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.survey_result_list_item;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, SurveyAnswer.MChoices mChoices, int position) {
            CheckBox cb_select = holder.obtainView(R.id.cb_select);
            TextView detail_tv = holder.obtainView(R.id.detail_tv);
            RoundRectProgressBar mRrogressBar = holder.obtainView(R.id.mRrogressBar);
            if (position < colors.length) {
                mRrogressBar.setmProgressColor(colors[position]);
            } else {
                mRrogressBar.setmProgressColor(colors[position % colors.length]);
            }
            mRrogressBar.setMax(getParticipateNum());
            TextView percentage = holder.obtainView(R.id.percentage);
            cb_select.setEnabled(false);
            cb_select.setText(SurveyAnswer.MChoices.choices[position]);
            detail_tv.setText(mChoices.getContent());
            if (dataMap.get(questionId) != null && dataMap.get(questionId).get(mChoices.getId()) != null
                    && dataMap.get(questionId).get(mChoices.getId()) > 0) {
                int supportNum = dataMap.get(questionId).get(mChoices.getId());
                cb_select.setChecked(true);
                mRrogressBar.setProgress(supportNum);
                percentage.setText(accuracy(supportNum, getParticipateNum(), 0));
            } else {
                cb_select.setChecked(false);
                mRrogressBar.setProgress(0);
                percentage.setText(accuracy(0, getParticipateNum(), 0));
            }
        }
    }

    //方法
    public String accuracy(double num, double total, int scale) {
        if (total == 0) {
            return "0%";
        }
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
        //可以设置精确几位小数
        df.setMaximumFractionDigits(scale);
        //模式 例如四舍五入
        df.setRoundingMode(RoundingMode.HALF_UP);
        double accuracy_num = num / total * 100;
        return df.format(accuracy_num) + "%";
    }

    class MSubmissionAdapter extends BaseArrayRecyclerAdapter<SurveyAnswerSubmission> {

        public MSubmissionAdapter(List<SurveyAnswerSubmission> mDatas) {
            super(mDatas);
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, SurveyAnswerSubmission entity, int position) {
            ImageView ic_user = holder.obtainView(R.id.ic_user);
            TextView tv_userName = holder.obtainView(R.id.tv_userName);
            TextView tv_content = holder.obtainView(R.id.tv_content);
            if (entity.getUser() != null && entity.getUser().getAvatar() != null) {
                GlideImgManager.loadCircleImage(context,
                        entity.getUser().getAvatar(), R.drawable.user_default,
                        R.drawable.user_default, ic_user);
            } else {
                ic_user.setImageResource(R.drawable.user_default);
            }
            if (entity.getUser() != null && entity.getUser().getRealName() != null) {
                tv_userName.setText(entity.getUser().getRealName());
            } else {
                tv_userName.setText("匿名用户");
            }
            tv_content.setText("\u3000\u3000" + entity.getResponse());
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.app_survey_submission_item;
        }
    }

    public interface ExpandAnswerCallBack {
        void expand(String questionId, int position);
    }
}
