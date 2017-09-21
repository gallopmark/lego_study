package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.haoyu.app.adapter.DiscussFileAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.AppActivityViewEntity;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.DetailMap;
import com.haoyu.app.entity.MAssignmentEntity;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.TimePeriod;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.ExpandableTextView;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.RoundRectProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;


public class TestAssignmentActivity extends BaseActivity implements OnClickListener {
    private TestAssignmentActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.alerady_provider)
    TextView alerady_provider;// 提交作显示
    @BindView(R.id.own_percent)
    TextView own_percent;// 已经提交的作业所占的比例
    @BindView(R.id.already_assessment)
    TextView already_assessment;// 互评显示
    @BindView(R.id.owntotal_percent)
    TextView owntotal_percent;// 已互评所占百分比
    @BindView(R.id.myhomeworkScore)
    TextView myhomeworkScore;// 我的作业得分

    @BindView(R.id.timelimt)
    TextView mTimeLimt;//限时完成该
    @BindView(R.id.assignment_commit)
    RelativeLayout mAssignmentCommit;//提交作业
    @BindView(R.id.huping)
    RelativeLayout mHuPing;//互评任务
    @BindView(R.id.in_control_content)
    ImageView in_control_content;
    @BindView(R.id.markType)
    TextView markType;// 评审类型
    @BindView(R.id.assign_desc)
    RelativeLayout AssignDesc;
    @BindView(R.id.scrollview)
    ScrollView mScrollView;
    @BindView(R.id.own_score)
    RelativeLayout mOwnScore;//我的得分
    private int allMarkNum;
    @BindView(R.id.reBack)
    TextView mReBack;//申请退回作业
    @BindView(R.id.own_progress)
    RoundRectProgressBar progressBar1;//作业所占百分比
    @BindView(R.id.own_progress2)
    RoundRectProgressBar progressBar2;//互评所占百分比
    private DetailMap detailMap;
    @BindView(R.id.rl_top)
    RelativeLayout rl_top;
    @BindView(R.id.xRecyclerView)
    RecyclerView xRecyclerView;
    @BindView(R.id.ll_content)
    LinearLayout ll_content;
    private String mAcid;//活动id
    private String userId;//用户id
    private String state;//作业的状态
    private GridLayoutManager fileManager;
    private DiscussFileAdapter discussFileAdapter;
    private List<MFileInfo> mFileInfoList = new ArrayList<>();
    private String aresponseScore;//
    private String aallScore;
    private String amarkScore;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;

    private boolean running;
    private TimePeriod timePeriod;

    @BindView(R.id.at_content)
    ExpandableTextView at_content;

    @Override
    public void obBusEvent(MessageEvent event) {
        String action = event.getAction();
        if (action.equals(Action.SUBMIT_COURSE_ASSIGNMENT)) {
            mFileInfoList.clear();
            getDescData();
        }
    }

    @Override
    public int setLayoutResID() {
        return R.layout.activity_assignment;
    }

    @Override
    public void setListener() {
        mOwnScore.setOnClickListener(context);
        mAssignmentCommit.setOnClickListener(context);
        mHuPing.setOnClickListener(context);

        AssignDesc.setOnClickListener(context);

        rl_top.setOnClickListener(context);
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getDescData();
            }
        });
        discussFileAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                MFileInfo mFileInfo = mFileInfoList.get(position);
                if (mFileInfo.getUrl() != null) {
                    Intent intent = new Intent(context, MFileInfoActivity.class);
                    intent.putExtra("fileInfo", mFileInfo);
                    startActivity(intent);
                } else {
                    toast(context, "文件链接不存在");
                }
            }
        });
    }

    public void initView() {
        mAcid = getIntent().getStringExtra("activityId");
        running = getIntent().getBooleanExtra("running", false);
        timePeriod = (TimePeriod) getIntent().getSerializableExtra("timePeriod");
        userId = context.getUserId();
        discussFileAdapter = new DiscussFileAdapter(context, mFileInfoList);
        fileManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        xRecyclerView.setLayoutManager(fileManager);
        xRecyclerView.setAdapter(discussFileAdapter);
        getDescData();
        registRxBus();
    }

    private void getDescData() {
        String url = Constants.OUTRT_NET + "/" + mAcid + "/study/m/activity/ncts/" + mAcid + "/view";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<AppActivityViewResult>() {
            @Override
            public void onBefore(Request request) {
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
                mScrollView.setVisibility(View.GONE);
            }

            @Override
            public void onResponse(AppActivityViewResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    mScrollView.setVisibility(View.VISIBLE);
                    loadFailView.setVisibility(View.GONE);
                    showContent(response);
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                    mScrollView.setVisibility(View.GONE);
                }
            }
        }));
    }

    private AppActivityViewResult mResult;

    // 获取内容
    private void showContent(AppActivityViewResult response) {
        mResult = response;
        if (response.getResponseData() != null && response.getResponseData().getmActivityResult() != null) {
            // 作业描述内容
            String title = response.getResponseData().getmActivityResult().getmActivity().getTitle();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                title = Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY).toString();
            } else {
                title = Html.fromHtml(title).toString();
            }
            toolBar.setTitle_text(title);

            state = response.getResponseData().getmActivityResult().getState();
            //作业得分
            detailMap = response.getResponseData().getmActivityResult().getDetailMap();
            if (detailMap != null) {
                if (state.equals("complete")) {
                    aresponseScore = String.valueOf((int) detailMap.getResponse_score());
                    amarkScore = String.valueOf((int) detailMap.getMark_score());
                    aallScore = String.valueOf((int) detailMap.getComplete_pct());
                    myhomeworkScore.setText(aallScore);
                }
            }
        }
        if (response.getResponseData() != null && response.getResponseData().getmAssignmentUser() != null) {
            int markedNum = response.getResponseData().getmAssignmentUser().getMarkedNum();
            if (response.getResponseData().getmAssignmentUser().getmAssignmentEntity() != null) {
                MAssignmentEntity mAssignmentEntity = response.getResponseData().getmAssignmentUser().getmAssignmentEntity();
                mFileInfoList.addAll(response.getResponseData().getmAssignmentUser().getmAssignmentEntity().getMFileInfos());
                discussFileAdapter.notifyDataSetChanged();
                String type = response.getResponseData().getmAssignmentUser().getmAssignmentEntity().getMarkType();
                //已经提交的作业个数
                int percenta = 100 - (int) mAssignmentEntity.getMarkScorePct();
                if (response.getResponseData().getmAssignmentUser().getState() != null) {
                    //批改他人作业后可获得分数
                    String state = response.getResponseData().getmAssignmentUser().getState();
                    own_percent.setText("占总分" + percenta + "%");
                    if (state.equals("commit") || state.equals("complete")) {
                        alerady_provider.setText("已提交1/1个");
                        if (percenta > 0) {
                            progressBar1.setProgress(100);
                        } else {
                            progressBar1.setProgress(0);
                        }
                    } else {
                        alerady_provider.setText("已提交0/1个");
                        progressBar1.setProgress(0);
                    }

                }
                if (type.equals("teacher")) {
                    mHuPing.setVisibility(View.GONE);
                } else {
                    mHuPing.setVisibility(View.VISIBLE);
                }
                //作业被批阅的次数 0表示可以退回

                //完成状态:not_attempt:未参 commit:已提交 complete:已完成 return:已退回
                //批改类型
                String mMarkType = mAssignmentEntity.getMarkType();
                //限时完成的时间
                if (running) {
                    String time;
                    if (timePeriod != null) {
                        if (timePeriod.getEndTime() > 0) {
                            String TIME_FORMAT2 = "yyyy-MM-dd HH:mm";
                            time = "限时完成：" + TimeUtil.convertToTime(TIME_FORMAT2, timePeriod.getEndTime());
                        } else {
                            time = "活动" + timePeriod.getState();
                        }

                    } else {
                        time = "活动时间未设置";
                    }
                    mTimeLimt.setText(time);

                } else {
                    mTimeLimt.setText("活动已经结束");
                }
                // 学员互评
                allMarkNum = response.getResponseData().getmAssignmentUser().getmAssignmentEntity().getMarkNum();
                //已经批阅的个数
                int markNum = response.getResponseData().getmAssignmentUser().getMarkNum();
                if (mMarkType != null) {
                    if (mMarkType.equals("each_other"))
                        markType.setText("学员互评");
                    if (markType.equals("teacher"))
                        markType.setText("老师评审");
                }
                already_assessment.setText("已互评" + response.getResponseData().getmAssignmentUser().getMarkNum() + "个/" + mAssignmentEntity.getMarkNum() + "个");
                String state = response.getResponseData().getmAssignmentUser().getState();
                if (state.equals("not_attempt")) {
                    myhomeworkScore.setText("未提交");
                } else if (state.equals("commit")) {
                    myhomeworkScore.setText("等待批阅");
                } else if (state.equals("return")) {
                    myhomeworkScore.setText("已退回");
                }
                owntotal_percent.setText(String.valueOf("占总分" + ((int) mAssignmentEntity.getMarkScorePct() + "%")));
                if (allMarkNum != 0) {
                    int percent = markNum * 100 / allMarkNum;
                    progressBar2.setProgress(percent);
                } else {
                    progressBar2.setProgress(0);
                }
                //作业未被批阅时可以申请退回
                if (running) {
                    if (markedNum == 0 && state.equals("commit")) {
                        mReBack.setVisibility(View.VISIBLE);
                    } else {
                        mReBack.setVisibility(View.GONE);
                    }
                }

                if (mAssignmentEntity != null && mAssignmentEntity.getContent() != null) {
                    at_content.setHtmlText(mAssignmentEntity.getContent());
                } else {
                    at_content.setHtmlText("暂无内容");
                }

            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_top:
                //课程描述内容
                if (ll_content.getVisibility() == View.VISIBLE) {
                    in_control_content.setImageResource(R.drawable.go_into);
                    ll_content.setVisibility(View.GONE);
                } else {
                    in_control_content.setImageResource(R.drawable.zhankai);
                    ll_content.setVisibility(View.VISIBLE);

                }
                break;

            case R.id.assignment_commit:
                /*完成状态:not_attempt:未参 commit:已提交 complete:已完成 return:已退回*/
                //提交作业
                if (running) {
                    submitAssignment();
                } else {
                    showMaterialDialog("提示", "活动已结束,无法提交作业");
                }
                break;
            case R.id.huping:
                //互评
                if (running) {
                    markEachOther();
                } else {
                    showMaterialDialog("提示", "活动已结束,无法参与互评");
                }
                break;
            case R.id.own_score:
                //得分详情
                AppActivityViewEntity.HomeWorkEntity user = mResult.getResponseData().getmAssignmentUser();
                Intent intenta = new Intent(TestAssignmentActivity.this, TestMyAssignmentActivity.class);
                intenta.putExtra("amarkScore", amarkScore);
                intenta.putExtra("aresponseScore", aresponseScore);
                intenta.putExtra("aallScore", aallScore);
                if (user.getState() != null) {
                    intenta.putExtra("astate", user.getState());
                }
                startActivity(intenta);

                break;
            case R.id.reBack:
                //申请退回作业
                reBack();
                break;

        }

    }

    //提交作业
    private void submitAssignment() {
        AppActivityViewEntity.HomeWorkEntity usera = mResult.getResponseData().getmAssignmentUser();
        String state = usera.getState();
        if (usera.getmAssignmentEntity() != null && usera.getmAssignmentEntity().getInResponseTime()) {
            if (state.equals("not_attempt") || state.endsWith("return")) {
                Intent intent3 = new Intent(TestAssignmentActivity.this, AppSubmitAssignmentActivity.class);
                if (usera != null) {
                    intent3.putExtra("aid", mAcid);
                    intent3.putExtra("uid", context.getUserId());
                    intent3.putExtra("auid", usera.getId());
                    if (usera.getmAssignmentEntity() != null && usera.getmAssignmentEntity().getFileTypes() != null) {
                        intent3.putExtra("fileType", usera.getmAssignmentEntity().getFileTypes());
                    }

                }
                startActivity(intent3);
            } else if (state.equals("commit")) {
                toast(context, "作业已提交");
            } else {
                toast(context, "作业已完成");
            }

        } else {
            toast(context, "已过提交时间");
        }
    }

    //互评
    private void markEachOther() {
        if (mResult != null && mResult.getResponseData() != null && mResult.getResponseData().getmAssignmentUser() != null) {
            if (mResult.getResponseData().getmAssignmentUser().getmAssignmentEntity() != null && mResult.getResponseData().getmAssignmentUser().getmAssignmentEntity().getInMarkTime()) {
                Intent mIntent = new Intent(TestAssignmentActivity.this, MyMarkActivity.class);
                mIntent.putExtra("aid", mAcid);
                mIntent.putExtra("uid", userId);
                mIntent.putExtra("markCount", mResult.getResponseData().getmAssignmentUser().getMarkNum());
                if (mResult.getResponseData().getmAssignmentUser().getmAssignmentEntity() != null) {
                    mIntent.putExtra("id", mResult.getResponseData().getmAssignmentUser().getmAssignmentEntity().getId());
                }
                mIntent.putExtra("relationId", mResult.getResponseData().getmAssignmentUser().getAssignmentRelationId());
                mIntent.putExtra("allMarkNum", String.valueOf(allMarkNum));
                startActivity(mIntent);
            } else {
                toast(context, "已过互评时间");
            }
        }
    }

    //申请退回作业
    private void reBack() {
        String
                url = Constants.OUTRT_NET + "/" + mAcid + "/study/unique_uid_" + context.getUserId() + "/m/assignment/user/"
                + mResult.getResponseData().getmAssignmentUser().getId() + "/back";
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    toast(context, "申请退回成功");
                    mReBack.setVisibility(View.GONE);
                    mFileInfoList.clear();
                    getDescData();
                } else {
                    toast(context, "申请退回失败");
                }
            }
        }, map));

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegistRxBus();

    }
}
