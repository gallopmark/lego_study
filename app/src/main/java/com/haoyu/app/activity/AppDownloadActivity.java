package com.haoyu.app.activity;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.DownloadCompleteAdapter;
import com.haoyu.app.adapter.DownloadingAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.MemoryUtil;
import com.haoyu.app.view.AppToolBar;

import org.wlf.filedownloader.DownloadFileInfo;
import org.wlf.filedownloader.FileDownloader;
import org.wlf.filedownloader.base.Status;
import org.wlf.filedownloader.listener.OnDeleteDownloadFilesListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * 创建日期：2017/7/6 on 12:35
 * 描述:视频下载页面
 * 作者:马飞奔 Administrator
 */
public class AppDownloadActivity extends BaseActivity implements View.OnClickListener {
    private AppDownloadActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.rl_downloading)
    RelativeLayout rl_downloading;
    @BindView(R.id.rl_complete)
    RelativeLayout rl_complete;
    @BindView(R.id.rvDownloading)
    RecyclerView rvDownloading;
    @BindView(R.id.rvComplete)
    RecyclerView rvComplete;
    @BindView(R.id.tv_empty1)
    TextView tv_empty1;
    @BindView(R.id.tv_empty2)
    TextView tv_empty2;
    private List<DownloadFileInfo> downloadList = new ArrayList<>();
    private List<DownloadFileInfo> completeList = new ArrayList<>();
    private DownloadingAdapter downloadingAdapter;
    private DownloadCompleteAdapter downloadCompleteAdapter;
    @BindView(R.id.fl_memory)
    FrameLayout fl_memory;
    @BindView(R.id.tv_memorySize)
    TextView tv_memorySize;
    @BindView(R.id.tv_totalSize)
    TextView tv_totalSize;
    @BindView(R.id.tv_availableSize)
    TextView tv_availableSize;
    @BindView(R.id.ll_edit)
    LinearLayout ll_edit;
    @BindView(R.id.bt_selectAll)
    Button bt_selectAll;
    @BindView(R.id.bt_delete)
    Button bt_delete;
    private int checkIndex = 0;
    private boolean isEdit = true, selectAll = true;
    private String text_edit = "编辑", text_cancel = "取消", text_selectAll = "全选", text_cancelAll = "反选", text_delete = "删除";

    @Override
    public int setLayoutResID() {
        return R.layout.activity_download;
    }

    @Override
    public void initView() {
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(context);
        layoutManager1.setOrientation(LinearLayoutManager.VERTICAL);
        rvDownloading.setLayoutManager(layoutManager1);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(context);
        layoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        rvComplete.setLayoutManager(layoutManager2);
        List<DownloadFileInfo> fileInfos = FileDownloader.getDownloadFiles();
        if (fileInfos != null && fileInfos.size() > 0) {
            for (DownloadFileInfo fileInfo : fileInfos) {
                if (fileInfo != null && fileInfo.getStatus() != Status.DOWNLOAD_STATUS_COMPLETED) {
                    downloadList.add(fileInfo);
                }
                if (fileInfo != null && fileInfo.getStatus() == Status.DOWNLOAD_STATUS_COMPLETED) {
                    completeList.add(fileInfo);
                }
            }
        }
        downloadingAdapter = new DownloadingAdapter(downloadList);
        rvDownloading.setAdapter(downloadingAdapter);
        downloadCompleteAdapter = new DownloadCompleteAdapter(context, completeList);
        rvComplete.setAdapter(downloadCompleteAdapter);
        if (downloadList.size() > 0) {
            toolBar.setShow_right_button(true);
            rvDownloading.setVisibility(View.VISIBLE);
            tv_empty1.setVisibility(View.GONE);
        } else {
            toolBar.setShow_right_button(false);
            rvDownloading.setVisibility(View.GONE);
            tv_empty1.setVisibility(View.VISIBLE);
        }
        if (completeList.size() > 0) {
            rvComplete.setVisibility(View.VISIBLE);
            tv_empty2.setVisibility(View.GONE);
        } else {
            rvComplete.setVisibility(View.GONE);
            tv_empty2.setVisibility(View.VISIBLE);
        }
        downloadingAdapter.setOnDownLoadFinishListener(new DownloadingAdapter.OnDownLoadFinishListener() {
            @Override
            public void onFileDownloadStatusCompleted(DownloadFileInfo downloadFileInfo) {
                downloadList.remove(downloadFileInfo);
                downloadingAdapter.notifyDataSetChanged();
                if (downloadList.size() == 0) {
                    rvDownloading.setVisibility(View.GONE);
                    tv_empty1.setVisibility(View.VISIBLE);
                    if (checkIndex == 0) {
                        toolBar.setShow_right_button(false);
                    }
                }
                if (!completeList.contains(downloadFileInfo)) {
                    completeList.add(downloadFileInfo);
                    downloadCompleteAdapter.notifyDataSetChanged();
                    rvComplete.setVisibility(View.VISIBLE);
                    tv_empty2.setVisibility(View.GONE);
                    if (checkIndex == 1) {
                        toolBar.setShow_right_button(true);
                    }
                }
            }
        });
        initMemory();
    }

    private void initMemory() {
        long totalSize = MemoryUtil.getSDTotalSizeLong(context);
        long availableSize = MemoryUtil.getSDAvailableSizeLong(context);
        String totalMemory = MemoryUtil.formatFileSize(totalSize, false);
        String availableMemory = MemoryUtil.formatFileSize(availableSize, false);
        tv_memorySize.setText("共" + totalMemory + "，可用空间" + availableMemory);
        float availableGb = (Math.round(availableSize / 1024 / 1024 / 1024) * 100) / 100;
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, totalSize);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, availableSize);
        tv_totalSize.setLayoutParams(params1);
        tv_availableSize.setLayoutParams(params2);
        if (availableGb < 0.1) {
            tv_totalSize.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
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
                if (isEdit) {
                    setEdit();
                } else {
                    resetEdit();
                }
            }
        });
        bt_selectAll.setOnClickListener(context);
        bt_delete.setOnClickListener(context);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkId) {
                switch (checkId) {
                    case R.id.rb_downloading:
                        checkIndex = 0;
                        rl_downloading.setVisibility(View.VISIBLE);
                        if (rvDownloading.getVisibility() == View.VISIBLE)
                            toolBar.setShow_right_button(true);
                        else
                            toolBar.setShow_right_button(false);
                        if (downloadCompleteAdapter.isEdit()) {
                            downloadCompleteAdapter.setEdit(false, null);
                            downloadCompleteAdapter.cancelAll();
                            resetEdit();
                        }
                        rl_complete.setVisibility(View.GONE);
                        return;
                    case R.id.rb_downloadComplete:
                        checkIndex = 1;
                        rl_downloading.setVisibility(View.GONE);
                        if (rvComplete.getVisibility() == View.VISIBLE)
                            toolBar.setShow_right_button(true);
                        else
                            toolBar.setShow_right_button(false);
                        if (downloadingAdapter.isEdit()) {
                            downloadingAdapter.setEdit(false, null);
                            downloadingAdapter.cancelAll();
                            resetEdit();
                        }
                        rl_complete.setVisibility(View.VISIBLE);
                        return;
                }
            }
        });
        downloadingAdapter.setOnItemLongClickListener(new BaseRecyclerAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                setEdit();
            }
        });
        downloadCompleteAdapter.setOnItemLongClickListener(new BaseRecyclerAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                setEdit();
            }
        });
        downloadCompleteAdapter.setOnItemClickListener(new DownloadCompleteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DownloadFileInfo fileInfo) {
                String path = fileInfo.getFilePath();
                if (path != null && new File(path).exists()) {
                    Intent intent = new Intent(context, IJKPlayerActivity.class);
                    intent.putExtra("videoUrl", path);
                    startActivity(intent);
                }
            }
        });
    }

    private void setEdit() {
        isEdit = false;
        toolBar.setRight_button_text(text_cancel);
        fl_memory.setVisibility(View.GONE);
        ll_edit.setVisibility(View.VISIBLE);
        if (checkIndex == 0) {
            if (downloadList.size() > 0) {
                downloadingAdapter.setEdit(true, new DownloadingAdapter.OnSelectedListener() {
                    @Override
                    public void onSelected(List<DownloadFileInfo> mSelect) {
                        if (mSelect.size() == downloadList.size()) {
                            bt_selectAll.setText(text_cancelAll);
                            selectAll = false;
                        } else {
                            bt_selectAll.setText(text_selectAll);
                            selectAll = true;
                        }
                        if (mSelect.size() > 0)
                            bt_delete.setText(text_delete + "(" + mSelect.size() + ")");
                        else
                            bt_delete.setText(text_delete);
                    }
                });
            }
        } else {
            if (completeList.size() > 0) {
                downloadCompleteAdapter.setEdit(true, new DownloadCompleteAdapter.OnSelectedListener() {
                    @Override
                    public void onSelected(List<DownloadFileInfo> mSelect) {
                        if (mSelect.size() == completeList.size()) {
                            bt_selectAll.setText(text_cancelAll);
                            selectAll = false;
                        } else {
                            bt_selectAll.setText(text_selectAll);
                            selectAll = true;
                        }
                        if (mSelect.size() > 0)
                            bt_delete.setText(text_delete + "(" + mSelect.size() + ")");
                        else
                            bt_delete.setText(text_delete);
                    }
                });
            }
        }
    }

    private void resetEdit() {
        isEdit = true;
        selectAll = true;
        toolBar.setRight_button_text(text_edit);
        fl_memory.setVisibility(View.VISIBLE);
        ll_edit.setVisibility(View.GONE);
        bt_selectAll.setText(text_selectAll);
        bt_delete.setText(text_delete);
        if (checkIndex == 0) {
            if (downloadingAdapter.isEdit()) {
                downloadingAdapter.setEdit(false, null);
                downloadingAdapter.cancelAll();
            }
        } else {
            if (downloadCompleteAdapter.isEdit()) {
                downloadCompleteAdapter.setEdit(false, null);
                downloadCompleteAdapter.cancelAll();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_selectAll:
                if (selectAll) {
                    bt_selectAll.setText(text_cancelAll);
                    selectAll = false;
                    if (checkIndex == 0) {
                        downloadingAdapter.selecetAll();
                    } else {
                        downloadCompleteAdapter.selecetAll();
                    }
                } else {
                    bt_selectAll.setText(text_selectAll);
                    selectAll = true;
                    if (checkIndex == 0) {
                        downloadingAdapter.cancelAll();
                    } else {
                        downloadCompleteAdapter.cancelAll();
                    }
                }
                if (checkIndex == 0) {
                    if (downloadingAdapter.getmSelected().size() > 0)
                        bt_delete.setText(text_delete + "(" + downloadingAdapter.getmSelected().size() + ")");
                    else
                        bt_delete.setText(text_delete);
                } else {
                    if (downloadCompleteAdapter.getmSelected().size() > 0)
                        bt_delete.setText(text_delete + "(" + downloadCompleteAdapter.getmSelected().size() + ")");
                    else
                        bt_delete.setText(text_delete);
                }
                return;
            case R.id.bt_delete:
                List<String> urls = new ArrayList<>();
                if (checkIndex == 0) {
                    if (downloadingAdapter.getmSelected().size() > 0) {
                        for (DownloadFileInfo fileInfo : downloadingAdapter.getmSelected()) {
                            urls.add(fileInfo.getUrl());
                        }
                    }
                } else {
                    if (downloadCompleteAdapter.getmSelected().size() > 0) {
                        for (DownloadFileInfo fileInfo : downloadCompleteAdapter.getmSelected()) {
                            urls.add(fileInfo.getUrl());
                        }
                    }
                }
                if (urls.size() > 0) {
                    FileDownloader.delete(urls, true, new OnDeleteDownloadFilesListener() {
                        @Override
                        public void onDeleteDownloadFilesPrepared(List<DownloadFileInfo> downloadFilesNeedDelete) {
                            showTipDialog();
                        }

                        @Override
                        public void onDeletingDownloadFiles(List<DownloadFileInfo> downloadFilesNeedDelete, List<DownloadFileInfo> downloadFilesDeleted, List<DownloadFileInfo> downloadFilesSkip, DownloadFileInfo downloadFileDeleting) {

                        }

                        @Override
                        public void onDeleteDownloadFilesCompleted(List<DownloadFileInfo> downloadFilesNeedDelete, List<DownloadFileInfo> downloadFilesDeleted) {
                            hideTipDialog();
                            if (checkIndex == 0) {
                                downloadList.removeAll(downloadFilesNeedDelete);
                                downloadingAdapter.notifyDataSetChanged();
                                if (downloadList.size() == 0) {
                                    toolBar.setShow_right_button(false);
                                    rvDownloading.setVisibility(View.GONE);
                                    tv_empty1.setVisibility(View.VISIBLE);
                                }
                            } else {
                                completeList.removeAll(downloadFilesDeleted);
                                downloadCompleteAdapter.notifyDataSetChanged();
                                if (completeList.size() == 0) {
                                    toolBar.setShow_right_button(false);
                                    rvComplete.setVisibility(View.GONE);
                                    tv_empty2.setVisibility(View.VISIBLE);
                                }
                            }
                            resetEdit();
                        }
                    });
                } else {
                    resetEdit();
                }
                return;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadingAdapter.getmOnFileDownloadStatusListener() != null) {
            FileDownloader.unregisterDownloadStatusListener(downloadingAdapter.getmOnFileDownloadStatusListener());
        }
    }
}
