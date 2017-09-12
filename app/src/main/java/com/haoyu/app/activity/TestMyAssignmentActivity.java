package com.haoyu.app.activity;

import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.AppToolBar;

import butterknife.BindView;

public class TestMyAssignmentActivity extends BaseActivity {
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.test_responoseScore)
    TextView mResponseScore;//作业得分
    @BindView(R.id.markScore)
    TextView mMarkScore;// 互评得分
    @BindView(R.id.Mutualevaluation_score)
    TextView Mutualevaluation_score;// 综合得分
    @BindView(R.id.view1)
    View view1;
    @BindView(R.id.view2)
    View view2;
    @BindView(R.id.state_commit)
    ImageView stateImage;
    @BindView(R.id.commit_warn)
    TextView commitWarn;
    private String responseScore;//作业得分
    private String markScore;//互评得分分数
    private String allScore;//综合得分
    private String state;//状态

    @Override
    public int setLayoutResID() {
        return R.layout.activity_myassignment;
    }

    @Override
    public void initView() {
        state = getIntent().getStringExtra("astate");
        markScore = getIntent().getStringExtra("amarkScore");
        responseScore = getIntent().getStringExtra("aresponseScore");
        allScore = getIntent().getStringExtra("aallScore");
    }

    @Override
    public void setListener() {
      toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    public void initData() {

        String msg1, msg2, msg3;
        if (responseScore == null) {
            msg1 = "作业得分:--分";

        } else {
            msg1 = "作业得分:" + responseScore + "分";
        }
        if (markScore == null) {
            msg2 = "互评得分:--分";
        } else {
            msg2 = "互评得分:" + markScore + "分";

        }
        if (allScore == null) {
            msg3 = "综合得分:--分";
        } else {
            msg3 = "综合得分:" + allScore + "分";
        }
        mResponseScore.setText(setMsgColor(msg1));
        mMarkScore.setText(setMsgColor(msg2));
        Mutualevaluation_score.setText(setMsgColor(msg3));
        if (state != null && state.equals("not_attempt")) {
            stateImage.setImageResource(R.drawable.weitijiaozuoye);
            commitWarn.setVisibility(View.VISIBLE);
            Mutualevaluation_score.setVisibility(View.GONE);
            mResponseScore.setVisibility(View.GONE);
            mMarkScore.setVisibility(View.GONE);
            view1.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);

        } else if (state != null && state.equals("complete")) {
            stateImage.setImageResource(R.drawable.sadefen);
        } else if (state != null && state.equals("commit")) {
            stateImage.setImageResource(R.drawable.sadefen);
            commitWarn.setVisibility(View.VISIBLE);
            commitWarn.setText("请等待批阅");
            Mutualevaluation_score.setVisibility(View.GONE);
            mResponseScore.setVisibility(View.GONE);
            mMarkScore.setVisibility(View.GONE);
            view1.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);
        } else {
            stateImage.setImageResource(R.drawable.sadefen);
            view1.setVisibility(View.VISIBLE);
            view2.setVisibility(View.VISIBLE);
            commitWarn.setVisibility(View.GONE);
        }

    }

    //设置字体颜色
    private SpannableStringBuilder setMsgColor(String msg) {
        SpannableStringBuilder mSpannableStringBuilder = new SpannableStringBuilder(msg);
        int startIndex = msg.lastIndexOf(":") + 1;
        int endIndex = msg.lastIndexOf("分");
        mSpannableStringBuilder.setSpan
                (new ForegroundColorSpan(ContextCompat.getColor(this,R.color.orange)), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return mSpannableStringBuilder;

    }

}
