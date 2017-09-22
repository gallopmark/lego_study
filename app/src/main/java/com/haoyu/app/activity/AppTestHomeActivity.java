package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.AppActivityViewEntity;
import com.haoyu.app.entity.AppTestMobileEntity;
import com.haoyu.app.entity.TimePeriod;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;

import butterknife.BindView;

/**
 * 创建日期：2016/12/23 on 11:23
 * 描述: 课程活动测验
 * 作者:马飞奔 Administrator
 */
public class AppTestHomeActivity extends BaseActivity {
    private AppTestHomeActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.testTips)
    TextView testTips;
    @BindView(R.id.testTime)
    TextView testTime;
    @BindView(R.id.bt_know)
    Button bt_konw;
    private boolean running;
    private String testType;
    private TimePeriod timePeriod;
    private AppActivityViewEntity.TestUserMobileEntity mTestUser;
    private String activityId;
    private String relationId;
    private String tuid;
    private AppTestMobileEntity mTestEntity;
    private final int REQUEST_CODE = 1;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_test_home;
    }

    @Override
    public void initView() {
        running = getIntent().getBooleanExtra("running", false);
        testType = getIntent().getStringExtra("testType");
        relationId = getIntent().getStringExtra("relationId");
        timePeriod = (TimePeriod) getIntent().getSerializableExtra("timePeriod");
        if (getIntent().getSerializableExtra("testUser") != null && getIntent().getSerializableExtra("testUser") instanceof AppActivityViewEntity.TestUserMobileEntity) {
            mTestUser = (AppActivityViewEntity.TestUserMobileEntity) getIntent().getSerializableExtra("testUser");
        }
        activityId = getIntent().getStringExtra("activityId");
        String activityTitle = getIntent().getStringExtra("activityTitle");
        if (activityTitle != null && activityTitle.trim().length() > 0)
            toolBar.setTitle_text(Html.fromHtml(activityTitle).toString());
        else
            toolBar.setTitle_text("在线测验");
        showData();
    }

    private void showData() {
        if (running) {   //活动在时间范围内
            if (timePeriod != null && timePeriod.getMinutes() > 0) {
                testTime.setText("距离活动结束还有" + TimeUtil.computeTimeDiff(timePeriod.getMinutes()));
            } else {
                if (timePeriod != null && timePeriod.getState() != null)
                    testTime.setText("活动" + timePeriod.getState());
                else
                    testTime.setText("活动进行中");
            }
        } else {
            testTime.setText("活动已结束");
            testTime.setTextColor(ContextCompat.getColor(context, R.color.orange));
            bt_konw.setText("测验题目仅供查阅");
        }
        if (mTestUser != null) {
            mTestEntity = mTestUser.getmTest();
            tuid = mTestUser.getId();
            if (mTestEntity != null && mTestEntity.getmQuestions() != null && mTestEntity.getmQuestions().size() > 0) {
                int size = mTestUser.getmTest().getmQuestions().size();
                testTips.setText("本次测验共" + size + "道题，满分为100分");
            } else {
                testTips.setText("测验题目未发布");
                bt_konw.setVisibility(View.GONE);
            }
        } else {
            testTips.setText("测验题目未发布");
            bt_konw.setVisibility(View.GONE);
        }
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        bt_konw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AppPageTestActivity.class);
                if (running) {
                    intent.putExtra("canSubmit", true);
                }
                intent.putExtra("testType", testType);
                intent.putExtra("activityId", activityId);
                intent.putExtra("mTestEntity", mTestEntity);
                intent.putExtra("relationId", relationId);
                intent.putExtra("tuid", tuid);
                startActivityForResult(intent, REQUEST_CODE);
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
