package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.CourseRegistStateAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.CourseRegistStateResultBase;
import com.haoyu.app.entity.MCourseRegister;
import com.haoyu.app.entity.MTrainRegister;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppCheckBox;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/5/19 on 15:46
 * 描述:课程报读情况
 * 作者:马飞奔 Administrator
 */
public class CourseRegistStateActivity extends BaseActivity implements View.OnClickListener {
    private CourseRegistStateActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.bottomView)
    RelativeLayout bottomView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.emptyData)
    TextView emptyData;
    @BindView(R.id.tv_selectTips)
    TextView tv_selectTips;
    @BindView(R.id.bt_submit)
    Button bt_submit;
    @BindView(R.id.layout_opreate)
    LinearLayout layout_opreate;
    @BindView(R.id.ll_selectAll)
    LinearLayout ll_selectAll;
    @BindView(R.id.cb_select)
    AppCheckBox cb_select;
    @BindView(R.id.tv_select)
    TextView tv_select;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;  //取消选课
    private String trainId;
    private boolean isNoLimit;
    private CourseRegistStateAdapter adapter;
    private List<MCourseRegister> mDatas = new ArrayList<>();
    private int totalHours, registHours, registedCourseNum;
    private String trainRegisterId;   //培训报名id
    private String selectAll = "全选", cancelAll = "反选", edit = "编辑", cancel = "取消";

    @Override
    public int setLayoutResID() {
        return R.layout.activity_course_regist_state;
    }

    @Override
    public void initView() {
        trainId = getIntent().getStringExtra("trainId");
        isNoLimit = getIntent().getBooleanExtra("isNoLimit", false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CourseRegistStateAdapter(context, mDatas);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initData() {
        getRegistState();
        getCourseList();
        getTrainState();
    }

    /*获取选课情况*/
    private void getRegistState() {
        String url = Constants.OUTRT_NET + "/m/course_center/my_register_state?trainId=" + trainId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<CourseRegistStateResultBase>() {

            @Override
            public void onError(Request request, Exception e) {
            }

            @Override
            public void onResponse(CourseRegistStateResultBase response) {
                if (response != null && response.getResponseData() != null) {
                    bottomView.setVisibility(View.VISIBLE);
                    updateUI(response.getResponseData());
                }
            }
        }));
    }

    /*获取已选课列表*/
    private void getCourseList() {
        String url = Constants.OUTRT_NET + "/m/course_register?relation.id=" + trainId + "&user.id=" + getUserId();
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<List<MCourseRegister>>>() {
            @Override
            public void onBefore(Request request) {
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(BaseResponseResult<List<MCourseRegister>> response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                }
            }
        }));
    }

    /*获取培训报名情况*/
    private void getTrainState() {
        final String url = Constants.OUTRT_NET + "/m/train_register/get_by_trainId?trainId=" + trainId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MTrainRegister>>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(BaseResponseResult<MTrainRegister> response) {
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                }
            }
        }));
    }

    private void updateUI(CourseRegistStateResultBase.CourseRegistStateData responseData) {
        registedCourseNum = responseData.getRegistedCourseNum();
        totalHours = responseData.getRequireStudyHours();
        registHours = responseData.getRegistedStudyHours();
        tv_selectTips.setText("要求" + totalHours + "学时"
                + "，已选" + registedCourseNum + "门课共" + registHours + "学时");
    }

    private void updateUI(List<MCourseRegister> mDatas) {
        if (mDatas.size() > 0) {
            toolBar.setShow_right_button(true);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.addAll(mDatas);
        } else {
            emptyData.setVisibility(View.VISIBLE);
        }
    }

    private void updateUI(MTrainRegister mTrainRegister) {
        trainRegisterId = mTrainRegister.getId();
        bt_submit.setVisibility(View.VISIBLE);
        if (mTrainRegister.getChooseCourseState() != null && mTrainRegister.getChooseCourseState().equals("submited")) {
            if (isNoLimit) {
                bt_submit.setText("提交选课");
                bt_submit.setEnabled(true);
            } else {
                bt_submit.setText("选课已提交");
                bt_submit.setEnabled(false);
            }
        } else {
            bt_submit.setText("提交选课");
            bt_submit.setEnabled(true);
        }
    }

    @Override
    public void setListener() {
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                if (isEdit) {
                    toolBar.setRight_button_text(cancel);
                    adapter.setEdit();
                    isEdit = false;
                    bottomView.setVisibility(View.GONE);
                    layout_opreate.setVisibility(View.VISIBLE);
                } else {
                    toolBar.setRight_button_text(edit);
                    adapter.cancel();
                    isEdit = true;
                    cb_select.setChecked(false);
                    tv_select.setText(selectAll);
                    tv_cancel.setVisibility(View.GONE);
                    bottomView.setVisibility(View.VISIBLE);
                    layout_opreate.setVisibility(View.GONE);
                }
            }
        });
        ll_selectAll.setOnClickListener(context);
        cb_select.setOnClickListener(context);
        tv_cancel.setOnClickListener(context);
        bt_submit.setOnClickListener(context);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getCourseList();
            }
        });
        adapter.setCancelCallBack(new CourseRegistStateAdapter.CancelCallBack() {
            @Override
            public void cancel(MCourseRegister mCourseRegister, int position) {
                unRegistCourse(mCourseRegister, position, false);
            }
        });
        adapter.setOnSelectListener(new CourseRegistStateAdapter.OnSelectListener() {
            @Override
            public void onSelect(ArrayMap<Integer, Boolean> isSelected) {
                boolean selected = false;
                int select = 0;
                for (Boolean isCheck : isSelected.values()) {
                    if (isCheck) {
                        select++;
                        selected = true;
                    }
                }
                if (!selected) {
                    cb_select.setChecked(false);
                    tv_select.setText(selectAll);
                    tv_cancel.setVisibility(View.GONE);
                } else {
                    tv_cancel.setVisibility(View.VISIBLE);
                    if (select == isSelected.size()) {
                        cb_select.setChecked(true);
                        tv_select.setText(cancelAll);
                    } else {
                        cb_select.setChecked(false);
                        tv_select.setText(selectAll);
                    }
                }
            }
        });
    }

    private boolean isEdit = true;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_selectAll:
                setCheckOpreate();
                break;
            case R.id.cb_select:
                setCheckOpreate();
                break;
            case R.id.tv_cancel:
                bottomView.setVisibility(View.VISIBLE);
                layout_opreate.setVisibility(View.GONE);
                ArrayMap<Integer, Boolean> isSelected = adapter.getIsSelected();
                for (Integer position : isSelected.keySet()) {
                    if (isSelected.get(position) && position < mDatas.size()) {
                        unRegistCourse(mDatas.get(position), position, true);
                    }
                }
                break;
            case R.id.bt_submit:
                String message;
                boolean submit;
                if (isNoLimit) {
                    message = "提交选课信息后，不允许改课程或取消选课";
                    submit = true;
                } else if (registHours < totalHours) {
                    message = "提交失败，选课学时未达标，请查看选课要求";
                    submit = false;
                } else {
                    message = "提交选课信息后，不允许再次选课或取消选课";
                    submit = true;
                }
                showSubmitDialog(message, submit);
                break;
        }
    }

    private void setCheckOpreate() {
        if (cb_select.isChecked()) {
            cb_select.setChecked(false);
            tv_select.setText(selectAll);
            adapter.clear();
            tv_cancel.setVisibility(View.GONE);
        } else {
            cb_select.setChecked(true);
            tv_select.setText(cancelAll);
            adapter.selectAll();
            tv_cancel.setVisibility(View.VISIBLE);
        }
    }

    /*取消选课*/
    private void unRegistCourse(final MCourseRegister mCourseRegister, final int position, final boolean isAll) {
        String url = Constants.OUTRT_NET + "/unique_uid_" + getUserId() + "/m/course_register/delete/" + mCourseRegister.getId();
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                if (!isAll) {
                    showTipDialog();
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    adapter.getIsSelected().put(position, false);
                    mDatas.remove(mCourseRegister);
                    adapter.notifyDataSetChanged();
                    if (mCourseRegister.getmCourse() != null) {
                        registHours -= mCourseRegister.getmCourse().getStudyHours();
                    }
                    registedCourseNum--;
                    if (registedCourseNum < 0) {
                        registedCourseNum = 0;
                    }
                    tv_selectTips.setText("要求" + totalHours + "学时"
                            + "，已选" + registedCourseNum + "门课共" + registHours + "学时");
                    if (mDatas.size() <= 0) {
                        emptyData.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        toolBar.setShow_right_button(false);
                    }
                }
            }
        }, map));
    }

    private void showSubmitDialog(String message, boolean submit) {
        MaterialDialog dialog = new MaterialDialog(context);
        if (submit) {
            dialog.setTitle("请慎重提交");
        } else {
            dialog.setTitle("提示");
        }
        dialog.setMessage(message);
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        dialog.setNegativeTextColor(ContextCompat.getColor(context, R.color.gray));
        if (submit) {
            dialog.setPositiveButton("确定提交", new MaterialDialog.ButtonClickListener() {
                @Override
                public void onClick(View v, AlertDialog dialog) {
                    submitCourse();
                }
            });
            dialog.setNegativeButton("取消", null);
        } else {
            dialog.setPositiveButton("我知道了", new MaterialDialog.ButtonClickListener() {
                @Override
                public void onClick(View v, AlertDialog dialog) {
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    /*提交选课*/
    private void submitCourse() {
        String url = Constants.OUTRT_NET + "/m/course_center/submit_choose_course";
        Map<String, String> map = new HashMap<>();
        map.put("id", trainRegisterId);
        map.put("train.id", trainId);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showLoadingDialog("正在提交");
            }

            @Override
            public void onError(Request request, Exception e) {
                hideLoadingDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideLoadingDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    if (!isNoLimit) {
                        MessageEvent ev = new MessageEvent();
                        ev.action = Action.SUBMIT_CHOOSE_COURSE;
                        RxBus.getDefault().post(ev);
                    }
                    finish();
                } else if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("03")) {
                    showDialog();
                } else {
                    toast(context, "提交失败，请稍后再试");
                }
            }
        }, map));
    }

    private void showDialog() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("提交失败，选课学时未达标，请查看选课要求");
        dialog.setPositiveButton("我知道了", null);
        dialog.show();
    }
}
