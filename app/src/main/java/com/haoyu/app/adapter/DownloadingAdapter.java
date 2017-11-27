package com.haoyu.app.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.view.CircleProgressBar;

import org.wlf.filedownloader.DownloadFileInfo;
import org.wlf.filedownloader.FileDownloader;
import org.wlf.filedownloader.base.Status;
import org.wlf.filedownloader.listener.OnDeleteDownloadFileListener;
import org.wlf.filedownloader.listener.OnFileDownloadStatusListener;
import org.wlf.filedownloader.listener.simple.OnSimpleFileDownloadStatusListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建日期：2017/7/6 on 14:47
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class DownloadingAdapter extends BaseArrayRecyclerAdapter<DownloadFileInfo> {
    private Map<String, View> viewMap = new HashMap<>();
    private OnDownLoadFinishListener onDownLoadFinishListener;
    private boolean edit = false;
    private List<DownloadFileInfo> mSelected = new ArrayList<>();
    private Map<Integer, Boolean> hashMap = new HashMap<>();
    private OnSelectedListener onSelectedListener;

    public DownloadingAdapter(List<DownloadFileInfo> mDatas) {
        super(mDatas);
        FileDownloader.registerDownloadStatusListener(mOnFileDownloadStatusListener);
    }

    public void setOnDownLoadFinishListener(OnDownLoadFinishListener onDownLoadFinishListener) {
        this.onDownLoadFinishListener = onDownLoadFinishListener;
    }

    public OnFileDownloadStatusListener getmOnFileDownloadStatusListener() {
        return mOnFileDownloadStatusListener;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit, OnSelectedListener onSelectedListener) {
        this.edit = edit;
        this.onSelectedListener = onSelectedListener;
        notifyDataSetChanged();
    }

    public void selecetAll() {
        for (int i = 0; i < mDatas.size(); i++) {
            hashMap.put(i, true);
        }
        mSelected.clear();
        mSelected.addAll(mDatas);
        notifyDataSetChanged();
    }

    public void cancelAll() {
        for (int i = 0; i < mDatas.size(); i++) {
            hashMap.put(i, false);
        }
        mSelected.clear();
        notifyDataSetChanged();
    }

    public List<DownloadFileInfo> getmSelected() {
        return mSelected;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.download_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final DownloadFileInfo fileInfo, final int position) {
        final CheckBox checkBox = holder.obtainView(R.id.checkBox);
        final TextView fileName = holder.obtainView(R.id.fileName);
        final TextView tv_tips = holder.obtainView(R.id.tv_tips);
        final TextView tv_state = holder.obtainView(R.id.tv_state);
        final RelativeLayout rl_download = holder.obtainView(R.id.rl_download);
        final CircleProgressBar progressBar = holder.obtainView(R.id.progressBar);
        final ImageView iv_state = holder.obtainView(R.id.iv_state);
        final String url = fileInfo.getUrl();
        viewMap.put(url, holder.itemView);
        if (edit) {
            checkBox.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.GONE);
        }
        fileName.setText(fileInfo.getFileName());
        String downloadSize = Common.FormetFileSize(fileInfo.getDownloadedSizeLong());
        String fileSize = Common.FormetFileSize(fileInfo.getFileSizeLong());
        tv_tips.setText("已下载...(" + downloadSize + "/" + fileSize + ")");
        progressBar.setMaxProgress((int) fileInfo.getFileSizeLong());
        progressBar.setProgress((int) fileInfo.getDownloadedSizeLong());
        if (fileInfo.getStatus() == Status.DOWNLOAD_STATUS_FILE_NOT_EXIST) {
            tv_state.setText("文件已被删除");
        } else if (fileInfo.getStatus() == Status.DOWNLOAD_STATUS_DOWNLOADING) {
            tv_state.setText("正在下载");
        } else if (fileInfo.getStatus() == Status.DOWNLOAD_STATUS_WAITING) {
            tv_state.setText("等待下载");
        } else if (fileInfo.getStatus() == Status.DOWNLOAD_STATUS_PREPARING) {
            tv_state.setText("正在连接资源");
        } else if (fileInfo.getStatus() == Status.DOWNLOAD_STATUS_PREPARED) {
            tv_state.setText("连接到了资源");
        } else if (fileInfo.getStatus() == Status.DOWNLOAD_STATUS_RETRYING) {
            tv_state.setText("下载重试");
        } else if (fileInfo.getStatus() == Status.DOWNLOAD_STATUS_PAUSED) {
            tv_state.setText("下载暂停");
        } else if (fileInfo.getStatus() == Status.DOWNLOAD_STATUS_ERROR) {
            tv_state.setText("下载失败");
        } else {
            tv_state.setText("下载错误");
        }
        if (fileInfo.getStatus() == Status.DOWNLOAD_STATUS_DOWNLOADING) {
            iv_state.setImageResource(R.drawable.download_play);
        } else {
            iv_state.setImageResource(R.drawable.download_pause);
        }
        rl_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadFileInfo fileInfo = FileDownloader.getDownloadFile(url);
                if (fileInfo != null && fileInfo.getStatus() != Status.DOWNLOAD_STATUS_DOWNLOADING) {
                    beginDownload(url);
                } else {
                    FileDownloader.pause(url);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit) {
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false);
                        hashMap.put(position, false);
                    } else {
                        checkBox.setChecked(true);
                        hashMap.put(position, true);
                    }
                } else {
                    DownloadFileInfo fileInfo = FileDownloader.getDownloadFile(url);
                    if (fileInfo != null && fileInfo.getStatus() != Status.DOWNLOAD_STATUS_DOWNLOADING) {
                        beginDownload(url);
                    } else {
                        FileDownloader.pause(url);
                    }
                }
            }
        });
        checkBox.setChecked(hashMap.get(position) == null ? false : hashMap.get(position));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                if (isCheck) {
                    if (!mSelected.contains(fileInfo)) {
                        mSelected.add(fileInfo);
                    }
                } else {
                    mSelected.remove(fileInfo);
                }
                if (onSelectedListener != null) {
                    onSelectedListener.onSelected(mSelected);
                }
            }
        });
    }

    private void beginDownload(final String url) {
        DownloadFileInfo fileInfo = FileDownloader.getDownloadFile(url);
        if (fileInfo != null && fileInfo.getStatus() == Status.DOWNLOAD_STATUS_FILE_NOT_EXIST)
            FileDownloader.delete(url, true, new OnDeleteDownloadFileListener() {
                @Override
                public void onDeleteDownloadFilePrepared(DownloadFileInfo downloadFileNeedDelete) {

                }

                @Override
                public void onDeleteDownloadFileSuccess(DownloadFileInfo downloadFileDeleted) {
                    FileDownloader.start(url);
                }

                @Override
                public void onDeleteDownloadFileFailed(DownloadFileInfo downloadFileInfo, DeleteDownloadFileFailReason failReason) {
                    FileDownloader.start(url);
                }
            });
        else
            FileDownloader.start(url);
    }

    private OnFileDownloadStatusListener mOnFileDownloadStatusListener = new OnSimpleFileDownloadStatusListener() {

        @Override
        public void onFileDownloadStatusWaiting(DownloadFileInfo downloadFileInfo) {
            // 等待下载
            String url = downloadFileInfo.getUrl();
            if (url != null && viewMap.get(url) != null) {
                View itemView = viewMap.get(url);
                TextView tv_state = itemView.findViewById(R.id.tv_state);
                tv_state.setText("等待下载");
            }
        }

        @Override
        public void onFileDownloadStatusRetrying(DownloadFileInfo downloadFileInfo, int retryTimes) {
            String url = downloadFileInfo.getUrl();
            if (url != null && viewMap.get(url) != null) {
                View itemView = viewMap.get(url);
                TextView tv_state = itemView.findViewById(R.id.tv_state);
                tv_state.setText("下载重试");
            }
        }

        @Override
        public void onFileDownloadStatusPreparing(DownloadFileInfo downloadFileInfo) {
            // 准备中（即，正在连接资源）
            String url = downloadFileInfo.getUrl();
            if (url != null && viewMap.get(url) != null) {
                View itemView = viewMap.get(url);
                TextView tv_state = itemView.findViewById(R.id.tv_state);
                tv_state.setText("正在连接资源");
            }
        }

        @Override
        public void onFileDownloadStatusPrepared(DownloadFileInfo downloadFileInfo) {
            // 已准备好（即，已经连接到了资源）
            String url = downloadFileInfo.getUrl();
            if (url != null && viewMap.get(url) != null) {
                View itemView = viewMap.get(url);
                TextView tv_state = itemView.findViewById(R.id.tv_state);
                tv_state.setText("连接到了资源");
            }
        }

        @Override
        public void onFileDownloadStatusDownloading(DownloadFileInfo downloadFileInfo, float downloadSpeed, long
                remainingTime) {
            // 正在下载，downloadSpeed为当前下载速度，单位KB/s，remainingTime为预估的剩余时间，单位秒
            String url = downloadFileInfo.getUrl();
            if (url != null && viewMap.get(url) != null) {
                View itemView = viewMap.get(url);
                CircleProgressBar progressBar = itemView.findViewById(R.id.progressBar);
                long totalSize = downloadFileInfo.getFileSizeLong();
                long downloadSize = downloadFileInfo.getDownloadedSizeLong();
                progressBar.setMaxProgress((int) totalSize);
                progressBar.setProgress((int) downloadSize);
                TextView tv_tips = itemView.findViewById(R.id.tv_tips);
                String downloadSizeStr = Common.FormetFileSize(downloadSize);
                String fileSizeStr = Common.FormetFileSize(totalSize);
                tv_tips.setText("已下载...(" + downloadSizeStr + "/" + fileSizeStr + ")");
                TextView tv_state = itemView.findViewById(R.id.tv_state);
                tv_state.setText(formetFileSize(downloadSpeed));
                ImageView iv_state = itemView.findViewById(R.id.iv_state);
                iv_state.setImageResource(R.drawable.download_play);
            }
        }

        private String formetFileSize(float downloadSpeed) {
            BigDecimal bigDecimal;
            String fileSizeString;
            if (downloadSpeed < 1000) {
                bigDecimal = new BigDecimal(downloadSpeed);
                fileSizeString = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "KB/s";
            } else {
                bigDecimal = new BigDecimal(downloadSpeed / 1024);
                fileSizeString = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "M/s";
            }
            return fileSizeString;
        }

        @Override
        public void onFileDownloadStatusPaused(DownloadFileInfo downloadFileInfo) {
            // 下载已被暂停
            String url = downloadFileInfo.getUrl();
            if (url != null && viewMap.get(url) != null) {
                View itemView = viewMap.get(url);
                ImageView iv_state = itemView.findViewById(R.id.iv_state);
                iv_state.setImageResource(R.drawable.download_pause);
                TextView tv_state = itemView.findViewById(R.id.tv_state);
                tv_state.setText("下载暂停");
            }
        }

        @Override
        public void onFileDownloadStatusCompleted(DownloadFileInfo downloadFileInfo) {
            // 下载完成（整个文件已经全部下载完成）
            if (onDownLoadFinishListener != null) {
                onDownLoadFinishListener.onFileDownloadStatusCompleted(downloadFileInfo);
            }
        }

        @Override
        public void onFileDownloadStatusFailed(String url, DownloadFileInfo downloadFileInfo, FileDownloadStatusFailReason failReason) {
            // 下载失败
            if (url != null && viewMap.get(url) != null) {
                View itemView = viewMap.get(url);
                ImageView iv_state = itemView.findViewById(R.id.iv_state);
                iv_state.setImageResource(R.drawable.download_pause);
                TextView tv_state = itemView.findViewById(R.id.tv_state);
                tv_state.setText("下载失败");
            }
        }
    };

    public interface OnDownLoadFinishListener {
        void onFileDownloadStatusCompleted(DownloadFileInfo downloadFileInfo);
    }

    public interface OnSelectedListener {
        void onSelected(List<DownloadFileInfo> mSelect);
    }
}
