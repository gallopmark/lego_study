package com.haoyu.app.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.download.AndroidDownladTask;
import com.haoyu.app.download.OnDownloadStatusListener;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.filePicker.FileUtils;
import com.haoyu.app.fragment.OfficeViewerFragment;
import com.haoyu.app.fragment.PDFViewerFragment;
import com.haoyu.app.fragment.PictureViewerFragment;
import com.haoyu.app.fragment.TxtViewerFragment;
import com.haoyu.app.fragment.VideoPlayerFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.MediaFile;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.RoundRectProgressBar;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;

/**
 * 创建日期：2017/9/5 on 15:51
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MFileInfoActivity extends BaseActivity {
    private MFileInfoActivity context = this;
    @BindView(R.id.rootView)
    LinearLayout rootView;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
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

    @BindView(R.id.container)
    FrameLayout container;  //视频文件
    private int smallHeight;
    private FragmentManager fragmentManager;
    private VideoPlayerFragment videoFragment;

    private OfficeViewerFragment officeFragment;
    private boolean isDownload;
    private AndroidDownladTask downladTask;
    private String fileRoot = Constants.fileDownDir;
    private String url, fileName, filePath;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_fileinfo;
    }

    @Override
    public void initData() {
        String title = getIntent().getStringExtra("title");
        MFileInfo mFileInfo = (MFileInfo) getIntent().getSerializableExtra("fileInfo");
        if (mFileInfo != null) {
            toolBar.setTitle_text(mFileInfo.getFileName());
        } else if (title != null) {
            toolBar.setTitle_text(title);
        }
        url = mFileInfo.getUrl();
        fileName = mFileInfo.getFileName();
        fragmentManager = getSupportFragmentManager();
        if (MediaFile.isImageFileType(url)) {
            setPictureFragment();
        } else if (MediaFile.isVideoFileType(url)) {
            setVideoFragment();
        } else {
            ll_fileInfo.setVisibility(View.VISIBLE);
            previewFile(mFileInfo);
        }
    }

    private void setPictureFragment() {
        PictureViewerFragment fragment = new PictureViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    private void setVideoFragment() {
        rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.videoplayer_control));
        smallHeight = ScreenUtils.getScreenHeight(context) / 5 * 2;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
        params.height = smallHeight;
        container.setLayoutParams(params);
        videoFragment = new VideoPlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("videoUrl", url);
        videoFragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.container, videoFragment).commit();
        videoFragment.setOnRequestedOrientation(new VideoPlayerFragment.OnRequestedOrientation() {
            @Override
            public void onRequested(int orientation) {
                setRequestedOrientation(orientation);
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
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
            params.height = smallHeight;
            container.setLayoutParams(params);
        } else { //横屏
            if (videoFragment != null) {
                videoFragment.setFullScreen(true);
            }
            hideOutSize();
            int screeWidth = ScreenUtils.getScreenWidth(context);
            int screenHeight = ScreenUtils.getScreenHeight(context);
            int statusHeight = ScreenUtils.getStatusHeight(context);
            int realHeight = screenHeight - statusHeight;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
            params.weight = screeWidth;
            params.height = realHeight;
            params.setMargins(0, 0, 0, 0);
            container.setLayoutParams(params);
        }
    }

    private void showOutSize() {
        toolBar.setVisibility(View.VISIBLE);
    }

    private void hideOutSize() {
        toolBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return true;
            } else if (officeFragment != null) {
                fragmentManager.beginTransaction().remove(officeFragment).commit();
                officeFragment = null;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void previewFile(MFileInfo mFileInfo) {
        String savePath = fileRoot + File.separator + fileName;
        if (new File(savePath).exists()) {
            if (new File(savePath).isFile() && MediaFile.isPdfFileType(url)) {
                openPdfFile(savePath);
            } else if (new File(savePath).isFile() && MediaFile.isTxtFileType(url)) {
                openTxtFile(savePath);
            } else {
                isDownload = true;
                filePath = savePath;
                showFileContent(mFileInfo);
            }
        } else {
            showFileContent(mFileInfo);
            beginDownload();
        }
    }

    /*打开pdf文件*/
    private void openPdfFile(String filePath) {
        PDFViewerFragment fragment = new PDFViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    /*打开txt文件*/
    private void openTxtFile(String filePath) {
        TxtViewerFragment fragment = new TxtViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    /*显示文件信息，名称、大小、类型*/
    private void showFileContent(MFileInfo fileInfo) {
        ll_fileInfo.setVisibility(View.VISIBLE);
        String url = fileInfo.getUrl();
        Common.setFileType(url, iv_type);
        tv_fileName.setText(fileInfo.getFileName());
        tv_fileSize.setText(FileUtils.getReadableFileSize(fileInfo.getFileSize()));
        if (MediaFile.isOfficeFileType(url)) {
            setSupport_text(url);
        } else {
            tv_tips.setText("由于文件格式问题，\n暂不支持在线浏览,请下载后查看");
        }
        if (isDownload) {
            bt_download.setVisibility(View.VISIBLE);
            bt_download.setText("其他应用打开");
            ll_downloadInfo.setVisibility(View.GONE);
        } else {
            bt_download.setVisibility(View.GONE);
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
        fragmentManager.beginTransaction().replace(R.id.container, officeFragment).commit();
    }

    private void beginDownload() {
        if (url == null) {
            toast(context, "文件链接不存在");
            return;
        }
        if (fileName == null) fileName = Common.getFileName(url);
        ll_downloadInfo.setVisibility(View.VISIBLE);
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", Constants.REFERER);
        downladTask = new AndroidDownladTask.Builder().setUrl(url).setFilePath(fileRoot).setFileName(fileName).setHeaders(headers).setmListner(new OnDownloadStatusListener() {
            @Override
            public void onPrepared(AndroidDownladTask downloadTask, long fileSize) {
                tv_fileSize.setText(FileUtils.getReadableFileSize(fileSize));
            }

            @Override
            public void onProgress(AndroidDownladTask downloadTask, long soFarBytes, long totalBytes) {
                String downloadSize = Common.FormetFileSize(soFarBytes);
                String fileSize = Common.FormetFileSize(totalBytes);
                tv_downloadInfo.setText("下载中...(" + downloadSize + "/" + fileSize + ")");
                downloadBar.setMax((int) totalBytes);
                downloadBar.setProgress((int) soFarBytes);
            }

            @Override
            public void onSuccess(AndroidDownladTask downloadTask, String savePath) {
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
            public void onFailed(AndroidDownladTask downloadTask) {
                toastFullScreen("文件下载出错", false);
                bt_download.setVisibility(View.VISIBLE);
                bt_download.setText("继续下载");
                ll_downloadInfo.setVisibility(View.GONE);
            }

            @Override
            public void onPaused(AndroidDownladTask downloadTask) {
                bt_download.setVisibility(View.VISIBLE);
                bt_download.setText("继续下载");
                ll_downloadInfo.setVisibility(View.GONE);
            }

            @Override
            public void onCancel(AndroidDownladTask downloadTask) {

            }
        }).build();
        downladTask.start();
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.bt_download:
                        if (isDownload) {
                            if (new File(filePath).exists())
                                Common.openFile(context, new File(filePath));
                            else {
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
        bt_download.setOnClickListener(listener);
        iv_pause.setOnClickListener(listener);
    }

    @Override
    protected void onDestroy() {
        if (downladTask != null) {
            downladTask.pause();
        }
        super.onDestroy();
    }
}
