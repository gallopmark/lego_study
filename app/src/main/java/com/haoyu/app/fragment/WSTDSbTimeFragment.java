package com.haoyu.app.fragment;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.dialog.DateTimePickerDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.lego.student.R;

import java.util.Calendar;

import butterknife.BindView;

/**
 * 创建日期：2017/12/5.
 * 描述:工作坊设置教学研讨活动时间（开始时间，结束时间）
 * 作者:xiaoma
 */

public class WSTDSbTimeFragment extends BaseFragment implements View.OnClickListener {
    @BindView(R.id.ll_startTime)
    LinearLayout ll_startTime;
    @BindView(R.id.tv_start)
    TextView tv_start;
    @BindView(R.id.ll_endTime)
    LinearLayout ll_endTime;
    @BindView(R.id.tv_end)
    TextView tv_end;
    private int startYear, startMonth, startDay;

    @Override
    public int createView() {
        return R.layout.fragment_wstdsbtime;
    }

    @Override
    public void setListener() {
        ll_startTime.setOnClickListener(this);
        ll_endTime.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_startTime:
                setTime(tv_start, 1);
                break;
            case R.id.ll_endTime:
                setTime(tv_end, 2);
                break;
        }
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
        } else if (year == nowYear) {
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
        } else if (endYear == startYear) {
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
}
