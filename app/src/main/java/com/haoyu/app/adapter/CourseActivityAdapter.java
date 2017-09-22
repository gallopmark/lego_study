package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.entity.VideoMobileEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.CircleProgressBar;

import org.wlf.filedownloader.DownloadFileInfo;
import org.wlf.filedownloader.FileDownloader;
import org.wlf.filedownloader.base.Status;
import org.wlf.filedownloader.listener.OnFileDownloadStatusListener;
import org.wlf.filedownloader.listener.simple.OnSimpleFileDownloadStatusListener;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建日期：2017/6/12 on 9:15
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CourseActivityAdapter extends BaseArrayRecyclerAdapter<CourseSectionActivity> {
    private Context context;
    private String pressId;
    private Map<String, View> viewMap = new HashMap<>();

    public CourseActivityAdapter(Context context, List<CourseSectionActivity> mDatas) {
        super(mDatas);
        this.context = context;
    }

    public void setSelected(String pressId) {
        this.pressId = pressId;
        notifyDataSetChanged();
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.course_microclass_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, CourseSectionActivity activity, int position) {
        ImageView iv_type = holder.obtainView(R.id.ic_selection_activity_type);
        TextView tv_title = holder.obtainView(R.id.tv_selection_activity_title);
        ImageView icState = holder.obtainView(R.id.ic_selection_activity_state);
        ImageView ic_download = holder.obtainView(R.id.ic_download);
        final RelativeLayout rl_download = holder.obtainView(R.id.rl_download);
        final CircleProgressBar progressBar = holder.obtainView(R.id.progressBar);
        if (activity.getType() != null && activity.getType().equals("video")) {
            if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                iv_type.setImageResource(R.drawable.progress_video_press);
            else
                iv_type.setImageResource(R.drawable.progress_video_default);
        } else if (activity.getType() != null && activity.getType().equals("html")) {
            if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                iv_type.setImageResource(R.drawable.progress_courseware_press);
            else
                iv_type.setImageResource(R.drawable.progress_courseware_default);
        } else if (activity.getType() != null && activity.getType().equals("discussion")) {
            if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                iv_type.setImageResource(R.drawable.progress_discuss_press);
            else
                iv_type.setImageResource(R.drawable.progress_discuss_default);
        } else if (activity.getType() != null && activity.getType().equals("survey")) {
            if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                iv_type.setImageResource(R.drawable.progress_questionnaire_press);
            else
                iv_type.setImageResource(R.drawable.progress_questionnaire_default);
        } else if (activity.getType() != null && activity.getType().equals("test")) {
            if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                iv_type.setImageResource(R.drawable.progress_test_press);
            else
                iv_type.setImageResource(R.drawable.progress_test_default);
        } else if (activity.getType() != null && activity.getType().equals("assignment")) {
            if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                iv_type.setImageResource(R.drawable.progress_homework_press);
            else
                iv_type.setImageResource(R.drawable.progress_homework_default);
        } else {
            if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                iv_type.setImageResource(R.drawable.course_word_selected);
            else
                iv_type.setImageResource(R.drawable.course_word_default);
        }
        if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
            tv_title.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        else
            tv_title.setTextColor(ContextCompat.getColor(context, R.color.blow_gray));
        setActivityTitle(activity.getTitle(), tv_title);
        if (activity.getCompleteState() != null && activity.getCompleteState().equals("已完成")) {
            if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                icState.setImageResource(R.drawable.state_solid_press);
            else
                icState.setImageResource(R.drawable.state_solid_default);
        } else if (activity.getCompleteState() != null && activity.getCompleteState().equals("进行中")) {
            if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                icState.setImageResource(R.drawable.state_semicircle_press);
            else
                icState.setImageResource(R.drawable.state_semicircle_default);
        } else {
            if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                icState.setImageResource(R.drawable.state_hollow_press);
            else
                icState.setImageResource(R.drawable.state_hollow_default);
        }
        VideoMobileEntity video = activity.getmVideo();
        final String url;
        if (video != null && video.getUrls() != null && video.getUrls().length() > 0) {
            url = video.getUrls();
        } else if (video != null && video.getVideoFiles() != null && video.getVideoFiles().size() > 0) {
            url = video.getVideoFiles().get(0).getUrl();
        } else if (video != null && video.getAttchFiles() != null && video.getAttchFiles().size() > 0) {
            url = video.getAttchFiles().get(0).getUrl();
        } else {
            url = null;
        }
        DownloadFileInfo downloadFileInfo;
        if (url != null)
            downloadFileInfo = FileDownloader.getDownloadFile(url);
        else
            downloadFileInfo = null;
        if (video != null && video.getAllowDownload() != null && video.getAllowDownload().equals("Y")) {
            if (downloadFileInfo != null) {
                if (downloadFileInfo.getStatus() == Status.DOWNLOAD_STATUS_FILE_NOT_EXIST) {
                    rl_download.setVisibility(View.GONE);
                    ic_download.setVisibility(View.VISIBLE);
                } else if (downloadFileInfo.getStatus() == Status.DOWNLOAD_STATUS_COMPLETED) {
                    ic_download.setVisibility(View.VISIBLE);
                    rl_download.setVisibility(View.GONE);
                    ic_download.setImageResource(R.drawable.download_checkall);
                } else {
                    ic_download.setVisibility(View.GONE);
                    rl_download.setVisibility(View.VISIBLE);
                    progressBar.setMaxProgress((int) downloadFileInfo.getFileSizeLong());
                    progressBar.setProgress((int) downloadFileInfo.getDownloadedSizeLong());
                }
            } else {
                rl_download.setVisibility(View.GONE);
                ic_download.setVisibility(View.VISIBLE);
            }
        } else {
            rl_download.setVisibility(View.GONE);
            ic_download.setVisibility(View.GONE);
        }
        if (downloadFileInfo != null && downloadFileInfo.getFilePath() != null
                && new File(downloadFileInfo.getFilePath()).exists()) {
            ic_download.setImageResource(R.drawable.download_checkall);
            ic_download.setEnabled(false);
        } else {
            ic_download.setEnabled(true);
            ic_download.setImageResource(R.drawable.course_download_default);
        }
        ic_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (url != null) {
                    beginDownload(url);
                }
            }
        });
        rl_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadFileInfo fileInfo = FileDownloader.getDownloadFile(url);
                if (fileInfo != null && fileInfo.getStatus() == Status.DOWNLOAD_STATUS_DOWNLOADING) {
                    FileDownloader.pause(url);
                } else {
                    beginDownload(url);
                }
            }
        });
        viewMap.put(url, holder.itemView);
    }

    private void setActivityTitle(String title, TextView tv_title) {
        if (title == null || title.trim().length() == 0)
            tv_title.setText("无标题");
        else {
            Spanned spanned = Html.fromHtml(title);
            SpannableString ss = new SpannableString(spanned);
            if (title.contains("<sup>")) {
                ss.setSpan(new SuperscriptSpan(), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_title.setText(ss);
            } else if (title.contains("<sub>")) {
                ss.setSpan(new SubscriptSpan(), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_title.setText(ss);
            } else {
                tv_title.setText(spanned);
            }
        }
    }

    private void beginDownload(final String url) {
        DownloadFileInfo fileInfo = FileDownloader.getDownloadFile(url);
        if (fileInfo != null && fileInfo.getStatus() == Status.DOWNLOAD_STATUS_FILE_NOT_EXIST)
            FileDownloader.delete(url, true, null);
        FileDownloader.start(url);
        FileDownloader.registerDownloadStatusListener(mOnFileDownloadStatusListener);
    }

    private OnFileDownloadStatusListener mOnFileDownloadStatusListener = new OnSimpleFileDownloadStatusListener() {

        @Override
        public void onFileDownloadStatusPreparing(DownloadFileInfo downloadFileInfo) {
            // 准备中（即，正在连接资源）
            String url = downloadFileInfo.getUrl();
            if (url != null && viewMap.get(url) != null) {
                View itemView = viewMap.get(url);
                final ImageView icDownload = itemView.findViewById(R.id.ic_download);
                final RelativeLayout rl_download = itemView.findViewById(R.id.rl_download);
                icDownload.setVisibility(View.GONE);
                rl_download.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFileDownloadStatusPrepared(DownloadFileInfo downloadFileInfo) {
            // 已准备好（即，已经连接到了资源）
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
                ImageView downloadOrPause = itemView.findViewById(R.id.downloadOrPause);
                downloadOrPause.setImageResource(R.drawable.download_play);
            }
        }

        @Override
        public void onFileDownloadStatusPaused(DownloadFileInfo downloadFileInfo) {
            // 下载已被暂停
            String url = downloadFileInfo.getUrl();
            if (url != null && viewMap.get(url) != null) {
                View itemView = viewMap.get(url);
                ImageView downloadOrPause = itemView.findViewById(R.id.downloadOrPause);
                downloadOrPause.setImageResource(R.drawable.download_pause);
            }
        }

        @Override
        public void onFileDownloadStatusCompleted(DownloadFileInfo downloadFileInfo) {
            // 下载完成（整个文件已经全部下载完成）
            String url = downloadFileInfo.getUrl();
            if (url != null && viewMap.get(url) != null) {
                View itemView = viewMap.get(url);
                final ImageView icDownload = itemView.findViewById(R.id.ic_download);
                final RelativeLayout rl_download = itemView.findViewById(R.id.rl_download);
                icDownload.setImageResource(R.drawable.download_checkall);
                icDownload.setVisibility(View.VISIBLE);
                rl_download.setVisibility(View.GONE);
            }
        }

        @Override
        public void onFileDownloadStatusFailed(String url, DownloadFileInfo downloadFileInfo, FileDownloadStatusFailReason failReason) {
            // 下载已被暂停
            if (url != null && viewMap.get(url) != null) {
                View itemView = viewMap.get(url);
                ImageView downloadOrPause = itemView.findViewById(R.id.downloadOrPause);
                downloadOrPause.setImageResource(R.drawable.download_pause);
            }
        }
    };

}
