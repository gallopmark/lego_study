package com.haoyu.app.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/4/12 on 10:04
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class DateTimePickerDialog extends AlertDialog {
    private Context context;
    private View contentView;
    private int width;
    private TextView tv_title;
    private CharSequence title;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button bt_cancel, bt_makesure;
    private boolean showTime;

    public DateTimePickerDialog(Context context) {
        super(context, R.style.datePickerDialog);
        this.context = context;
        contentView = getLayoutInflater().inflate(R.layout.dialog_datetimepicker, null);
        initView();
    }

    public DateTimePickerDialog(Context context, boolean showTime) {
        super(context, R.style.datePickerDialog);
        this.context = context;
        this.showTime = showTime;
        contentView = getLayoutInflater().inflate(R.layout.dialog_datetimepicker, null);
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        width = ScreenUtils.getScreenWidth(context) / 4 * 3;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        setContentView(contentView, params);
    }

    private void initView() {
        tv_title = contentView.findViewById(R.id.tv_title);
        datePicker = contentView.findViewById(R.id.datePicker);
        timePicker = contentView.findViewById(R.id.timePicker);
        if (showTime) {
            timePicker.setVisibility(View.VISIBLE);
        } else
            timePicker.setVisibility(View.GONE);
        timePicker.setIs24HourView(true);
        bt_cancel = contentView.findViewById(R.id.bt_cancel);
        bt_makesure = contentView.findViewById(R.id.bt_makesure);
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        bt_makesure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dateListener != null) {
                    dateListener.date(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        dateListener.time(timePicker.getHour(), timePicker.getMinute());
                    else
                        dateListener.time(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                }
                dismiss();
            }
        });
    }

    @Override
    public void setTitle(CharSequence title) {
        tv_title.setText(title);
    }

    public void setPositiveButton(CharSequence text) {
        bt_makesure.setText(text);
    }

    public void setNegativeButton(CharSequence text) {
        bt_cancel.setText(text);
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
        if (!showTime)
            params = new LinearLayout.LayoutParams(width / 3, LinearLayout.LayoutParams.WRAP_CONTENT);
        else
            params = new LinearLayout.LayoutParams(width / 7, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(PixelFormat.dp2px(context, 12), 0, 0, 0);
        np.setLayoutParams(params);
    }


    public interface DateListener {
        void date(int year, int month, int day);

        void time(int hour, int minute);
    }

    public DateListener dateListener;

    public void setDateListener(DateListener dateListener) {
        this.dateListener = dateListener;
    }
}
