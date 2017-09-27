package com.haoyu.app.activity;


import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.CorrectmarkAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.dialog.LoadingDialog;
import com.haoyu.app.entity.CorrectResult;
import com.haoyu.app.entity.CourseActivityListResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.ReceiveAssignment;
import com.haoyu.app.entity.ReceiveList;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * Created by acer1 on 2017/1/7.
 * 学员互评
 */
public class MyMarkActivity extends BaseActivity implements View.OnClickListener {
    private MyMarkActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;

    @BindView(R.id.rl_shake)
    RelativeLayout mR1Sharke;//头部提示
    @BindView(R.id.tv_Already_get)
    TextView mAlreadyGet;//已经领取的作业
    @BindView(R.id.mark_list)
    RecyclerView mMarkListView;//作业内容列表
    private String aid;//活动id
    private String uid;//用户id
    private String assignmentId;//	作业ID
    private String relationId;//作业关系ID
    private String mAllMarkNum;//总共需要领取的作业数量;
    private Intent intent;
    private CorrectmarkAdapter mCorrectAdapter;
    private LoadingDialog dialog;
    @BindView(R.id.data_warn)
    TextView mDataWarn;//没有数据的时候的提醒
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_correct;
    }

    @Override
    public void initView() {
        intent = getIntent();
        aid = intent.getStringExtra("aid");
        uid = intent.getStringExtra("uid");

        assignmentId = intent.getStringExtra("id");
        relationId = intent.getStringExtra("relationId");
        mAllMarkNum = intent.getStringExtra("allMarkNum");
        FullyLinearLayoutManager manager = new FullyLinearLayoutManager(context);
        manager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        mMarkListView.setLayoutManager(manager);
        mCorrectAdapter = new CorrectmarkAdapter(context, receiveAssignmentList, aid, uid, idList);
        mMarkListView.setAdapter(mCorrectAdapter);
    }


    @Override
    public void setListener() {

        mR1Sharke.setOnClickListener(context);
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        mCorrectAdapter.setCallBack(new CorrectmarkAdapter.OpenResourceCallBack() {
            @Override
            public void open(MFileInfo mFileInfo) {

                if (mFileInfo != null && mFileInfo.getUrl() != null) {
                    Intent intent = new Intent(context, MFileInfoActivity.class);
                    intent.putExtra("fileInfo", mFileInfo);
                    startActivity(intent);
                } else {
                    toast(context, "文件链接不存在");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_shake:
                //点击获取作业列表
                getAssignment();
                break;
        }
    }

    private List<ReceiveList> idList = new ArrayList<>();

    //获取已经领取的作业
    public void initData() {
        String url = Constants.OUTRT_NET + "/" + aid + "/study/m/assignment/mark?assignmentRelationId=" + relationId;
        OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<ReceiveAssignment>() {

            @Override
            public void onError(Request request, Exception e) {


                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(ReceiveAssignment receiveAssignment) {
                loadFailView.setVisibility(View.GONE);
                if (receiveAssignment != null && receiveAssignment.getResponseCode() != null && receiveAssignment.getResponseCode().equals("00") && receiveAssignment.getResponseData() != null) {
                    updateContent(receiveAssignment);
                    idList.addAll(receiveAssignment.getResponseData());
                    if (receiveAssignment.getResponseData().size() == 0) {
                        mDataWarn.setVisibility(View.VISIBLE);
                    } else {
                        mDataWarn.setVisibility(View.GONE);
                    }
                    getCorrectAssignment(receiveAssignment);
                    loadFailView.setVisibility(View.GONE);
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //给组件赋值
    private void updateContent(ReceiveAssignment receiveAssignment) {
        if (mAllMarkNum != null && receiveAssignment.getResponseData() != null) {
            String message = "已领取" + receiveAssignment.getResponseData().size() + "/" + mAllMarkNum + "份,点击领取作业";
            SpannableStringBuilder mSpannableStringBuilder = new SpannableStringBuilder(message);
            int startIndex = message.indexOf("已领取") + 3;
            int endIndex = message.indexOf("/");
            mSpannableStringBuilder.setSpan
                    (new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            mAlreadyGet.setText(mSpannableStringBuilder);

        }

    }

    //获取已经领取的作业的详细信息列表
    private List<CorrectResult> receiveAssignmentList = new ArrayList<>();

    //获取要批改作业的详细信息
    private void getCorrectAssignment(final ReceiveAssignment receiveAssignment) {
        if (receiveAssignment != null && receiveAssignment.getResponseData().size() > 0) {
            for (int i = 0; i < receiveAssignment.getResponseData().size(); i++) {
                String id = receiveAssignment.getResponseData().get(i).getId();
                String url = Constants.OUTRT_NET + "/" + aid + "/study/unique_uid_" + uid + "/m/assignment/mark/" + id;
                try {
                    OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<CorrectResult>() {
                        @Override
                        public void onError(Request request, Exception e) {
                            hideTipDialog();
                            mDataWarn.setText("加载失败，请稍后再试");
                        }

                        @Override
                        public void onResponse(CorrectResult response) {
                            hideTipDialog();
                            loadingView.setVisibility(View.GONE);
                            if (response != null && response.getResponseData() != null && response.isSuccess()) {
                                receiveAssignmentList.add(response);
                                mCorrectAdapter.notifyDataSetChanged();
                                if (receiveAssignmentList.size() == 0) {
                                    mDataWarn.setVisibility(View.VISIBLE);
                                } else {
                                    mDataWarn.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }

    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    /**
     * 领取作业
     */
    private void getAssignment() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new LoadingDialog(this, "正在查找作业");
        dialog.show();
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                dialog.dismiss();

                if (hasUnGet) {
                    toast(context, "暂无未领取的作业");
                }
            }
        }, 3000);
        shakeAssignment();
        mCorrectAdapter.notifyDataSetChanged();

    }

    private boolean hasUnGet = false;

    //摇一摇领取作业
    private void shakeAssignment() {
        String url = Constants.OUTRT_NET + "/" + aid + "/study/unique_uid_" + uid + "/m/assignment/mark?assignment.id=" + assignmentId + "&assignmentRelationId=" + relationId;
        OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<CourseActivityListResult>() {
            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(CourseActivityListResult response) {
                hideTipDialog();
                if (response != null && response.getSuccess() && response.getResponseData() != null) {
                    if (idList.size() == response.getResponseData().size()) {
                        hasUnGet = true;
                    } else {
                        initData();
                    }
                }
            }
        });


    }
}
