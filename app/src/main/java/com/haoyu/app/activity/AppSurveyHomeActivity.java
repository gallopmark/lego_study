package com.haoyu.app.activity;

import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.AppActivityViewEntity;
import com.haoyu.app.entity.CourseSurveyEntity;
import com.haoyu.app.entity.TimePeriod;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;

import butterknife.BindView;

/**
 * 创建日期：2016/12/24 on 10:44
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppSurveyHomeActivity extends BaseActivity {
    private AppSurveyHomeActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.time_layout)
    View time_layout;
    @BindView(R.id.survey_time)
    TextView survey_time;
    @BindView(R.id.welcome)
    TextView welcome;
    @BindView(R.id.tv_survey_title)
    TextView tv_survey_title;
    @BindView(R.id.tv_description)
    TextView tv_description;
    private String relationId;
    private String type;
    private String activityId;
    private String surveyId;
    private String surveyTitle;
    @BindView(R.id.surveyIco)
    ImageView surveyIco;
    @BindView(R.id.survey_content)
    LinearLayout survey_content;
    @BindView(R.id.stopTips)
    TextView stopTips;
    @BindView(R.id.tv_bottomtips)
    TextView tv_bottomtips;
    private String state;
    private boolean running, isStop;
    private TimePeriod timePeriod;
    private AppActivityViewEntity.SurveyUserMobileEntity mSurveyUser;
    private int REQUEST_CODE = 1;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_survey_home;
    }

    @Override
    public void initView() {
        running = getIntent().getBooleanExtra("running", false);
        timePeriod = (TimePeriod) getIntent().getSerializableExtra("timePeriod");
        mSurveyUser = (AppActivityViewEntity.SurveyUserMobileEntity) getIntent().getSerializableExtra("surveyUser");
        type = getIntent().getStringExtra("type");
        relationId = getIntent().getStringExtra("relationId");
        activityId = getIntent().getStringExtra("activityId");
        String activityTitle = getIntent().getStringExtra("activityTitle");
        if (activityTitle != null && activityTitle.trim().length() > 0)
            toolBar.setTitle_text(Html.fromHtml(activityTitle).toString());
        else
            toolBar.setTitle_text("问卷调查");
    }

    public void initData() {
        updateUI(timePeriod);
        if (mSurveyUser != null) {
            state = mSurveyUser.getState();
            updateUI(mSurveyUser.getmSurvey());
        }
    }

    private void updateUI(TimePeriod timePeriod) {
        if (running) {
            if (timePeriod != null && timePeriod.getMinutes() > 0) {   //活动在时间范围内
                survey_time.setText("离问卷调研结束还剩：" + TimeUtil.computeTimeDiff(timePeriod.getMinutes()));
            } else {
                if (timePeriod != null && timePeriod.getState() != null)
                    survey_time.setText("问卷调研" + timePeriod.getState());
                else
                    survey_time.setText("问卷调研进行中");
            }
            showSurvey();
        } else {
            isStop = true;
            stopSurvey();
        }
    }

    private void updateUI(CourseSurveyEntity surveyEntity) {
        surveyId = surveyEntity.getId();
        surveyTitle = surveyEntity.getTitle();
        tv_survey_title.setText(surveyTitle);
        toolBar.setTitle_text(surveyTitle);
        String description = surveyEntity.getDescription();
        Spanned spanned = Html.fromHtml(description, new HtmlHttpImageGetter(tv_description, Constants.REFERER, true), null);
        tv_description.setText(spanned);
    }

    private void showSurvey() {
        surveyIco.setImageResource(R.drawable.course_survey_home_ig);
        time_layout.setVisibility(View.VISIBLE);
        welcome.setVisibility(View.VISIBLE);
        survey_content.setVisibility(View.VISIBLE);
        tv_bottomtips.setText("开始问卷调查");
    }

    private void stopSurvey() {
        surveyIco.setImageResource(R.drawable.course_survey_home_stop_icon);
        time_layout.setVisibility(View.GONE);
        welcome.setVisibility(View.GONE);
        stopTips.setVisibility(View.VISIBLE);
        toolBar.setTitle_text("调研问卷");
        tv_bottomtips.setText("查看问卷");
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        tv_bottomtips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStop) {
                    Intent intent = new Intent(context, AppSurveyResultActivity.class);
                    intent.putExtra("type", type);
                    intent.putExtra("relationId", relationId);
                    intent.putExtra("activityId", activityId);
                    intent.putExtra("surveyId", surveyId);
                    intent.putExtra("surveyTitle", surveyTitle);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(context, AppPageSurveyActivity.class);
                    if (running) {
                        intent.putExtra("canSubmit", true);
                    }
                    intent.putExtra("type", type);
                    intent.putExtra("relationId", relationId);
                    intent.putExtra("activityId", activityId);
                    intent.putExtra("surveyTitle", surveyTitle);
                    intent.putExtra("surveyId", surveyId);
                    intent.putExtra("state", state);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
