package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.download.DownloadListener;
import com.haoyu.app.download.DownloadManager;
import com.haoyu.app.download.DownloadTask;
import com.haoyu.app.download.db.DownloadDBManager;
import com.haoyu.app.download.db.DownloadFileInfo;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.filePicker.FileUtils;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.MediaFile;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.RoundRectProgressBar;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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
    @BindView(R.id.tv_nonsupport)
    TextView tv_nonsupport;  //文件不支持在线预览
    @BindView(R.id.ll_support)
    LinearLayout ll_support;   //文件支持在线预览（office文件）,调用第三方（微软）打开
    @BindView(R.id.tv_support)
    TextView tv_support;
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
    @BindView(R.id.pdfView)
    PDFView pdfView;
    @BindView(R.id.tv_txt)
    TextView tv_txt;

    private boolean isDownload, isKonw;
    private String url, fileName, filePath;
    private DownloadDBManager dbManager;
    private AlertDialog gestureDialog;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_fileinfo;
    }

    @Override
    public void initData() {
        dbManager = new DownloadDBManager(context);
        String title = getIntent().getStringExtra("title");
        MFileInfo mFileInfo = (MFileInfo) getIntent().getSerializableExtra("fileInfo");
        if (mFileInfo != null) {
            toolBar.setTitle_text(mFileInfo.getFileName());
        } else if (title != null) {
            toolBar.setTitle_text(title);
        }
        url = mFileInfo.getUrl();
        fileName = mFileInfo.getFileName();
        if (MediaFile.isImageFileType(url)) {
            Intent intent = new Intent(context, MFileInfoPreViewActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("fileName", fileName);
            startActivity(intent);
            finish();
        } else
            previewFile(mFileInfo);
    }

    private void previewFile(MFileInfo mFileInfo) {
        String savePath = dbManager.search(url);
        if (savePath != null && new File(savePath).exists()) {
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
        ll_fileInfo.setVisibility(View.GONE);
        pdfView.setVisibility(View.VISIBLE);
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
                    }
                })
                .load();
    }

    /*打开txt文件*/
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
            ll_fileInfo.setVisibility(View.GONE);
            tv_txt.setVisibility(View.VISIBLE);
            tv_txt.setText(text);
            tv_txt.setMovementMethod(ScrollingMovementMethod.getInstance());
        } catch (Exception e) {

        }
    }

    /*显示文件信息，名称、大小、类型*/
    private void showFileContent(MFileInfo fileInfo) {
        ll_fileInfo.setVisibility(View.VISIBLE);
        String url = fileInfo.getUrl();
        Common.setFileType(url, iv_type);
        tv_fileName.setText(fileInfo.getFileName());
        tv_fileSize.setText(FileUtils.getReadableFileSize(fileInfo.getFileSize()));
        if (MediaFile.isOfficeFileType(url)) {
            ll_support.setVisibility(View.VISIBLE);
        } else {
            tv_nonsupport.setVisibility(View.VISIBLE);
        }
        if (isDownload) {
            bt_download.setVisibility(View.VISIBLE);
            bt_download.setText("其他应用打开");
            ll_downloadInfo.setVisibility(View.GONE);
        } else {
            bt_download.setVisibility(View.GONE);
            ll_downloadInfo.setVisibility(View.VISIBLE);
        }
    }

    private void beginDownload() {
        if (fileName == null)
            fileName = Common.getFileName(url);
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", Constants.REFERER);
        DownloadManager.getInstance().create(url).setFilePath(Constants.fileDownDir).setFileName(fileName).addHeaders(headers).addListener(new DownloadListener() {
            @Override
            public void onProgress(DownloadTask downloadTask, long soFarBytes, long totalBytes) {
                String downloadSize = Common.FormetFileSize(soFarBytes);
                String fileSize = Common.FormetFileSize(totalBytes);
                tv_downloadInfo.setText("下载中...(" + downloadSize + "/" + fileSize + ")");
                downloadBar.setMax((int) totalBytes);
                downloadBar.setProgress((int) soFarBytes);
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
                        bt_download.setVisibility(View.VISIBLE);
                        bt_download.setText("其他应用打开");
                        ll_downloadInfo.setVisibility(View.GONE);
                    }
                } else {
                    isDownload = true;
                    bt_download.setVisibility(View.VISIBLE);
                    bt_download.setText("继续下载");
                    ll_downloadInfo.setVisibility(View.GONE);
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
                dbManager.delete(downloadTask.getUrl());
            }
        }).start();
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
                    case R.id.tv_support:
                        Intent intent = new Intent(context, MFileInfoPreViewActivity.class);
                        intent.putExtra("url", "https://view.officeapps.live.com/op/view.aspx?src=" + url);
                        intent.putExtra("fileName", fileName);
                        startActivity(intent);
                        return;
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
                        DownloadManager.getInstance().pause(url);
                        return;
                }
            }
        };
        tv_support.setOnClickListener(listener);
        bt_download.setOnClickListener(listener);
        iv_pause.setOnClickListener(listener);
    }

    @Override
    protected void onDestroy() {
        if (gestureDialog != null)
            gestureDialog.dismiss();
        pdfView.recycle();
        DownloadManager.getInstance().pause(url);
        super.onDestroy();
    }
}
