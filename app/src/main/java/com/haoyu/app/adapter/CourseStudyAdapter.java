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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.CourseChildSectionEntity;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.entity.CourseSectionEntity;
import com.haoyu.app.entity.MultiItemEntity;
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
 * 创建日期：2017/9/12 on 14:12
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CourseStudyAdapter extends BaseArrayRecyclerAdapter<MultiItemEntity> {

    private Context context;
    public static final int TYPE_LEVEL_0 = 0;
    public static final int TYPE_LEVEL_1 = 1;
    public static final int TYPE_LEVEL_2 = 2;
    private String pressId;
    private Map<Integer, Boolean> collapses = new HashMap<>();
    private Map<String, View> viewMap = new HashMap<>();
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public CourseStudyAdapter(Context context, List<MultiItemEntity> mDatas) {
        super(mDatas);
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return mDatas.get(position).getItemType();
    }

    public void setSelected(String pressId) {
        this.pressId = pressId;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onActivityClickCallBack) {
        this.onItemClickListener = onActivityClickCallBack;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public OnFileDownloadStatusListener getmOnFileDownloadStatusListener() {
        return mOnFileDownloadStatusListener;
    }

    @Override
    public int bindView(int viewtype) {
        if (viewtype == TYPE_LEVEL_0)
            return R.layout.course_section_item;
        else if (viewtype == TYPE_LEVEL_1)
            return R.layout.course_section_child_item;
        else
            return R.layout.course_section_activity_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, MultiItemEntity item, final int position) {
        int viewType = holder.getItemViewType();
        if (viewType == TYPE_LEVEL_0) {
            final CourseSectionEntity sectionEntity = (CourseSectionEntity) item;
            final TextView tv_title = holder.obtainView(R.id.section_title);
            setSpannedText(sectionEntity.getTitle(), tv_title);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (onItemLongClickListener != null)
                        onItemLongClickListener.onItemLongClick(tv_title, getSpanned(sectionEntity.getTitle()));
                    return false;
                }
            });
        } else if (viewType == TYPE_LEVEL_1) {
            final CourseChildSectionEntity childEntity = (CourseChildSectionEntity) item;
            ImageView ic_selection_state = holder.obtainView(R.id.ic_selection_state);
            final TextView tv_title = holder.obtainView(R.id.tv_selection_title);
            if (childEntity.getCompleteState() != null && childEntity.getCompleteState().equals("已完成"))
                ic_selection_state.setImageResource(R.drawable.state_solid_default);
            else if (childEntity.getCompleteState() != null && childEntity.getCompleteState().equals("complete"))
                ic_selection_state.setImageResource(R.drawable.state_solid_default);
            else if (childEntity.getCompleteState() != null && childEntity.getCompleteState().equals("进行中"))
                ic_selection_state.setImageResource(R.drawable.state_semicircle_default);
            else if (childEntity.getCompleteState() != null && childEntity.getCompleteState().equals("in_progress"))
                ic_selection_state.setImageResource(R.drawable.state_semicircle_default);
            else
                ic_selection_state.setImageResource(R.drawable.state_hollow_default);
            setSpannedText(childEntity.getTitle(), tv_title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (collapses.get(position) != null && collapses.get(position)) {
                        for (int i = 0; i < childEntity.getActivities().size(); i++)
                            childEntity.getActivities().get(i).setVisiable(false);
                        collapses.put(position, false);
                    } else {
                        for (int i = 0; i < childEntity.getActivities().size(); i++)
                            childEntity.getActivities().get(i).setVisiable(true);
                        collapses.put(position, true);
                    }
                    notifyDataSetChanged();
                    if (onItemClickListener != null)
                        onItemClickListener.onChildSectionClick(position + childEntity.getActivities().size());
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (onItemLongClickListener != null)
                        onItemLongClickListener.onItemLongClick(tv_title, getSpanned(childEntity.getTitle()));
                    return false;
                }
            });
        } else {
            final ImageView icType = holder.obtainView(R.id.ic_selection_activity_type);
            final TextView tvTitle = holder.obtainView(R.id.tv_selection_activity_title);
            final ImageView icDownload = holder.obtainView(R.id.ic_download);
            final RelativeLayout rl_download = holder.obtainView(R.id.rl_download);
            final CircleProgressBar progressBar = holder.obtainView(R.id.progressBar);
            final ImageView icState = holder.obtainView(R.id.ic_selection_activity_state);
            final CourseSectionActivity activity = (CourseSectionActivity) item;
            LinearLayout.LayoutParams params;
            if (activity.isVisiable())
                params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            else
                params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
            holder.itemView.setLayoutParams(params);
            final String activity_type = activity.getType();
            if (activity_type != null && activity_type.equals("video")) {
                if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                    icType.setImageResource(R.drawable.progress_video_press);
                else
                    icType.setImageResource(R.drawable.progress_video_default);
            } else if (activity_type != null && activity_type.equals("html")) {
                if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                    icType.setImageResource(R.drawable.progress_courseware_press);
                else
                    icType.setImageResource(R.drawable.progress_courseware_default);
            } else if (activity_type != null && activity_type.equals("discussion")) {
                if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                    icType.setImageResource(R.drawable.progress_discuss_press);
                else
                    icType.setImageResource(R.drawable.progress_discuss_default);
            } else if (activity_type != null && activity_type.equals("survey")) {
                if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                    icType.setImageResource(R.drawable.progress_questionnaire_press);
                else
                    icType.setImageResource(R.drawable.progress_questionnaire_default);
            } else if (activity_type != null && activity.getId() != null && activity_type.equals("test")) {
                if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                    icType.setImageResource(R.drawable.progress_test_press);
                else
                    icType.setImageResource(R.drawable.progress_test_default);
            } else if (activity_type != null && activity_type.equals("assignment")) {
                if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                    icType.setImageResource(R.drawable.progress_homework_press);
                else
                    icType.setImageResource(R.drawable.progress_homework_default);
            } else {
                if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                    icType.setImageResource(R.drawable.course_word_selected);
                else
                    icType.setImageResource(R.drawable.course_word_default);
            }
            if (activity.getCompleteState() != null && activity.getCompleteState().equals("已完成")) {
                if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                    icState.setImageResource(R.drawable.state_solid_press);
                else
                    icState.setImageResource(R.drawable.state_solid_default);
            } else if (activity.getCompleteState() != null && activity.getCompleteState().equals("complete")) {
                if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                    icState.setImageResource(R.drawable.state_solid_press);
                else
                    icState.setImageResource(R.drawable.state_solid_default);
            } else if (activity.getCompleteState() != null && activity.getCompleteState().equals("进行中")) {
                if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                    icState.setImageResource(R.drawable.state_semicircle_press);
                else
                    icState.setImageResource(R.drawable.state_semicircle_default);
            } else if (activity.getCompleteState() != null && activity.getCompleteState().equals("in_progress")) {
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
            setSpannedText(activity.getTitle(), tvTitle);
            if (pressId != null && activity.getId() != null && pressId.equals(activity.getId()))
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
            else
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.blow_gray));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setSelected(activity.getId());
                    if (onItemClickListener != null) {
                        onItemClickListener.onActivityClick(activity);
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (onItemLongClickListener != null)
                        onItemLongClickListener.onItemLongClick(tvTitle, getSpanned(activity.getTitle()));
                    return false;
                }
            });
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
            viewMap.put(url, holder.itemView);
        }
    }

    private void setSpannedText(String title, TextView tv) {
        if (title == null || title.trim().length() == 0) {
            tv.setText("无标题");
        } else {
            Spanned spanned = Html.fromHtml(title);
            tv.setText(spanned.toString());
        }
    }

    private Spanned getSpanned(String title) {
        Spanned spanned = Html.fromHtml(title);
        SpannableString ss = new SpannableString(spanned);
        if (title.contains("<sup>"))
            ss.setSpan(new SuperscriptSpan(), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        else if (title.contains("<sub>"))
            ss.setSpan(new SubscriptSpan(), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        else
            return spanned;
        return ss;
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

    public interface OnItemClickListener {
        void onChildSectionClick(int position);

        void onActivityClick(CourseSectionActivity activity);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(TextView tv, CharSequence charSequence);
    }
}
