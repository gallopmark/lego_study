package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.haoyu.app.entity.ChildSectionMobileEntity;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.entity.SectionMobileEntity;
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

import static com.haoyu.app.lego.student.R.id.ic_selection_state;


/**
 * 创建日期：2017/6/12 on 10:46
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CourseSectionAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    private Context context;
    public static final int TYPE_LEVEL_0 = 0;
    public static final int TYPE_LEVEL_1 = 1;
    public static final int TYPE_LEVEL_2 = 2;
    private String pressId;
    private Map<String, View> viewMap = new HashMap<>();
    private OnActivityClickCallBack onActivityClickCallBack;

    public CourseSectionAdapter(Context context, List<MultiItemEntity> data) {
        super(data);
        this.context = context;
        addItemType(TYPE_LEVEL_0, R.layout.course_section_item);
        addItemType(TYPE_LEVEL_1, R.layout.course_section_child_item);
        addItemType(TYPE_LEVEL_2, R.layout.course_section_activity_item);
    }

    public void setSelected(String pressId) {
        this.pressId = pressId;
        notifyDataSetChanged();
    }

    public void setOnActivityClickCallBack(OnActivityClickCallBack onActivityClickCallBack) {
        this.onActivityClickCallBack = onActivityClickCallBack;
    }

    public OnFileDownloadStatusListener getmOnFileDownloadStatusListener() {
        return mOnFileDownloadStatusListener;
    }

    @Override
    protected void convert(final BaseViewHolder helper, MultiItemEntity item) {
        switch (helper.getItemViewType()) {
            case TYPE_LEVEL_0:
                SectionMobileEntity item0 = (SectionMobileEntity) item;
                if (item0.getTitle() != null && item0.getTitle().trim().length() > 0)
                    helper.setText(R.id.course_title, item0.getTitle());
                else
                    helper.setText(R.id.course_title, "无标题");
                break;
            case TYPE_LEVEL_1:
                final ChildSectionMobileEntity item1 = (ChildSectionMobileEntity) item;
                if (item1.getCompleteState() != null && item1.getCompleteState().equals("已完成")) {
                    helper.setImageResource(ic_selection_state, R.drawable.state_solid_default);
                } else if (item1.getCompleteState() != null && item1.getCompleteState().equals("complete")) {
                    helper.setImageResource(ic_selection_state, R.drawable.state_solid_default);
                } else if (item1.getCompleteState() != null && item1.getCompleteState().equals("进行中")) {
                    helper.setImageResource(ic_selection_state, R.drawable.state_semicircle_default);
                } else if (item1.getCompleteState() != null && item1.getCompleteState().equals("in_progress")) {
                    helper.setImageResource(ic_selection_state, R.drawable.state_semicircle_default);
                } else {
                    helper.setImageResource(ic_selection_state, R.drawable.state_hollow_default);
                }
                if (item1.getTitle() != null && item1.getTitle().trim().length() > 0)
                    helper.setText(R.id.tv_selection_title, item1.getTitle());
                else
                    helper.setText(R.id.tv_selection_title, "无标题");
                helper.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = helper.getAdapterPosition();
                        if (item1.isExpanded()) {
                            collapse(pos, false);
                        } else {
                            expand(pos, false);
                        }
                    }
                });
                break;
            case TYPE_LEVEL_2:
                final ImageView icType = helper.getView(R.id.ic_selection_activity_type);
                final TextView tvTitle = helper.getView(R.id.tv_selection_activity_title);
                final ImageView icDownload = helper.getView(R.id.ic_download);
                final RelativeLayout rl_download = helper.getView(R.id.rl_download);
                final CircleProgressBar progressBar = helper.getView(R.id.progressBar);
                final ImageView icState = helper.getView(R.id.ic_selection_activity_state);
                final CourseSectionActivity item2 = (CourseSectionActivity) item;
                final String activity_type = item2.getType();
                if (activity_type != null && activity_type.equals("video")) {
                    if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                        icType.setImageResource(R.drawable.progress_video_press);
                    else
                        icType.setImageResource(R.drawable.progress_video_default);
                } else if (activity_type != null && activity_type.equals("html")) {
                    if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                        icType.setImageResource(R.drawable.progress_courseware_press);
                    else
                        icType.setImageResource(R.drawable.progress_courseware_default);
                } else if (activity_type != null && activity_type.equals("discussion")) {
                    if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                        icType.setImageResource(R.drawable.progress_discuss_press);
                    else
                        icType.setImageResource(R.drawable.progress_discuss_default);
                } else if (activity_type != null && activity_type.equals("survey")) {
                    if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                        icType.setImageResource(R.drawable.progress_questionnaire_press);
                    else
                        icType.setImageResource(R.drawable.progress_questionnaire_default);
                } else if (activity_type != null && item2.getId() != null && activity_type.equals("test")) {
                    if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                        icType.setImageResource(R.drawable.progress_test_press);
                    else
                        icType.setImageResource(R.drawable.progress_test_default);
                } else if (activity_type != null && activity_type.equals("assignment")) {
                    if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                        icType.setImageResource(R.drawable.progress_homework_press);
                    else
                        icType.setImageResource(R.drawable.progress_homework_default);
                } else {
                    if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                        icType.setImageResource(R.drawable.course_word_selected);
                    else
                        icType.setImageResource(R.drawable.course_word_default);
                }
                if (item2.getCompleteState() != null && item2.getCompleteState().equals("已完成")) {
                    if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                        icState.setImageResource(R.drawable.state_solid_press);
                    else
                        icState.setImageResource(R.drawable.state_solid_default);
                } else if (item2.getCompleteState() != null && item2.getCompleteState().equals("complete")) {
                    if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                        icState.setImageResource(R.drawable.state_solid_press);
                    else
                        icState.setImageResource(R.drawable.state_solid_default);
                } else if (item2.getCompleteState() != null && item2.getCompleteState().equals("进行中")) {
                    if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                        icState.setImageResource(R.drawable.state_semicircle_press);
                    else
                        icState.setImageResource(R.drawable.state_semicircle_default);
                } else if (item2.getCompleteState() != null && item2.getCompleteState().equals("in_progress")) {
                    if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                        icState.setImageResource(R.drawable.state_semicircle_press);
                    else
                        icState.setImageResource(R.drawable.state_semicircle_default);
                } else {
                    if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                        icState.setImageResource(R.drawable.state_hollow_press);
                    else
                        icState.setImageResource(R.drawable.state_hollow_default);
                }
                if (item2.getTitle() != null && item2.getTitle().trim().length() > 0)
                    tvTitle.setText(item2.getTitle());
                else
                    tvTitle.setText("无标题");
                if (pressId != null && item2.getId() != null && pressId.equals(item2.getId()))
                    tvTitle.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
                else
                    tvTitle.setTextColor(ContextCompat.getColor(context, R.color.blow_gray));
                helper.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setSelected(item2.getId());
                        if (onActivityClickCallBack != null) {
                            onActivityClickCallBack.onActivityClick(item2);
                        }
                    }
                });
                VideoMobileEntity video = item2.getmVideo();
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
                            icDownload.setVisibility(View.VISIBLE);
                        } else if (downloadFileInfo.getStatus() == Status.DOWNLOAD_STATUS_COMPLETED) {
                            icDownload.setVisibility(View.VISIBLE);
                            rl_download.setVisibility(View.GONE);
                            icDownload.setImageResource(R.drawable.download_checkall);
                        } else {
                            icDownload.setVisibility(View.GONE);
                            rl_download.setVisibility(View.VISIBLE);
                            progressBar.setMaxProgress((int) downloadFileInfo.getFileSizeLong());
                            progressBar.setProgress((int) downloadFileInfo.getDownloadedSizeLong());
                        }
                    } else {
                        rl_download.setVisibility(View.GONE);
                        icDownload.setVisibility(View.VISIBLE);
                    }
                } else {
                    rl_download.setVisibility(View.GONE);
                    icDownload.setVisibility(View.GONE);
                }
                if (downloadFileInfo != null && downloadFileInfo.getFilePath() != null
                        && new File(downloadFileInfo.getFilePath()).exists()) {
                    icDownload.setImageResource(R.drawable.download_checkall);
                    icDownload.setEnabled(false);
                } else {
                    icDownload.setEnabled(true);
                    icDownload.setImageResource(R.drawable.course_download_default);
                }
                icDownload.setOnClickListener(new View.OnClickListener() {
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
                viewMap.put(url, helper.itemView);
                break;
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

    public interface OnActivityClickCallBack {
        void onActivityClick(CourseSectionActivity activity);
    }
}
