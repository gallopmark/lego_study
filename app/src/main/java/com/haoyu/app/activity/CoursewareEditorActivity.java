package com.haoyu.app.activity;

import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.ProgressWebView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 创建日期：2017/9/5 on 15:22
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CoursewareEditorActivity extends BaseActivity {
    private CoursewareEditorActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.ll_tips)
    LinearLayout ll_tips;
    @BindView(R.id.tv_tips)
    TextView tv_tips;
    @BindView(R.id.tv_close)
    TextView tv_close;
    @BindView(R.id.content)
    FrameLayout content;
    ProgressWebView webView;
    private boolean running, needUpload;
    private String activityId;
    private int viewNum, needViewNum, interval;    //已观看次数，要求观看次数，延时访问时间

    @Override
    public int setLayoutResID() {
        return R.layout.activity_courseware_editor;
    }

    @Override
    public void initView() {
        running = getIntent().getBooleanExtra("running", false);
        needUpload = getIntent().getBooleanExtra("needUpload", false);
        activityId = getIntent().getStringExtra("activityId");
        String title = getIntent().getStringExtra("title");
        viewNum = getIntent().getIntExtra("viewNum", 0);
        needViewNum = getIntent().getIntExtra("needViewNum", 0);
        interval = getIntent().getIntExtra("interval", 12);
        toolBar.setTitle_text(title);
        showTips();
        String editor = getIntent().getStringExtra("editor");
        setEditor(editor);
        if (running && needUpload) {
            updateAttempt();
        }
    }

    private void showTips() {
        toolBar.setShow_right_button(false);
        String message = "观看文档即可完成活动，要求观看文档 <font color='#ffa500'>" + needViewNum + "</font> 次/您已观看 " + "<font color='#ffa500'>" + viewNum + " 次。";
        tv_tips.setText(Html.fromHtml(message));
    }

    private void setEditor(String editor) {
        content.setVisibility(View.VISIBLE);
        webView = new ProgressWebView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(params);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        content.addView(webView);
        webView.getSettings().setTextZoom(300);
        webView.loadDataWithBaseURL(null, editor, "text/html", "utf-8", null);
    }

    /**
     * 更新课件观看次数
     */
    private void updateAttempt() {
        final String activityId = getIntent().getStringExtra("activityId");
        final String mTextInfoUserId = getIntent().getStringExtra("mTextInfoUserId");
        final String url = Constants.OUTRT_NET + "/" + activityId + "/study/m/textInfo/user/updateAttempt";
        addSubscription(Flowable.timer(interval, TimeUnit.SECONDS).map(new Function<Long, BaseResponseResult>() {
            @Override
            public BaseResponseResult apply(Long aLong) throws Exception {
                Map<String, String> map = new HashMap<>();
                map.put("_method", "put");
                map.put("id", mTextInfoUserId);
                String body = OkHttpClientManager.postAsString(context, url, map);
                BaseResponseResult result = new GsonBuilder().create().fromJson(body, BaseResponseResult.class);
                return result;
            }
        }).map(new Function<BaseResponseResult, AppActivityViewResult>() {
            @Override
            public AppActivityViewResult apply(BaseResponseResult result) throws Exception {
                if (result != null && result.getResponseCode() != null && result.getResponseCode().equals("00")) {
                    return getActivityInfo(activityId);
                }
                return null;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<AppActivityViewResult>() {
            @Override
            public void accept(AppActivityViewResult response) throws Exception {
                if (response != null && response.getResponseData() != null) {
                    if (response.getResponseData().getmTextInfoUser() != null)
                        viewNum = response.getResponseData().getmTextInfoUser().getViewNum();
                    showTips();
                    showTopView();
                    if (response.getResponseData().getmActivityResult() != null && response.getResponseData().getmActivityResult().getmActivity() != null) {
                        CourseSectionActivity activity = response.getResponseData().getmActivityResult().getmActivity();
                        Intent intent = new Intent();
                        intent.putExtra("activity", activity);
                        setResult(RESULT_OK, intent);
                    }
                }
            }
        }));
    }

    private AppActivityViewResult getActivityInfo(String activityId) throws Exception {
        String url = Constants.OUTRT_NET + "/" + activityId + "/study/m/activity/ncts/" + activityId + "/view";
        String json = OkHttpClientManager.getAsString(context, url);
        AppActivityViewResult result = new Gson().fromJson(json, AppActivityViewResult.class);
        return result;
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
                showTopView();
            }
        });
        tv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideTopView();
            }
        });
    }

    private void showTopView() {
        ll_tips.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.scale_show);
        if (animation != null) {
            ll_tips.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    toolBar.setShow_right_button(false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void hideTopView() {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.scale_hide);
        if (animation != null) {
            ll_tips.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ll_tips.setVisibility(View.GONE);
                    toolBar.setShow_right_button(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            ll_tips.setVisibility(View.GONE);
            toolBar.setShow_right_button(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers(); //小心这个！！！暂停整个 WebView 所有布局、解析、JS。
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        content.removeAllViews();
        if (webView != null) {
            webView.stopLoading();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
    }
}
