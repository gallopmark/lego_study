package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.haoyu.app.adapter.AppTestResultGridAdapter;
import com.haoyu.app.adapter.AppTestResultListAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.AppActivityViewEntity;
import com.haoyu.app.entity.AppTestMobileEntity;
import com.haoyu.app.entity.MTestSubmission;
import com.haoyu.app.entity.TimePeriod;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.ColorArcProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * 创建日期：2017/3/3 on 16:14
 * 描述: 测验结果
 * 作者:马飞奔 Administrator
 */
public class AppTestResultActivity extends BaseActivity {
    private AppTestResultActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.progressBar)
    ColorArcProgressBar progressBar;
    @BindView(R.id.tv_score)
    TextView tv_score;
    @BindView(R.id.testTime)
    TextView testTime;
    @BindView(R.id.listLayout)
    View listLayout;
    @BindView(R.id.gridLayout)
    View gridLayout;
    @BindView(R.id.tv_All)
    TextView tv_All;
    @BindView(R.id.tv_correct1)
    TextView tv_correct1;
    @BindView(R.id.tv_error)
    TextView tv_error;
    @BindView(R.id.tv_correct2)
    TextView tv_correct2;
    @BindView(R.id.mTogBtn)
    ToggleButton mTogBtn;
    @BindView(R.id.listRecyclerView)
    RecyclerView listRecyclerView;
    @BindView(R.id.gridRecyclerView)
    RecyclerView gridRecyclerView;
    @BindView(R.id.rl_redo)
    RelativeLayout rl_redo;
    private List<Boolean> gridDatas = new ArrayList<>();
    private AppTestResultGridAdapter gridAdapter;
    private List<AppTestMobileEntity.AppTestQuestion> listDatas = new ArrayList<>();
    private Map<String, MTestSubmission> testSubmissionMap = new HashMap<>();
    private AppTestResultListAdapter listAdapter;
    private boolean running;
    private TimePeriod timePeriod;
    private AppActivityViewEntity.TestUserMobileEntity mTestUser;
    private String activityId;
    private String activityTitle;
    private String testType;
    private double score;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_test_result;
    }

    @Override
    public void initView() {
        running = getIntent().getBooleanExtra("running", false);
        timePeriod = (TimePeriod) getIntent().getSerializableExtra("timePeriod");
        if (getIntent().getSerializableExtra("testUser") != null && getIntent().getSerializableExtra("testUser") instanceof AppActivityViewEntity.TestUserMobileEntity) {
            mTestUser = (AppActivityViewEntity.TestUserMobileEntity) getIntent().getSerializableExtra("testUser");
        }
        activityId = getIntent().getStringExtra("activityId");
        activityTitle = getIntent().getStringExtra("activityTitle");
        testType = getIntent().getStringExtra("testType");
        score = getIntent().getDoubleExtra("score", 0);
        listRecyclerView.setNestedScrollingEnabled(false);
        gridRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager llManager = new LinearLayoutManager(context);
        llManager.setOrientation(LinearLayoutManager.VERTICAL);
        listRecyclerView.setLayoutManager(llManager);
        listAdapter = new AppTestResultListAdapter(context, listDatas, testSubmissionMap);
        listRecyclerView.setAdapter(listAdapter);
        gridAdapter = new AppTestResultGridAdapter(context, gridDatas);
        GridLayoutManager glManager = new GridLayoutManager(context, 5);
        glManager.setOrientation(GridLayoutManager.VERTICAL);
        gridRecyclerView.setLayoutManager(glManager);
        gridRecyclerView.setAdapter(gridAdapter);
        rl_redo.setEnabled(false);
        showData();
    }

    private void showData() {
        if (running) {   //活动在时间范围内
            if (timePeriod != null) {
                if (timePeriod.getMinutes() > 0) {
                    testTime.setText("距离活动结束还有" + TimeUtil.computeTimeDiff(timePeriod.getMinutes()));
                } else {
                    testTime.setText("活动" + timePeriod.getState());
                }
            }
        } else {
            testTime.setText("活动已结束");
            testTime.setTextColor(ContextCompat.getColor(context, R.color.orange));
            rl_redo.setEnabled(false);
        }
        if (running && mTestUser != null && mTestUser.getmTest() != null) {
            int attempts = mTestUser.getAttempts();
            int maxAttempts = mTestUser.getmTest().getMaxAttempts();
            if (maxAttempts == 0) {
                rl_redo.setEnabled(true);
            } else if (maxAttempts > 0 && attempts <= maxAttempts) {
                rl_redo.setEnabled(true);
            } else {
                rl_redo.setEnabled(false);
            }
        } else {
            rl_redo.setEnabled(false);
        }
        progressBar.setMaxValues(100);
        progressBar.setCurrentValues((int) score);
        tv_score.setText(String.valueOf(score));
        if (mTestUser != null) {
            testSubmissionMap = mTestUser.getmTestSubmissionMap();
            int totalNum = testSubmissionMap.size();
            tv_All.setText("全部(" + totalNum + ")");
            int correntNum = 0;
            for (MTestSubmission test : testSubmissionMap.values()) {
                gridDatas.add(test.isCorrect());
                if (test.isCorrect()) {
                    correntNum++;
                }
            }
            gridAdapter.notifyDataSetChanged();
            tv_correct1.setText("答对(" + correntNum + ")");
            tv_error.setText("答错(" + (totalNum - correntNum) + ")");
            tv_correct2.setText("答对" + correntNum + "题");
            if (mTestUser.getmTest() != null && mTestUser.getmTest().getmQuestions() != null) {
                listAdapter.addDatas(mTestUser.getmTest().getmQuestions(), testSubmissionMap);
                listAdapter.notifyDataSetChanged();
            }
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
        rl_redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AppTestHomeActivity.class);
                intent.putExtra("running", running);
                intent.putExtra("testType", testType);
                intent.putExtra("timePeriod", timePeriod);
                intent.putExtra("testUser", mTestUser);
                intent.putExtra("activityId", activityId);
                intent.putExtra("activityTitle", activityTitle);
                startActivity(intent);
                finish();
            }
        });
        mTogBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //选中
                    listLayout.setVisibility(View.VISIBLE);
                    gridLayout.setVisibility(View.GONE);
                    gridRecyclerView.setVisibility(View.GONE);
                    listRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    //未选中
                    listLayout.setVisibility(View.GONE);
                    gridLayout.setVisibility(View.VISIBLE);
                    listRecyclerView.setVisibility(View.GONE);
                    gridRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });// 添加监听事件
    }
}
