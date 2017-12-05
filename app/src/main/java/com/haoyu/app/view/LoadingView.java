package com.haoyu.app.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.lego.student.R;


/**
 * 创建日期：2016/11/10 on 10:19
 * 描述:正在加载视图
 * 作者:马飞奔 Administrator
 */
public class LoadingView extends FrameLayout {
    private ImageView iv_loading;
    private TextView mLoadTextView;
    private AnimationDrawable mAnimation;
    private String mLoadText;

    public LoadingView(Context context) {
        super(context);
        initView();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mLoadText = getResources().getString(R.string.layout_loading_text);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_loading, null);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        iv_loading = view.findViewById(R.id.iv_loading);
        mLoadTextView = view.findViewById(R.id.loadingText);
        mAnimation = (AnimationDrawable) iv_loading.getDrawable();
        startAnimation();
        setLoadingText(mLoadText);
        addView(view, layoutParams);
    }

    public void setLoadingText(CharSequence loadingText) {
        if (TextUtils.isEmpty(loadingText)) {
            mLoadTextView.setVisibility(GONE);
        } else {
            mLoadTextView.setVisibility(VISIBLE);
        }
        mLoadTextView.setText(loadingText);
    }

    public void setLoadingTextColor(int color) {
        mLoadTextView.setTextColor(color);
    }

    public void setLoadingTextSize(int textSize) {
        mLoadTextView.setTextSize(textSize);
    }

    public void startAnimation() {
        if (mAnimation != null && !mAnimation.isRunning())
            mAnimation.start();
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == VISIBLE) {
            if (mAnimation != null && !mAnimation.isRunning())
                mAnimation.start();
        } else {
            if (mAnimation != null && mAnimation.isRunning())
                mAnimation.stop();
        }
        super.setVisibility(visibility);
    }
}
