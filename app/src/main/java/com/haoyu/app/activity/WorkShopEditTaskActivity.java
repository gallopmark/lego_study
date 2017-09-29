package com.haoyu.app.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Selection;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.TimePeriod;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/4/13 on 15:00
 * 描述:教师工作坊修改阶段任务
 * 作者:马飞奔 Administrator
 */
public class WorkShopEditTaskActivity extends BaseActivity implements View.OnClickListener {
    private WorkShopEditTaskActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.tv_picker)
    TextView tv_picker;
    @BindView(R.id.pickerContent)
    View pickerContent;
    @BindView(R.id.startDatePicker)
    DatePicker startDatePicker;
    @BindView(R.id.endDatePicker)
    DatePicker endDatePicker;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;
    @BindView(R.id.tv_confirm)
    TextView tv_confirm;
    private boolean showPicker;
    private int startYear, endYear, startMonth, endMonth, startDay, endDay;
    private int defaultYear, defaultMonth, defaultDay;
    private String startTime, endTime, startDesc, endDesc;
    private String workShopId, taskId, oldTitle, oldDate;
    private long oldStartTime, oldEndTime;
    private boolean hasOldDate;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_workshop_edittask;
    }

    @Override
    public void initView() {
        workShopId = getIntent().getStringExtra("workShopId");
        taskId = getIntent().getStringExtra("relationId");
        oldTitle = getIntent().getStringExtra("title");
        oldStartTime = getIntent().getLongExtra("startTime", -1);
        oldEndTime = getIntent().getLongExtra("endTime", -1);
        if (oldTitle != null && oldTitle.length() > 0) {
            et_title.setText(oldTitle);
            Editable editable = et_title.getText();
            Selection.setSelection(editable, editable.length());
        }
        if (oldStartTime != -1 && oldEndTime != -1) {
            oldDate = "研修时间：" + TimeUtil.getDateYM(oldStartTime) + "-" + TimeUtil.getDateYM(oldEndTime);
            tv_picker.setText(oldDate);
            hasOldDate = true;
        }
        resizePikcer(startDatePicker);
        resizePikcer(endDatePicker);
        hideDay(startDatePicker);
        hideDay(endDatePicker);
    }

    @Override
    public void initData() {
        // 获取当前的年、月、日、小时、分钟
        Calendar c = Calendar.getInstance();
        defaultYear = c.get(Calendar.YEAR);
        defaultMonth = c.get(Calendar.MONTH);
        defaultDay = c.get(Calendar.DAY_OF_MONTH);
        if (hasOldDate) {
            c.setTimeInMillis(oldStartTime);
            startYear = c.get(Calendar.YEAR);
            startMonth = c.get(Calendar.MONTH);
            startDay = c.get(Calendar.DAY_OF_MONTH);
            c.setTimeInMillis(oldEndTime);
            endYear = c.get(Calendar.YEAR);
            endMonth = c.get(Calendar.MONTH);
            endDay = c.get(Calendar.DAY_OF_MONTH);
        } else {
            startYear = endYear = defaultYear;
            startMonth = endMonth = defaultMonth + 1;
            startDay = endDay = defaultDay;
        }
        startDesc = startYear + "年" + (startMonth < 10 ? "0" + startMonth : startMonth) + "月";
        endDesc = endYear + "年" + (endMonth < 10 ? "0" + endMonth : endMonth) + "月";
        startTime = startYear + "-" + (startMonth < 10 ? "0" + startMonth : startMonth);
        endTime = endYear + "-" + (endMonth < 10 ? "0" + endMonth : endMonth);
        startDatePicker.init(startYear, startMonth, startDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
                startYear = year;
                startMonth = month + 1;
                startTime = startYear + "-" + (startMonth < 10 ? "0" + startMonth : startMonth);
                startDesc = startYear + "年" + (startMonth < 10 ? "0" + startMonth : startMonth) + "月";
            }
        });

        endDatePicker.init(endYear, endMonth, endDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
                endYear = year;
                endMonth = month + 1;
                endTime = endYear + "-" + (endMonth < 10 ? "0" + endMonth : endMonth);
                endDesc = endYear + "年" + (endMonth < 10 ? "0" + endMonth : endMonth) + "月";
            }
        });
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
                String title = et_title.getText().toString().trim();
                String time = tv_picker.getText().toString().trim();
                if (title.length() == 0) {
                    showMaterialDialog("提示", "请输入阶段标题");
                } else if (time.length() == 0) {
                    showMaterialDialog("提示", "请选择研修时间");
                } else if (title.equals(oldTitle) && time.equals(oldDate)) {
                    finish();
                } else {
                    alterTask();
                }
            }
        });
        tv_picker.setOnClickListener(context);
        tv_cancel.setOnClickListener(context);
        tv_confirm.setOnClickListener(context);
        et_title.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // et.getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
                Drawable drawable = et_title.getCompoundDrawables()[2];
                //如果右边没有图片，不再处理
                if (drawable == null)
                    return false;
                //如果不是按下事件，不再处理
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (event.getX() > et_title.getWidth()
                        - et_title.getPaddingRight()
                        - drawable.getIntrinsicWidth()) {
                    et_title.setSelection(et_title.getText().length());//将光标移至文字末尾
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        Drawable shouqi = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(),
                shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_xiala);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(),
                zhankai.getMinimumHeight());
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_picker:
                Common.hideSoftInput(context);
                if (!showPicker) {
                    pickerContent.setVisibility(View.VISIBLE);
                    showPicker = true;
                    tv_picker.setCompoundDrawables(null, null, shouqi, null);
                } else {
                    pickerContent.setVisibility(View.GONE);
                    showPicker = false;
                    tv_picker.setCompoundDrawables(null, null, zhankai, null);
                }
                break;
            case R.id.tv_confirm:
                if (checkDate()) {
                    tv_picker.setText("研修时间：" + startDesc + "-" + endDesc);
                    pickerContent.setVisibility(View.GONE);
                    showPicker = false;
                    tv_picker.setCompoundDrawables(null, null, zhankai, null);
                }
                break;
            case R.id.tv_cancel:
                pickerContent.setVisibility(View.GONE);
                showPicker = false;
                tv_picker.setCompoundDrawables(null, null, zhankai, null);
                break;
        }
    }

    private void alterTask() {
        String url = Constants.OUTRT_NET + "/master_" + workShopId + "/unique_uid_" + getUserId() +
                "/m/workshop_section/" + taskId;
        final String title = et_title.getText().toString().trim();
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("title", title);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
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
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    Intent intent = new Intent();
                    intent.putExtra("title", title);
                    TimePeriod timePeriod = new TimePeriod();
                    timePeriod.setStartTime(TimeUtil.dateToLong(startTime, "yyyy-MM"));
                    timePeriod.setEndTime(TimeUtil.dateToLong(endTime, "yyyy-MM"));
                    intent.putExtra("timePeriod", timePeriod);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    toastFullScreen("修改失败", false);
                }
            }
        }, map));
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
        params = new LinearLayout.LayoutParams(ScreenUtils.getScreenWidth(context) / 5, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 0);
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

    private boolean checkDate() {
        if (endYear < startYear) {
            toast(context, "结束年份不可以小于开始年份");
            return false;
        }
        if (endYear == startYear && endMonth < startMonth) {
            toast(context, "结束月份不可以小于开始年月");
            return false;
        }
        if (endYear < defaultYear) {
            toast(context, "结束年份不可以小于当前年份");
            return false;
        }
        if (endYear == defaultYear && endMonth < defaultMonth) {
            toast(context, "结束月份不可以小于当前月份");
            return false;
        }
        return true;
    }

}
