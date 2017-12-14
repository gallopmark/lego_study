package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.filePicker.FileUtils;
import com.haoyu.app.fragment.OfficeViewerFragment;
import com.haoyu.app.fragment.PictureViewerFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.MediaFile;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.RoundRectProgressBar;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 创建日期：2017/9/5 on 15:51
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MFileInfoActivity extends BaseActivity {
    private MFileInfoActivity context = this;
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
    private FragmentManager fragmentManager;

    private OfficeViewerFragment officeFragment;
    private boolean isDownload, isDestroy;
    private String fileRoot = Constants.fileDownDir;
    private MFileInfo fileInfo;
    private String url, fileName, filePath;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_fileinfo;
    }

    @Override
    public void initData() {
        String title = getIntent().getStringExtra("title");
        fileInfo = (MFileInfo) getIntent().getSerializableExtra("fileInfo");
        if (fileInfo != null) {
            toolBar.setTitle_text(fileInfo.getFileName());
        } else if (title != null) {
            toolBar.setTitle_text(title);
        }
        url = fileInfo.getUrl();
        fragmentManager = getSupportFragmentManager();
        if (MediaFile.isImageFileType(url)) {
            setPictureFragment();
        } else if (MediaFile.isVideoFileType(url)) {
            playVideo();
        } else {
            if (url == null) {
                toast(context, "文件链接不存在");
            } else {
                fileName = Common.getFileName(url);
                FileDownloader.setup(context);
                if (!isFileExists()) {
                    beginDownload();
                }
            }
        }
    }

    private void setPictureFragment() {
        PictureViewerFragment fragment = new PictureViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    private void playVideo() {
        Intent intent = new Intent(context, VideoPlayerLibActivity.class);
        intent.putExtra("videoUrl", url);
        intent.putExtra("videoTitle", fileInfo.getFileName());
        startActivity(intent);
        finish();
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

    private boolean isFileExists() {
        String savePath = FileDownloadUtils.getTargetFilePath(fileRoot, true, fileName);
        if (savePath != null && new File(savePath).exists()) {
            isDownload = true;
            filePath = savePath;
            showFileContent();
            generateFile();
            return true;
        }
        return false;
    }

    private void beginDownload() {
        showFileContent();
        String path = fileRoot + File.separator + fileName;
        FileDownloader.getImpl().create(url)
                .addHeader("Referer", Constants.REFERER)
                .setPath(path, false)
                .setListener(downloadListener).start();
    }

    private void generateFile() {
        if (new File(filePath).isFile() && MediaFile.isPdfFileType(url)) {
            openPdfFile(filePath);
        } else if (new File(filePath).isFile() && MediaFile.isTxtFileType(url)) {
            openTxtFile(filePath);
        } else {
            ll_fileInfo.setVisibility(View.VISIBLE);
            bt_download.setVisibility(View.VISIBLE);
            bt_download.setText("其他应用打开");
            ll_downloadInfo.setVisibility(View.GONE);
        }
    }

    private FileDownloadListener downloadListener = new FileDownloadListener() {
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            ll_downloadInfo.setVisibility(View.VISIBLE);
            String downloadSize = Common.FormetFileSize(soFarBytes);
            String fileSize = Common.FormetFileSize(totalBytes);
            tv_downloadInfo.setText("下载中...(" + downloadSize + "/" + fileSize + ")");
            downloadBar.setMax(totalBytes);
            downloadBar.setProgress(soFarBytes);
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            isDownload = true;
            filePath = task.getTargetFilePath();
            generateFile();
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            bt_download.setVisibility(View.VISIBLE);
            bt_download.setText("继续下载");
            ll_downloadInfo.setVisibility(View.GONE);
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            toastFullScreen("文件下载出错", false);
            bt_download.setVisibility(View.VISIBLE);
            bt_download.setText("继续下载");
            ll_downloadInfo.setVisibility(View.GONE);
        }

        @Override
        protected void warn(BaseDownloadTask task) {

        }
    };

    /*显示文件信息，名称、大小、类型*/
    private void showFileContent() {
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
                        if (!isRead && !isDestroy) {
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
                        FileDownloader.getImpl().pause(downloadListener);
                        return;
                }
            }
        };
        bt_download.setOnClickListener(listener);
        iv_pause.setOnClickListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroy = true;
        FileDownloader.getImpl().pause(downloadListener);
    }
}
