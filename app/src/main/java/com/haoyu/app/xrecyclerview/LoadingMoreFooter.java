package com.haoyu.app.xrecyclerview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.lego.student.R;

public class LoadingMoreFooter extends LinearLayout {

    private LinearLayout footerContainer;
    //    private CircularProgressView progressView;
    public final static int STATE_LOADING = 0;
    public final static int STATE_COMPLETE = 1;
    public final static int STATE_NOMORE = 2;
    public final static int STATE_FAILURE = 3;
    public final static int STATE_NORMAL = 4;
    private ImageView iv_loading;
    private AnimationDrawable animationDrawable;
    private TextView mText;
    private String normalHint;
    private String loadingHint;
    private String noMoreHint;
    private String loadingDoneHint;
    private String loadingFailure;
    private int STATE = STATE_NORMAL;

    public LoadingMoreFooter(Context context) {
        super(context);
        initView();
    }

    /**
     * @param context
     * @param attrs
     */
    public LoadingMoreFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void setLoadingHint(String hint) {
        loadingHint = hint;
    }

    public void setNoMoreHint(String hint) {
        noMoreHint = hint;
    }

    public void setLoadingDoneHint(String hint) {
        loadingDoneHint = hint;
    }

    public void initView() {
        footerContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(
                R.layout.xrecyclerview_footer, null);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        footerContainer.setLayoutParams(params);
        setGravity(Gravity.CENTER);
        setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(footerContainer);
//        progressView = (CircularProgressView) findViewById(progressView);
//        progressView.setVisibility(View.GONE);
        iv_loading = (ImageView) findViewById(R.id.listview_foot_loading);
        animationDrawable = (AnimationDrawable) iv_loading.getDrawable();
        iv_loading.setVisibility(View.GONE);
        mText = (TextView) findViewById(R.id.listview_foot_more);
//        mText = new TextView(getContext());
//        mText.setText("正在加载...");
        normalHint = (String) getContext().getText(R.string.click_to_loadmore);
        loadingHint = (String) getContext().getText(R.string.xrecyclerview_footer_loading);
        noMoreHint = (String) getContext().getText(R.string.xrecyclerview_footer_never_hasdata);
        loadingDoneHint = (String) getContext().getText(R.string.loading_done);
        loadingFailure = (String) getContext().getText(R.string.load_fail_message);
        mText.setText(normalHint);
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.setMargins( (int)getResources().getDimension(R.dimen.textandiconmargin),0,0,0 );
//
//        mText.setLayoutParams(layoutParams);
//        addView(mText);
    }

    public void setProgressStyle(int style) {
        if (style == ProgressStyle.SysProgress) {
//            progressCon.setView(new ProgressBar(getContext(), null, android.R.attr.progressBarStyle));
        } else {
//            AVLoadingIndicatorView progressView = new AVLoadingIndicatorView(this.getContext());
//            progressView.setIndicatorColor(0xffB5B5B5);
//            progressView.setIndicatorId(style);
//            progressCon.setView(progressView);
        }
    }

    public void setState(int state) {
        STATE = state;
        switch (state) {
            case STATE_LOADING:
//                progressView.setVisibility(View.VISIBLE);
                iv_loading.setVisibility(View.VISIBLE);
                if (animationDrawable != null && !animationDrawable.isRunning()) {
                    animationDrawable.start();
                }
                mText.setText(loadingHint);
                this.setVisibility(View.VISIBLE);
                break;
            case STATE_COMPLETE:
                mText.setText(loadingDoneHint);
                if (animationDrawable != null && animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
                this.setVisibility(View.GONE);
                break;
            case STATE_NOMORE:
                mText.setText(noMoreHint);
//                progressView.setVisibility(View.GONE);
                iv_loading.setVisibility(View.GONE);
                if (animationDrawable != null && animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
                this.setVisibility(View.GONE);
                break;
            case STATE_FAILURE:
                mText.setText(loadingFailure);
//                progressView.setVisibility(View.GONE);
                iv_loading.setVisibility(View.GONE);
                if (animationDrawable != null && animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
                this.setVisibility(View.VISIBLE);
                break;
            case STATE_NORMAL:
                mText.setText(normalHint);
//                progressView.setVisibility(View.GONE);
                iv_loading.setVisibility(View.GONE);
                if (animationDrawable != null && animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
                this.setVisibility(View.VISIBLE);
                break;
        }
    }

    public int getState() {
        return STATE;
    }
}
