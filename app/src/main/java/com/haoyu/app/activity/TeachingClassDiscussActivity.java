package com.haoyu.app.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.fragment.VideoPlayerFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;

import butterknife.BindView;

/**
 * 创建日期：2017/11/2.
 * 描述:评课议课
 * 作者:Administrator
 */
public class TeachingClassDiscussActivity extends BaseActivity {
    private TeachingClassDiscussActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.ll_layout)
    LinearLayout ll_layout;
    @BindView(R.id.tv_time)
    TextView tv_time;

    /*videoplayer layout*/
    @BindView(R.id.fl_video)
    FrameLayout fl_video;

    @BindView(R.id.ll_videoOutSide)
    LinearLayout ll_videoOutSide;
    private String videoUrl;
    private int smallHeight;
    private VideoPlayerFragment videoFragment;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teachclass_comment;
    }

    @Override
    public void initView() {
        videoUrl = getIntent().getStringExtra("videoUrl");
        String title = getIntent().getStringExtra("title");
        if (title != null)
            toolBar.setTitle_text(title);
        else
            toolBar.setTitle_text("评课议课");
        smallHeight = ScreenUtils.getScreenHeight(context) / 5 * 2;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, smallHeight);
        fl_video.setLayoutParams(params);
        videoFragment = new VideoPlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("videoUrl", videoUrl);
        videoFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fl_video, videoFragment).commit();
        videoFragment.setOnRequestedOrientation(new VideoPlayerFragment.OnRequestedOrientation() {
            @Override
            public void onRequested(int orientation) {
                setRequestedOrientation(orientation);
            }
        });
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 判断Android当前的屏幕是横屏还是竖屏。横竖屏判断
        setOrientattion(newConfig.orientation);
    }


    private void setOrientattion(int orientation) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {   //竖屏
            if (videoFragment != null) {
                videoFragment.setFullScreen(false);
            }
            showOutSize();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, smallHeight);
            fl_video.setLayoutParams(params);
        } else { //横屏
            if (videoFragment != null) {
                videoFragment.setFullScreen(true);
            }
            hideOutSize();
            int screeWidth = ScreenUtils.getScreenWidth(context);
            int screenHeight = ScreenUtils.getScreenHeight(context);
            int statusHeight = ScreenUtils.getStatusHeight(context);
            int realHeight = screenHeight - statusHeight;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screeWidth, realHeight);
            fl_video.setLayoutParams(params);
        }
    }

    private void showOutSize() {
        tv_time.setVisibility(View.VISIBLE);
        toolBar.setVisibility(View.VISIBLE);
        ll_videoOutSide.setVisibility(View.VISIBLE);
    }

    private void hideOutSize() {
        toolBar.setVisibility(View.GONE);
        tv_time.setVisibility(View.GONE);
        ll_videoOutSide.setVisibility(View.GONE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && videoFragment != null) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
