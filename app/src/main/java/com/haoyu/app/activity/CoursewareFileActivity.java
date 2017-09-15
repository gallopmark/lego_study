package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.download.DownloadListener;
import com.haoyu.app.download.DownloadManager;
import com.haoyu.app.download.DownloadTask;
import com.haoyu.app.download.db.DownloadDBManager;
import com.haoyu.app.download.db.DownloadFileInfo;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.MediaFile;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.RoundRectProgressBar;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * 创建日期：2017/9/5 on 14:25
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CoursewareFileActivity extends BaseActivity {
    private CoursewareFileActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.ll_tips)
    LinearLayout ll_tips;
    @BindView(R.id.tv_tips)
    TextView tv_tips;
    @BindView(R.id.tv_close)
    TextView tv_close;
    @BindView(R.id.ll_fileInfo)
    LinearLayout ll_fileInfo;
    @BindView(R.id.iv_type)
    ImageView iv_type;
    @BindView(R.id.tv_fileName)
    TextView tv_fileName;
    @BindView(R.id.bt_download)
    Button bt_download;
    @BindView(R.id.ll_downloadInfo)
    LinearLayout ll_downloadInfo;
    @BindView(R.id.tv_downloadInfo)
    TextView tv_downloadInfo;
    @BindView(R.id.progressBar)
    RoundRectProgressBar progressBar;
    @BindView(R.id.iv_pause)
    ImageView iv_pause;
    @BindView(R.id.pdfView)
    PDFView pdfView;
    @BindView(R.id.tv_txt)
    TextView tv_txt;
    private boolean running, needUpload, isDownload, isKonw;
    private int viewNum, needViewNum, interval;    //已观看次数，要求观看次数，延时访问时间
    private String url, filePath;
    private DownloadDBManager dbManager;
    private AlertDialog gestureDialog;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_courseware_file;
    }

    @Override
    public void initView() {
        running = getIntent().getBooleanExtra("running", false);
        needUpload = getIntent().getBooleanExtra("needUpload", false);
        String title = getIntent().getStringExtra("title");
        viewNum = getIntent().getIntExtra("viewNum", 0);
        needViewNum = getIntent().getIntExtra("needViewNum", 0);
        interval = getIntent().getIntExtra("interval", 12);
        toolBar.setTitle_text(title);
        showTips();
        url = getIntent().getStringExtra("file");
        dbManager = new DownloadDBManager(context);
        previewFile();
    }

    private void showTips() {
        toolBar.setShow_right_button(false);
        String message = "观看文档即可完成活动，要求观看文档 <font color='#ffa500'>" + needViewNum + "</font> 次/您已观看 " + "<font color='#ffa500'>" + viewNum + " 次。";
        tv_tips.setText(Html.fromHtml(message));
    }

    private void previewFile() {
        String savePath = dbManager.search(url);
        if (savePath != null && new File(savePath).exists()) {
            if (new File(savePath).isFile() && MediaFile.isPdfFileType(url)) {
                openPdfFile(savePath);
            } else if (new File(savePath).isFile() && MediaFile.isTxtFileType(url)) {
                openTxtFile(savePath);
            } else {
                isDownload = true;
                filePath = savePath;
                showFileContent(url);
            }
        } else {
            showFileContent(url);
            beginDownload();
        }
    }

    private void showFileContent(String url) {
        ll_fileInfo.setVisibility(VISIBLE);
        Common.setFileType(url, iv_type);
        tv_fileName.setText(Common.getFileName(url));
        if (isDownload) {
            bt_download.setVisibility(VISIBLE);
            bt_download.setText("其他应用打开");
            ll_downloadInfo.setVisibility(GONE);
        } else {
            bt_download.setVisibility(GONE);
            ll_downloadInfo.setVisibility(VISIBLE);
        }
    }

    private void beginDownload() {
        final String fileName = Common.getFileName(url);
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", Constants.REFERER);
        DownloadManager.getInstance().create(url).setFilePath(Constants.coursewareDir).setFileName(fileName).addHeaders(headers).addListener(new DownloadListener() {
            @Override
            public void onProgress(DownloadTask downloadTask, long soFarBytes, long totalBytes) {
                String downloadSize = Common.FormetFileSize(soFarBytes);
                String fileSize = Common.FormetFileSize(totalBytes);
                tv_downloadInfo.setText("下载中...(" + downloadSize + "/" + fileSize + ")");
                progressBar.setMax((int) totalBytes);
                progressBar.setProgress((int) soFarBytes);
            }

            @Override
            public void onSuccess(DownloadTask downloadTask, String savePath) {
                if (savePath != null && new File(savePath).exists()) {
                    isDownload = true;
                    filePath = savePath;
                    if (new File(savePath).isFile() && MediaFile.isPdfFileType(url)) {
                        openPdfFile(filePath);
                    } else if (new File(savePath).isFile() && MediaFile.isTxtFileType(url)) {
                        openTxtFile(filePath);
                    } else {
                        bt_download.setVisibility(VISIBLE);
                        bt_download.setText("其他应用打开");
                        ll_downloadInfo.setVisibility(GONE);
                    }
                } else {
                    isDownload = false;
                    bt_download.setVisibility(VISIBLE);
                    bt_download.setText("继续下载");
                    ll_downloadInfo.setVisibility(GONE);
                    toastFullScreen("下载的文件不存在", false);
                }
                DownloadFileInfo fileInfo = new DownloadFileInfo();
                fileInfo.setFileName(downloadTask.getFileName());
                fileInfo.setUrl(downloadTask.getUrl());
                fileInfo.setFilePath(savePath);
                dbManager.save(fileInfo);
            }

            @Override
            public void onFailed(DownloadTask downloadTask) {
                toastFullScreen("文件下载出错", false);
                bt_download.setVisibility(View.VISIBLE);
                bt_download.setText("继续下载");
                ll_downloadInfo.setVisibility(View.GONE);
            }

            @Override
            public void onPaused(DownloadTask downloadTask) {
                bt_download.setVisibility(View.VISIBLE);
                bt_download.setText("继续下载");
                ll_downloadInfo.setVisibility(View.GONE);
            }

            @Override
            public void onCancel(DownloadTask downloadTask) {

            }
        }).start();
    }

    private void openPdfFile(String filePath) {
        ll_fileInfo.setVisibility(GONE);
        pdfView.setVisibility(VISIBLE);
        pdfView.fromFile(new File(filePath))
                .swipeHorizontal(true)
                .defaultPage(0)
                .enableDoubletap(true)
                .enableSwipe(false)
                .scrollHandle(new DefaultScrollHandle(context))
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        if (!isKonw) {
                            showGestureDialog();
                        }
                        if (running && needUpload)
                            updateAttempt();
                    }
                })
                .load();
    }

    private void showGestureDialog() {
        gestureDialog = new AlertDialog.Builder(context, R.style.GestureDialog).create();
        gestureDialog.show();
        View view = View.inflate(context, R.layout.dialog_gesture_tips, null);
        TextView tv_tips = view.findViewById(R.id.tv_tips);
        ImageView iv_center = view.findViewById(R.id.iv_center);
        tv_tips.setText("手势可放大缩小");
        iv_center.setImageResource(R.drawable.gesture_big);
        view.findViewById(R.id.bt_know).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isKonw = true;
                gestureDialog.dismiss();
                gestureDialog = null;
            }
        });
        gestureDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isKonw = true;
            }
        });
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        gestureDialog.setContentView(view, params);
    }

    private void openTxtFile(String filePath) {
        File file = new File(filePath);
        BufferedReader reader;
        String text = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(fis);
            in.mark(4);
            byte[] first3bytes = new byte[3];
            in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
            in.reset();
            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                    && first3bytes[2] == (byte) 0xBF) {// utf-8
                reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFE) {
                reader = new BufferedReader(
                        new InputStreamReader(in, "unicode"));
            } else if (first3bytes[0] == (byte) 0xFE
                    && first3bytes[1] == (byte) 0xFF) {
                reader = new BufferedReader(new InputStreamReader(in,
                        "utf-16be"));
            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFF) {
                reader = new BufferedReader(new InputStreamReader(in,
                        "utf-16le"));
            } else {
                reader = new BufferedReader(new InputStreamReader(in, "GBK"));
            }
            String str = reader.readLine();
            while (str != null) {
                text = text + str + "\n";
                str = reader.readLine();
            }
            reader.close();
            ll_fileInfo.setVisibility(GONE);
            tv_txt.setVisibility(VISIBLE);
            tv_txt.setText(text);
            tv_txt.setMovementMethod(ScrollingMovementMethod.getInstance());
            if (running && needUpload)
                updateAttempt();
        } catch (Exception e) {
            ll_fileInfo.setVisibility(VISIBLE);
            bt_download.setVisibility(VISIBLE);
            bt_download.setText("其他应用打开");
            ll_downloadInfo.setVisibility(GONE);
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
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_close:
                        hideTopView();
                        return;
                    case R.id.bt_download:
                        if (isDownload) {
                            if (new File(filePath).exists()) {
                                if (Common.openFile(context, new File(filePath)) && running && needUpload)
                                    updateAttempt();
                            } else {
                                toast(context, "文件已被删除，请重新下载");
                                isDownload = false;
                                bt_download.setText("重新下载");
                            }
                        } else {
                            bt_download.setVisibility(View.GONE);
                            ll_downloadInfo.setVisibility(View.VISIBLE);
                            beginDownload();
                        }
                        return;
                    case R.id.iv_pause:
                        DownloadManager.getInstance().pause(url);
                        return;
                }
            }
        };
        tv_close.setOnClickListener(listener);
        bt_download.setOnClickListener(listener);
        iv_pause.setOnClickListener(listener);
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
    protected void onDestroy() {
        super.onDestroy();
        if (gestureDialog != null)
            gestureDialog.dismiss();
        pdfView.recycle();
        DownloadManager.getInstance().pause(url);
    }
}
