package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.download.DownloadManager;
import com.haoyu.app.download.DownloadTask;
import com.haoyu.app.download.db.DownloadDBManager;
import com.haoyu.app.download.db.DownloadFileInfo;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.ProgressWebView;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 创建日期：2017/9/5 on 15:07
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CoursewareLinkActivity extends BaseActivity {
    private CoursewareLinkActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.ll_tips)
    LinearLayout ll_tips;
    @BindView(R.id.tv_tips)
    TextView tv_tips;
    @BindView(R.id.tv_close)
    TextView tv_close;
    @BindView(R.id.fl_link)
    FrameLayout fl_link;
    @BindView(R.id.ll_failure)
    LinearLayout ll_failure;
    private ProgressWebView webView;
    private DownloadDBManager dbManager;
    private boolean running, needUpload;
    private int viewNum, needViewNum, interval;    //已观看次数，要求观看次数，延时访问时间

    @Override
    public int setLayoutResID() {
        return R.layout.activity_courseware_link;
    }

    @Override
    public void initView() {
        String url = getIntent().getStringExtra("link");
        String title = getIntent().getStringExtra("title");
        running = getIntent().getBooleanExtra("running", false);
        needUpload = getIntent().getBooleanExtra("needUpload", false);
        viewNum = getIntent().getIntExtra("viewNum", 0);
        needViewNum = getIntent().getIntExtra("needViewNum", 0);
        interval = getIntent().getIntExtra("interval", 12);
        toolBar.setTitle_text(title);
        showTips();
        configWebview(url);
        if (running && needUpload)
            updateAttempt();
    }

    private void showTips() {
        toolBar.setShow_right_button(false);
        String message = "观看文档即可完成活动，要求观看文档 <font color='#ffa500'>" + needViewNum + "</font> 次/您已观看 " + "<font color='#ffa500'>" + viewNum + " 次。";
        tv_tips.setText(Html.fromHtml(message));
    }

    private void configWebview(String url) {
        fl_link.setVisibility(View.VISIBLE);
        webView = new ProgressWebView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(params);
        fl_link.addView(webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                try {
                    return handleOkHttp(url);
                } catch (Exception e) {
                    return super.shouldInterceptRequest(view, url);
                }
            }

            private WebResourceResponse handleOkHttp(String url) throws Exception {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .addHeader("Referer", Constants.REFERER)
                        .url(url).build();
                Call call = client.newCall(request);
                final Response response = call.execute();
                String mimeType = response.header("content-type", response.body().contentType().type());
                if (mimeType != null && mimeType.contains(";"))
                    mimeType = mimeType.substring(0, mimeType.indexOf(";"));
                String encoding = response.body().contentType().charset().name();
                return new WebResourceResponse(mimeType, encoding, response.body().byteStream());
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                return super.shouldOverrideKeyEvent(view, event);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                fl_link.setVisibility(View.GONE);
                ll_failure.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                fl_link.setVisibility(View.GONE);
                ll_failure.setVisibility(View.VISIBLE);
            }
        });
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.loadUrl(url);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                onDownload(url);
            }
        });
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {  //表示按返回键
                        webView.goBack();   //后退
                        return true;    //已处理
                    }
                }
                return false;
            }
        });
    }

    private void onDownload(final String url) {
        dbManager = new DownloadDBManager(context);
        String savePath = dbManager.search(url);
        if (savePath != null && new File(savePath).exists()) {
            Common.openFile(context, new File(savePath));
        } else {
            if (NetStatusUtil.isConnected(context)) {
                String message;
                if (NetStatusUtil.isWifi(context))
                    message = "创建下载链接：" + url;
                else
                    message = "当前非Wifi网络环境，下载文件会消耗过多的流量！是否创建下载链接：" + url;
                downloadTips(url, message);
            } else {
                toast(context, "当前网络不稳定，请检查网络设置！");
            }
        }
    }

    private void downloadTips(final String url, String message) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("下载提示");
        dialog.setMessage(message);
        dialog.setNegativeButton("取消", null);
        dialog.setPositiveButton("下载", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                download(url);
            }
        });
        dialog.setCancelable(true);
        dialog.show();
    }

    private void download(final String url) {
        String fileName = Common.getFileName(url);
        View contentView = getLayoutInflater().inflate(R.layout.dialog_download, null);
        TextView tv_fileName = contentView.findViewById(R.id.tv_fileName);
        final ProgressBar mProgressBar = contentView.findViewById(R.id.mRrogressBar);
        Button bt_close = contentView.findViewById(R.id.bt_close);
        TextView tv_download = contentView.findViewById(R.id.tv_download);
        final TextView tv_progress = contentView.findViewById(R.id.tv_progress);
        tv_fileName.setText(fileName);
        tv_download.setText("正在下载");
        tv_progress.setText("0%");
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((ScreenUtils.getScreenWidth(context) / 6 * 5), LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(contentView, params);
        DownloadManager.getInstance().create(url).setFilePath(Constants.fileDownDir).setFileName(fileName).addListener(new com.haoyu.app.download.DownloadListener() {
            @Override
            public void onProgress(DownloadTask downloadTask, long soFarBytes, long totalBytes) {
                mProgressBar.setProgress((int) soFarBytes);
                mProgressBar.setMax((int) totalBytes);
                tv_progress.setText(accuracy(soFarBytes, totalBytes));
            }

            @Override
            public void onSuccess(DownloadTask downloadTask, String savePath) {
                dialog.dismiss();
                if (new File(savePath).exists())
                    Common.openFile(context, new File(savePath));
                else
                    toast(context, "下载的文件已被删除");
                DownloadFileInfo fileInfo = new DownloadFileInfo();
                fileInfo.setFileName(downloadTask.getFileName());
                fileInfo.setUrl(downloadTask.getUrl());
                fileInfo.setFilePath(savePath);
                dbManager.save(fileInfo);
            }

            @Override
            public void onFailed(DownloadTask downloadTask) {
                dialog.dismiss();
                toast(context, "下载失败");
            }

            @Override
            public void onPaused(DownloadTask downloadTask) {
                dialog.dismiss();
            }

            @Override
            public void onCancel(DownloadTask downloadTask) {
                dialog.dismiss();
            }
        }).start();
        bt_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                DownloadManager.getInstance().pause(url);
            }
        });
    }

    private String accuracy(double num, double total) {
        try {
            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
            //可以设置精确几位小数
            df.setMaximumFractionDigits(0);
            //模式 例如四舍五入
            df.setRoundingMode(RoundingMode.HALF_UP);
            double accuracy_num = num / total * 100;
            return df.format(accuracy_num) + "%";
        } catch (Exception e) {
            return num / total * 100 + "%";
        }
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
        ll_failure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fl_link.setVisibility(View.VISIBLE);
                ll_failure.setVisibility(View.GONE);
                webView.reload();
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
        fl_link.removeAllViews();
        if (webView != null) {
            webView.stopLoading();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
    }
}
