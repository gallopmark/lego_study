package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.download.FileDownladTask;
import com.haoyu.app.download.OnDownloadStatusListener;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.filePicker.FileUtils;
import com.haoyu.app.fragment.CoursewareEditorFragment;
import com.haoyu.app.fragment.CoursewareLinkFragment;
import com.haoyu.app.fragment.OfficeViewerFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.MediaFile;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.RoundRectProgressBar;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 创建日期：2017/11/16.
 * 描述:教学课件预览
 * 作者:xiaoma
 */

public class CoursewareViewerActivity extends BaseActivity {
    private CoursewareViewerActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.ll_tips)
    LinearLayout ll_tips;
    @BindView(R.id.tv_topTips)
    TextView tv_topTips;
    @BindView(R.id.tv_close)
    TextView tv_close;
    @BindView(R.id.container)
    FrameLayout container;
    /*当课件类型是pdf文件时*/
    @BindView(R.id.ll_fileInfo)
    LinearLayout ll_fileInfo;
    @BindView(R.id.iv_type)
    ImageView iv_type;   //文件类型
    @BindView(R.id.tv_fileName)
    TextView tv_fileName;   //文件名称
    @BindView(R.id.tv_fileSize)
    TextView tv_fileSize;   //文件大小
    @BindView(R.id.tv_tips)
    TextView tv_tips;  //文件不支持在线预览
    @BindView(R.id.bt_download)
    Button bt_download;  //下载、继续下载、打开文件按钮
    @BindView(R.id.ll_downloadInfo)
    LinearLayout ll_downloadInfo;    //显示下载信息
    @BindView(R.id.tv_downloadInfo)
    TextView tv_downloadInfo;   //显示文件下载大小和总大小
    @BindView(R.id.downloadBar)
    RoundRectProgressBar downloadBar;  //显示文件下载进度
    @BindView(R.id.iv_pause)
    ImageView iv_pause;   //暂停下载按钮
    private String fileRoot = Constants.coursewareDir;
    private String url, filePath, fileName;
    private boolean isDownload;
    private FileDownladTask downladTask;
    private FragmentManager fragmentManager;
    private OfficeViewerFragment officeFragment;
    /**********************/
    private boolean running, needUpload;
    private int viewNum, needViewNum, interval;    //已观看次数，要求观看次数，延时访问时间
    private String type;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_courseware_viewer;
    }

    @Override
    public void initView() {
        running = getIntent().getBooleanExtra("running", false);
        needUpload = getIntent().getBooleanExtra("needUpload", false);
        String title = getIntent().getStringExtra("title");
        viewNum = getIntent().getIntExtra("viewNum", 0);
        needViewNum = getIntent().getIntExtra("needViewNum", 0);
        interval = getIntent().getIntExtra("interval", 12);
        type = getIntent().getStringExtra("type");
        if (title != null && title.trim().length() > 0)
            toolBar.setTitle_text(Html.fromHtml(title).toString());
        else
            toolBar.setTitle_text("教学课件");
        showTips();
        fragmentManager = getSupportFragmentManager();
        if (type.equals("file")) {   //pdf文件
            url = getIntent().getStringExtra("url");
            beginDownload();
        } else if (type.equals("link")) { //外链
            url = getIntent().getStringExtra("url");
            setLinkFragment(url);
        } else {    //文本编辑器
            String editor = getIntent().getStringExtra("editor");
            setEditorF(editor);
        }
    }

    private void showTips() {
        toolBar.setShow_right_button(false);
        String message = "观看文档即可完成活动，要求观看文档 " + needViewNum + " 次/您已观看 " + viewNum + " 次。";
        SpannableString ssb = new SpannableString(message);
        int start = message.lastIndexOf("档") + 1;
        int end = message.indexOf("次");
        ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = message.lastIndexOf("看") + 1;
        end = message.lastIndexOf("次");
        ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_topTips.setText(ssb);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && officeFragment != null) {
            fragmentManager.beginTransaction().remove(officeFragment).commit();
            officeFragment = null;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void beginDownload() {
        if (url == null) {
            toast(context, "文件链接不存在");
            return;
        }
        if (fileName == null) fileName = Common.getFileName(url);
        showFileContent();
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", Constants.REFERER);
        downladTask = new FileDownladTask.Builder().with(context).setUrl(url).setFilePath(fileRoot).setFileName(fileName).setHeaders(headers).setmListner(new OnDownloadStatusListener() {
            @Override
            public void onPreDownload(FileDownladTask downloadTask) {
                showFileContent();
            }

            @Override
            public void onPrepared(FileDownladTask downloadTask, long fileSize) {
                ll_downloadInfo.setVisibility(View.VISIBLE);
                tv_fileSize.setText(FileUtils.getReadableFileSize(fileSize));
            }

            @Override
            public void onProgress(FileDownladTask downloadTask, long soFarBytes, long totalBytes) {
                String downloadSize = Common.FormetFileSize(soFarBytes);
                String fileSize = Common.FormetFileSize(totalBytes);
                tv_downloadInfo.setText("下载中...(" + downloadSize + "/" + fileSize + ")");
                downloadBar.setMax((int) totalBytes);
                downloadBar.setProgress((int) soFarBytes);
            }

            @Override
            public void onSuccess(FileDownladTask downloadTask, String savePath) {
                isDownload = true;
                filePath = savePath;
                if (new File(savePath).isFile() && MediaFile.isPdfFileType(url)) {
                    openPdfFile(savePath);
                } else if (new File(savePath).isFile() && MediaFile.isTxtFileType(url)) {
                    openTxtFile(savePath);
                } else {
                    bt_download.setVisibility(View.VISIBLE);
                    bt_download.setText("其他应用打开");
                    ll_downloadInfo.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailed(FileDownladTask downloadTask) {
                toastFullScreen("文件下载出错", false);
                bt_download.setVisibility(View.VISIBLE);
                bt_download.setText("继续下载");
                ll_downloadInfo.setVisibility(View.GONE);
            }

            @Override
            public void onPaused(FileDownladTask downloadTask) {
                bt_download.setVisibility(View.VISIBLE);
                bt_download.setText("继续下载");
                ll_downloadInfo.setVisibility(View.GONE);
            }

            @Override
            public void onCancel(FileDownladTask downloadTask) {

            }
        }).build();
        downladTask.start();
    }

    private void showFileContent() {
        ll_fileInfo.setVisibility(View.VISIBLE);
        Common.setFileType(url, iv_type);
        tv_fileName.setText(Common.getFileName(url));
        if (MediaFile.isOfficeFileType(url)) {
            setSupport_text(url);
        } else {
            tv_tips.setText("由于文件格式问题，\n暂不支持在线浏览,请下载后查看");
        }
    }

    private void setSupport_text(final String url) {
        String text = "由于文件格式问题，暂不支持本地查看\n您可以点击 在线预览 在浏览器查阅";
        SpannableString ssb = new SpannableString(text);
        int start = text.indexOf("击") + 1;
        int end = text.indexOf("览") + 1;
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                setOfficeViewer(url);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.bgColor = ContextCompat.getColor(context, R.color.white);
                ds.setColor(ContextCompat.getColor(context, R.color.defaultColor));
                ds.setUnderlineText(false);
            }
        };
        ssb.setSpan(clickableSpan, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_tips.setMovementMethod(LinkMovementMethod.getInstance());
        tv_tips.setText(ssb);
    }

    private void setOfficeViewer(String url) {
        officeFragment = new OfficeViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        officeFragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.container, officeFragment).commitAllowingStateLoss();
    }

    private boolean isRead;

    /*打开pdf文件*/
    private void openPdfFile(String filePath) {
        container.removeAllViews();
        View view = LayoutInflater.from(context).inflate(R.layout.layout_pdfviewer, container, false);
        PDFView pdfView = view.findViewById(R.id.pdfView);
        final TextView tv_page = view.findViewById(R.id.tv_page);
        pdfView.fromFile(new File(filePath))
                .swipeHorizontal(true)
                .defaultPage(0)
                .enableDoubletap(true)
                .enableSwipe(true)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .scrollHandle(null)
                .linkHandler(null)
                .enableAntialiasing(true) //  .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        if (!isRead) {
                            showGestureDialog();
                        }
                    }
                })
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        tv_page.setText((page + 1) + "/" + pageCount);
                    }
                })
                .load();
        container.addView(view);
    }

    private void showGestureDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.GestureDialog).create();
        dialog.show();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_gesture_tips, null);
        TextView tv_tips = view.findViewById(R.id.tv_tips);
        ImageView iv_center = view.findViewById(R.id.iv_center);
        tv_tips.setText("手势可放大缩小");
        iv_center.setImageResource(R.drawable.gesture_big);
        view.findViewById(R.id.bt_know).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRead = true;
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isRead = true;
            }
        });
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        dialog.setContentView(view, params);
    }

    /*打开txt文件*/
    private void openTxtFile(String filePath) {
        container.removeAllViews();
        TextView tv = new TextView(context);
        tv.setTextSize(16);
        int dp_12 = PixelFormat.dp2px(context, 12);
        tv.setPadding(dp_12, dp_12, dp_12, dp_12);
        tv.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        container.addView(tv);
        openTxtFile(tv, filePath);
    }

    private void openTxtFile(final TextView tv_txt, final String filePath) {
        showTipDialog();
        addSubscription(Flowable.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                File file = new File(filePath);
                BufferedReader reader = null;
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
                    while (reader.readLine() != null) {
                        text += reader.readLine() + "\n";
                    }
                    reader.close();
                    fis.close();
                    in.close();
                } catch (Exception e) {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e1) {
                    }
                }
                return text;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String content) throws Exception {
                hideTipDialog();
                tv_txt.setText(content);
                tv_txt.setMovementMethod(ScrollingMovementMethod.getInstance());
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                hideTipDialog();
                tv_txt.setGravity(Gravity.CENTER);
                tv_txt.setText("无法预览此文件");
            }
        }));
    }

    private void setLinkFragment(String url) {
        CoursewareLinkFragment fragment = new CoursewareLinkFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    private void setEditorF(String editor) {
        CoursewareEditorFragment fragment = new CoursewareEditorFragment();
        Bundle bundle = new Bundle();
        bundle.putString("editor", editor);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
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
                        tv_close.setEnabled(false);
                        hideTopView();
                        return;
                    case R.id.bt_download:
                        if (isDownload) {
                            if (new File(filePath).exists()) {
                                if (Common.openFile(context, new File(filePath))) ;
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
                        if (downladTask != null) {
                            downladTask.pause();
                        }
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
                    tv_close.setEnabled(true);
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
    public void initData() {
        if (running && needUpload) {
            updateAttempt();
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
    protected void onDestroy() {
        if (downladTask != null) {
            downladTask.pause();
        }
        super.onDestroy();
    }
}
