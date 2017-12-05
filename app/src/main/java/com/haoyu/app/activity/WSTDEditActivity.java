package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.widget.NestedScrollView;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.DateTimePickerDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.MWorkshopActivity;
import com.haoyu.app.entity.WorkshopActivityResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

/**
 * 创建日期：2017/5/27 on 15:30
 * 描述：工作坊添加教学研讨活动
 * 作者:马飞奔 Administrator
 */
public class WSTDEditActivity extends BaseActivity implements View.OnClickListener {
    private WSTDEditActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.et_content)
    EditText et_content;
    @BindView(R.id.et_mainCount)
    EditText et_mainCount;
    @BindView(R.id.et_childCount)
    EditText et_childCount;
    @BindView(R.id.ll_startTime)
    LinearLayout ll_startTime;
    @BindView(R.id.tv_start)
    TextView tv_start;
    @BindView(R.id.ll_endTime)
    LinearLayout ll_endTime;
    @BindView(R.id.tv_end)
    TextView tv_end;
    private String workshopId;
    private String workSectionId;
    private int nowYear, nowMonth, nowDay;
    private int startYear, endYear, startDay;
    private int startMonth, endMonth, endDay;
    private MWorkshopActivity activity;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_wstdedit;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        workSectionId = getIntent().getStringExtra("workSectionId");
        Calendar c = Calendar.getInstance();
        nowYear = c.get(Calendar.YEAR);
        nowMonth = c.get(Calendar.MONTH) + 1;
        nowDay = c.get(Calendar.DAY_OF_MONTH);
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
                Common.hideSoftInput(context);
                String title = et_title.getText().toString().toString();
                String content = et_content.getText().toString().trim();
                String mainCount = et_mainCount.getText().toString().trim();
                String childCount = et_childCount.getText().toString().trim();
                String startTime = tv_start.getText().toString().trim();
                String endTime = tv_end.getText().toString().trim();
                if (title.length() == 0) {
                    showMaterialDialog("提示", "请输入研讨标题");
                } else if (content.length() == 0) {
                    showMaterialDialog("提示", "请输入研讨内容");
                } else if (mainCount.length() == 0) {
                    showMaterialDialog("提示", "请输入输入主帖数");
                } else if (childCount.length() == 0) {
                    showMaterialDialog("提示", "请输入输入子帖数");
                } else if (startTime.length() == 0) {
                    showMaterialDialog("提示", "请设置开始时间");
                } else if (endTime.length() == 0) {
                    showMaterialDialog("提示", "请设置结束时间");
                } else {
                    if (activity != null)
                        lastSubmit(activity.getId(), mainCount, childCount, startTime, endTime);
                    else
                        submitAt(title, content, childCount, mainCount, startTime, endTime);
                }
            }
        });
        TextWatcher watcher = new TextWatcher() {
            private CharSequence temp;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                temp = charSequence.toString().trim();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (temp != null && temp.equals(editable.toString().trim())) {
                    if (activity != null)
                        activity = null;
                }
            }
        };
        et_title.addTextChangedListener(watcher);
        et_content.addTextChangedListener(watcher);
        ll_startTime.setOnClickListener(context);
        ll_endTime.setOnClickListener(context);
        View.OnFocusChangeListener listener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                switch (view.getId()) {
                    case R.id.et_mainCount:
                        if (hasFocus) {
                            scrollView.smoothScrollTo(0, et_mainCount.getBottom());
                        }
                        break;
                    case R.id.et_childCount:
                        if (hasFocus) {
                            scrollView.smoothScrollTo(0, et_childCount.getBottom());
                        }
                        break;
                }
            }
        };
        et_mainCount.setOnFocusChangeListener(listener);
        et_childCount.setOnFocusChangeListener(listener);
    }

    @Override
    public void onClick(View view) {
        Common.hideSoftInput(context);
        switch (view.getId()) {
            case R.id.ll_startTime:
                setStartTime(tv_start);
                break;
            case R.id.ll_endTime:
                setEndTime(tv_end);
                break;
        }
    }

    private void setStartTime(final TextView tv_start) {
        DateTimePickerDialog pickerDialog = new DateTimePickerDialog(context);
        pickerDialog.setTitle("选择开始时间");
        pickerDialog.setPositiveButton("设置");
        pickerDialog.setDateListener(new DateTimePickerDialog.DateListener() {
            @Override
            public void Date(int year, int month, int day) {
                startYear = year;
                startMonth = month;
                startDay = day;
                String startTime;
                if (month < 10) {
                    startTime = year + "-0" + month;
                } else {
                    startTime = year + "-" + month;
                }
                if (day < 10) {
                    startTime += "-0" + startDay;
                } else {
                    startTime += "-" + startDay;
                }
                if (chckDate()) {
                    tv_start.setText(startTime);
                }
            }

            @Override
            public void Time(int hour, int minute) {

            }
        });
        pickerDialog.show();
    }

    private void setEndTime(final TextView tv_end) {
        DateTimePickerDialog pickerDialog = new DateTimePickerDialog(context);
        pickerDialog.setTitle("选择结束时间");
        pickerDialog.setPositiveButton("设置");
        pickerDialog.setDateListener(new DateTimePickerDialog.DateListener() {
            @Override
            public void Date(int year, int month, int day) {
                endYear = year;
                endMonth = month;
                endDay = day;
                if (chckDate()) {
                    setEndTime(tv_end, endYear, endMonth, endDay);
                }
            }

            @Override
            public void Time(int hour, int minute) {

            }
        });
        pickerDialog.show();
    }

    private boolean chckDate() {
        if ((startYear != 0 && endYear != 0) && (endYear < nowYear ||
                (endYear == nowYear && endMonth < nowMonth) || (endYear == nowYear && endMonth == nowMonth && endDay < nowDay) || endYear < startYear || (startYear == endYear && endMonth < startMonth) || (startYear == endYear && endMonth == startMonth && endDay < startDay))) {
            toastMsg("<p>您选择的时间不符合要求，请重新选择！<br />" +
                    "时间选择要求：<br />1.结束年份不可以小于开始年份；<br />" +
                    "2.结束年份不可以小于当前年份；<br />" +
                    "3.当选择的年份相同时，选择的结束月份不可以小于开始月份。</p>");
            return false;
        } else {
            return true;
        }

    }

    private void setEndTime(TextView tv_end, int year, int month, int day) {
        String endTime;
        if (month < 10) {
            endTime = year + "-0" + month;
        } else {
            endTime = year + "-" + month;
        }
        if (day < 10) {
            endTime += "-0" + day;
        } else {
            endTime += "-" + day;
        }
        tv_end.setText(endTime);
    }

    private void submitAt(String title, final String content, final String childCount, final String mainCount, final String startTime, final String endTime) {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + context.getUserId() + "/m/activity/wsts";
        final Map<String, String> map = new HashMap<>();
        map.put("activity.relation.id", workSectionId);
        map.put("activity.type", "discussion");
        map.put("discussion.discussionRelations[0].relation.id", workshopId);
        map.put("discussion.title", title);
        map.put("discussion.content", content);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<WorkshopActivityResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(WorkshopActivityResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    activity = response.getResponseData();
                    lastSubmit(activity.getId(), mainCount, childCount, startTime, endTime);
                }
            }
        }, map));
    }

    private void lastSubmit(final String activityId, final String mainCount, final String childCount, final String startTime, final String endTime) {
        showTipDialog();
        addSubscription(Flowable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return submitPost(mainCount, childCount, activityId) && submitTime(startTime, endTime, activityId);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean success) throws Exception {
                hideTipDialog();
                if (success) {
                    Intent intent = new Intent();
                    intent.putExtra("activity", activity);
                    setResult(RESULT_OK, intent);
                    finish();
                } else
                    toastFullScreen("提交失败", false);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                hideTipDialog();
                toastFullScreen("提交失败", false);
            }
        }));
    }

    private boolean submitPost(String mainCount, String childCount, String activityId) throws Exception {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId() + "/m/activity/wsts/" + activityId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("activity.attributeMap[main_post_num].attrValue", mainCount);
        map.put("activity.attributeMap[sub_post_num].attrValue", childCount);
        String json = OkHttpClientManager.postAsString(context, url, map);
        Gson gson = new GsonBuilder().create();
        BaseResponseResult result = gson.fromJson(json, BaseResponseResult.class);
        return result != null && result.getResponseCode() != null && result.getResponseCode().equals("00");
    }

    private boolean submitTime(String startTime, String endTime, String activityId) throws Exception {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId() + "/m/activity/wsts/" + activityId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("activity.startTime", startTime);
        map.put("activity.endTime", endTime);
        String json = OkHttpClientManager.postAsString(context, url, map);
        Gson gson = new GsonBuilder().create();
        BaseResponseResult result = gson.fromJson(json, BaseResponseResult.class);
        return result != null && result.getResponseCode() != null && result.getResponseCode().equals("00");
    }

    private void toastMsg(String msg) {
        Spanned spanned = Html.fromHtml(msg);
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage(spanned);
        dialog.setPositiveButton("重新选择", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                dialog.dismiss();

            }
        });
        dialog.setNegativeButton("取消选择", null);
        dialog.show();
    }

}
