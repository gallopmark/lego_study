package com.haoyu.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.dialog.DateTimePickerDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.MEvaluateItem;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.FullyLinearLayoutManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/12/5.
 * 描述:听课评课添加评分项和设置活动时间
 * 作者:xiaoma
 */

public class WSTSSubmitFragment extends BaseFragment implements View.OnClickListener {
    private Activity context;
    @BindView(R.id.iv_addScore)
    ImageView iv_addScore;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.ll_start_time)
    LinearLayout ll_start_time;
    @BindView(R.id.tv_start)
    TextView tv_start;
    @BindView(R.id.ll_end_time)
    LinearLayout ll_end_time;
    @BindView(R.id.tv_end)
    TextView tv_end;
    private List<MEvaluateItem> items = new ArrayList<>();
    private MEvaluateItemAdapter adapter;
    private String workshopId, activityId, userId;
    private int startYear, startMonth, startDay;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (Activity) context;
        Bundle bundle = getArguments();
        workshopId = bundle.getString("workshopId");
        activityId = bundle.getString("activityId");
        userId = getUserId();
    }

    @Override
    public int createView() {
        return R.layout.fragment_wstssubmit;
    }

    @Override
    public void initView(View view) {
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
        layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MEvaluateItemAdapter(items);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void setListener() {
        iv_addScore.setOnClickListener(this);
        ll_start_time.setOnClickListener(this);
        ll_end_time.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_addScore:
                showInputDialog();
                break;
            case R.id.ll_start_time:
                setTime(tv_start, 1);
                break;
            case R.id.ll_end_time:
                setTime(tv_end, 2);
                break;
        }
    }

    private void showInputDialog() {
        CommentDialog dialog = new CommentDialog(context, "输入评分项", "提交");
        dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
            @Override
            public void sendComment(String content) {
                addScoreItem(content);
            }
        });
        dialog.show();
    }

    /*添加评分项*/
    private void addScoreItem(final String content) {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + userId + "/m/evaluate_item";
        Map<String, String> map = new HashMap<>();
        map.put("aid", activityId);
        map.put("content", content);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MEvaluateItem>>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError();
            }

            @Override
            public void onResponse(BaseResponseResult<MEvaluateItem> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    updateItem(response.getResponseData());
                }
            }
        }, map));
    }

    private void updateItem(final MEvaluateItem item) {
        items.add(item);
        adapter.notifyDataSetChanged();
    }

    private void setTime(final TextView tv, final int type) {
        DateTimePickerDialog dialog = new DateTimePickerDialog(context);
        if (type == 1) {
            dialog.setTitle("选择开始时间");
        } else {
            dialog.setTitle("选择结束时间");
        }
        dialog.setPositiveButton("确定");
        dialog.setDateListener(new DateTimePickerDialog.DateListener() {
            @Override
            public void Date(int year, int month, int day) {
                String mMonth = month < 10 ? "0" + month : String.valueOf(month);
                String mDay = day < 10 ? "0" + day : String.valueOf(day);
                String text = year + "-" + mMonth + "-" + mDay;
                if (type == 1) {
                    startYear = year;
                    startMonth = month;
                    startDay = day;
                    if (checkStartTime(year, month, day)) {
                        tv.setText(text);
                    }
                } else {
                    if (checkEndTime(year, month, day)) {
                        tv.setText(text);
                    }
                }
            }

            @Override
            public void Time(int hour, int minute) {

            }
        });
        dialog.show();
    }

    private boolean checkStartTime(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        int nowYear = c.get(Calendar.YEAR);
        int nowMonth = c.get(Calendar.MONTH) + 1;
        int nowDay = c.get(Calendar.DAY_OF_MONTH);
        String message = "活动开始时间不能是";
        if (year < nowYear) {
            message += nowYear + "年前";
            showMaterialDialog(message);
            return false;
        } else {
            if (month < nowMonth) {
                message += nowYear + "年" + nowMonth + "月前";
                showMaterialDialog(message);
                return false;
            } else {
                if (day < nowDay) {
                    message += nowYear + "年" + nowMonth + "月" + nowDay + "日前";
                    showMaterialDialog(message);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkEndTime(int endYear, int endMonth, int endDay) {
        if (startYear == 0 || startMonth == 0 || startDay == 0) {
            showMaterialDialog("请先设置开始时间");
            return false;
        }
        String message = "活动结束时间不能是";
        if (endYear < startYear) {
            message += startYear + "年前";
            showMaterialDialog(message);
            return false;
        } else {
            if (endMonth < startMonth) {
                message += startYear + "年" + startMonth + "月前";
                showMaterialDialog(message);
                return false;
            } else {
                if (endDay < startDay) {
                    message += startYear + "年" + startMonth + "月" + startDay + "日前";
                    showMaterialDialog(message);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isAddItem() {
        if (items.size() > 0) {
            return true;
        }
        return false;
    }

    public String getStartTime() {
        return tv_start.getText().toString().trim();
    }

    public String getEndTime() {
        return tv_end.getText().toString().trim();
    }

    private void showMaterialDialog(String message) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage(message);
        dialog.setPositiveButton("确定", null);
        dialog.show();
    }

    private class MEvaluateItemAdapter extends BaseArrayRecyclerAdapter<MEvaluateItem> {

        public MEvaluateItemAdapter(List<MEvaluateItem> mDatas) {
            super(mDatas);
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.wsts_addscore;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, final MEvaluateItem item, final int position) {
            TextView tv_score = holder.obtainView(R.id.tv_score);
            ImageView iv_delete = holder.obtainView(R.id.iv_delete);
            tv_score.setText(item.getContent());
            iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteScoreItem(position, item.getId());
                }
            });
        }

        /*删除评分项*/
        private void deleteScoreItem(final int position, String itemId) {
            String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + userId + "/m/evaluate_item/" + itemId;
            addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
                @Override
                public void onBefore(Request request) {
                    showTipDialog();
                }

                @Override
                public void onError(Request request, Exception e) {
                    hideTipDialog();
                    onNetWorkError();
                }

                @Override
                public void onResponse(BaseResponseResult response) {
                    hideTipDialog();
                    if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                        mDatas.remove(position);
                        notifyDataSetChanged();
                    }
                }
            }, new OkHttpClientManager.Param("_method", "delete")));
        }
    }
}
