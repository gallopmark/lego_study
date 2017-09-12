package com.haoyu.app.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.ScreenUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 创建日期：2017/4/12 on 10:04
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class DatePickerDialog extends AlertDialog implements View.OnClickListener {

    private DatePicker startDatePicker, endDatePicker;
    private Context context;
    private int startYear, endYear, startMonth, endMonth, startDay, endDay;
    private int defaultYear, defaultMonth, defaultDay;
    private OnDatePickerListener datePickerListener;
    private int width;
    private boolean hideDay;

    public DatePickerDialog(Context context) {
        super(context, R.style.datePickerDialog);
        this.context = context;
    }

    public DatePickerDialog(Context context, boolean hideDay) {
        super(context, R.style.datePickerDialog);
        this.context = context;
        this.hideDay = hideDay;
    }

    public DatePickerDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, R.style.datePickerDialog);
        this.context = context;
    }

    public DatePickerDialog(Context context, @StyleRes int themeResId) {
        super(context, R.style.datePickerDialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        width = ScreenUtils.getScreenWidth(context) / 8 * 7;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        View view = getLayoutInflater().inflate(R.layout.dialog_datepicker, null);
        initView(view);
        setContentView(view, params);
    }

    private void initView(View view) {
        startDatePicker = view.findViewById(R.id.start_date_picker);
        endDatePicker = view.findViewById(R.id.end_date_picker);
        Button tv_cancel = view.findViewById(R.id.bt_cancel);
        Button tv_confirm = view.findViewById(R.id.bt_makesure);
        resizePikcer(startDatePicker);
        resizePikcer(endDatePicker);
        if (hideDay) {
            hideDay(startDatePicker);
            hideDay(endDatePicker);
        }
        initDate();
        tv_cancel.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);
    }

    private void initDate() {
        // 获取当前的年、月、日、小时、分钟
        Calendar c = Calendar.getInstance();
        defaultYear = c.get(Calendar.YEAR);
        defaultMonth = c.get(Calendar.MONTH);
        defaultDay = c.get(Calendar.DAY_OF_MONTH);
        startYear = endYear = defaultYear;
        startMonth = endMonth = defaultMonth + 1;
        startDay = endDay = defaultDay;
        startDatePicker.init(defaultYear, defaultMonth, defaultDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
                startYear = year;
                startMonth = month + 1;
                startDay = day;
            }
        });

        endDatePicker.init(defaultYear, defaultMonth, defaultDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
                endYear = year;
                endMonth = month + 1;
                endDay = day;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_cancel:
                dismiss();
                break;
            case R.id.bt_makesure:
                if (checkDate()) {
                    if (datePickerListener != null) {
                        datePickerListener.datePicker(startYear, startMonth, startDay,
                                endYear, endMonth, endDay);
                    }
                    dismiss();
                }
                break;
        }
    }

    private boolean checkDate() {
        if (endYear < startYear || (endYear == startYear && endMonth < startMonth)
                || (endYear < defaultYear) || (endYear == defaultYear && endMonth < defaultMonth)) {
            toastMsg("<p>您选择的时间不符合要求，请重新选择！<br />" +
                    "时间选择要求：<br />1.结束年份不可以小于开始年份；<br />" +
                    "2.结束年份不可以小于当前年份；<br />" +
                    "3.当选择的年份相同时，选择的结束月份不可以小于开始月份。</p>");
            return false;
        }
        return true;
    }

    private void toastMsg(String msg) {
        Spanned spanned = Html.fromHtml(msg);
        DatePickerDialog.this.dismiss();
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage(spanned);
        dialog.setPositiveButton("重新选择", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                if (!DatePickerDialog.this.isShowing()) {
                    DatePickerDialog.this.show();
                }
            }
        });
        dialog.setNegativeButton("取消选择", null);
        dialog.show();
    }

    /**
     * 调整FrameLayout大小
     *
     * @param tp
     */
    private void resizePikcer(FrameLayout tp) {
        List<NumberPicker> npList = findNumberPicker(tp);
        for (NumberPicker np : npList) {
            resizeNumberPicker(np);
        }
    }

    /**
     * 得到viewGroup里面的numberpicker组件
     *
     * @param viewGroup
     * @return
     */
    private List<NumberPicker> findNumberPicker(ViewGroup viewGroup) {
        List<NumberPicker> npList = new ArrayList<>();
        View child;
        if (null != viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                child = viewGroup.getChildAt(i);
                if (child instanceof NumberPicker) {
                    npList.add((NumberPicker) child);
                } else if (child instanceof LinearLayout) {
                    List<NumberPicker> result = findNumberPicker((ViewGroup) child);
                    if (result.size() > 0) {
                        return result;
                    }
                }
            }
        }
        return npList;
    }

    /*
     * 调整numberpicker大小
     */
    private void resizeNumberPicker(NumberPicker np) {
        LinearLayout.LayoutParams params;
        if (hideDay) {
            params = new LinearLayout.LayoutParams(width / 5, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 0, 10, 0);
        } else {
            params = new LinearLayout.LayoutParams(width / 7, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(5, 0, 5, 0);
        }
        params.gravity = Gravity.CENTER;
        np.setLayoutParams(params);
    }

    private void hideDay(DatePicker mDatePicker) {
        try {
            /* 处理android5.0以上的特殊情况 */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int daySpinnerId = Resources.getSystem().getIdentifier("day", "id", "android");
                if (daySpinnerId != 0) {
                    View daySpinner = mDatePicker.findViewById(daySpinnerId);
                    if (daySpinner != null) {
                        daySpinner.setVisibility(View.GONE);
                    }
                }
            } else {
                Field[] datePickerfFields = mDatePicker.getClass().getDeclaredFields();
                for (Field datePickerField : datePickerfFields) {
                    if ("mDaySpinner".equals(datePickerField.getName()) || ("mDayPicker").equals(datePickerField.getName())) {
                        datePickerField.setAccessible(true);
                        Object dayPicker = new Object();
                        try {
                            dayPicker = datePickerField.get(mDatePicker);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                        ((View) dayPicker).setVisibility(View.GONE);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDatePickerListener(OnDatePickerListener datePickerListener) {
        this.datePickerListener = datePickerListener;
    }

    public interface OnDatePickerListener {
        void datePicker(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay);
    }
}
