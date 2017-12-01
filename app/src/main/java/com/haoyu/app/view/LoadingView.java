package com.haoyu.app.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
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
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class LoadingView extends FrameLayout {
    private ImageView iv_loading;
    private TextView mLoadTextView;
    private AnimationDrawable mAnimation;
    private String mLoadText;

    public LoadingView(Context context) {
        super(context);
        initAnimation();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAnimation();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAnimation();
    }

    private void initAnimation() {
        mAnimation = new AnimationDrawable();
        Drawable frame_1 = ContextCompat.getDrawable(getContext(), R.drawable.qb_tenpay_loading_1);
        Drawable frame_2 = ContextCompat.getDrawable(getContext(), R.drawable.qb_tenpay_loading_2);
        Drawable frame_3 = ContextCompat.getDrawable(getContext(), R.drawable.qb_tenpay_loading_3);
        Drawable frame_4 = ContextCompat.getDrawable(getContext(), R.drawable.qb_tenpay_loading_4);
        Drawable frame_5 = ContextCompat.getDrawable(getContext(), R.drawable.qb_tenpay_loading_5);
        Drawable frame_6 = ContextCompat.getDrawable(getContext(), R.drawable.qb_tenpay_loading_6);
        Drawable frame_7 = ContextCompat.getDrawable(getContext(), R.drawable.qb_tenpay_loading_7);
        Drawable frame_8 = ContextCompat.getDrawable(getContext(), R.drawable.qb_tenpay_loading_8);
        Drawable frame_9 = ContextCompat.getDrawable(getContext(), R.drawable.qb_tenpay_loading_9);
        Drawable frame_10 = ContextCompat.getDrawable(getContext(), R.drawable.qb_tenpay_loading_10);
        mAnimation.addFrame(frame_1, 100);
        mAnimation.addFrame(frame_2, 100);
        mAnimation.addFrame(frame_3, 100);
        mAnimation.addFrame(frame_4, 100);
        mAnimation.addFrame(frame_5, 100);
        mAnimation.addFrame(frame_6, 100);
        mAnimation.addFrame(frame_7, 100);
        mAnimation.addFrame(frame_8, 100);
        mAnimation.addFrame(frame_9, 100);
        mAnimation.addFrame(frame_10, 100);
        mAnimation.setOneShot(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLoadText = getResources().getString(R.string.layout_loading_text);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_loading, null);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        iv_loading = view.findViewById(R.id.iv_loading);
        mLoadTextView = view.findViewById(R.id.loadingText);
        setmAnimation(mAnimation);
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

    public void setmAnimation(AnimationDrawable mAnimation) {
        this.mAnimation = mAnimation;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            iv_loading.setBackground(mAnimation);
        else
            iv_loading.setBackgroundDrawable(mAnimation);
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
