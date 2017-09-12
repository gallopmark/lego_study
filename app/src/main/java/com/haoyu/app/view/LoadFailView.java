package com.haoyu.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.haoyu.app.lego.student.R;


/**
 * 创建日期：2016/11/10 on 14:30
 * 描述:
 * 作者:马飞奔 Administrator
 */
/*自定义加载重试布局*/
public class LoadFailView extends FrameLayout implements View.OnClickListener {
    private String errorMsg;       //加载错误信息
    private TextView mErrorTextView;
    private Button bt_retry;    //重试按钮
    private OnRetryListener onRetryListener; //点击重试按钮监听器

    public LoadFailView(Context context) {
        super(context);
        init(context);
    }

    public LoadFailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadFailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        errorMsg = getResources().getString(R.string.load_fail_message);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_load_fail, null);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.gravity = Gravity.CENTER;

        mErrorTextView = view.findViewById(R.id.error_msg);
        bt_retry = view.findViewById(R.id.bt_retry);
        bt_retry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(View.GONE);
                if (onRetryListener != null) {
                    onRetryListener.onRetry(LoadFailView.this);
                }
            }
        });
        view.setOnClickListener(this);
        setErrorMsg(errorMsg);
        addView(view, layoutParams);
    }

    /*设置加载错误信息*/
    public void setErrorMsg(String errorMsg) {
        mErrorTextView.setText(errorMsg);
    }

    public void setRetryText(String retryText) {
        bt_retry.setText(retryText);
    }

    public void setOnRetryListener(OnRetryListener onRetryListener) {
        this.onRetryListener = onRetryListener;
    }

    @Override
    public void onClick(View view) {
        setVisibility(View.GONE);
        if (onRetryListener != null) {
            onRetryListener.onRetry(LoadFailView.this);
        }
    }

    public interface OnRetryListener {
        void onRetry(View v);
    }
}
