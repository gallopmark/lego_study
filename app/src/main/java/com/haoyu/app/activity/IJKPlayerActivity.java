package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.VideoMobileEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.RoundRectProgressBar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.widget.IjkVideoView;

/**
 * 创建日期：2018/1/2.
 * 描述:视频播放器
 * 作者:xiaoma
 */

public class IJKPlayerActivity extends BaseActivity implements View.OnClickListener {
    private IJKPlayerActivity context;
    @BindView(R.id.fl_video)
    FrameLayout fl_video;
    @BindView(R.id.iv_play)
    ImageView iv_play;
    @BindView(R.id.ll_netG)
    LinearLayout ll_netG;    //网络为移动网络时提醒
    @BindView(R.id.ll_netUseless)
    LinearLayout ll_netUseless;   //网络不可用时提醒

    @BindView(R.id.ijkVideoView)
    IjkVideoView videoView;

    @BindView(R.id.tv_loading)
    TextView tv_loading;   //提示即将播放
    @BindView(R.id.indicator)
    View indicator;  //加载进度条

    @BindView(R.id.iv_isLocked)
    AppCompatImageView iv_isLocked;  //控制器加锁解锁

    @BindView(R.id.fl_controller)
    FrameLayout fl_controller;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_videoName)
    TextView tv_videoName;
    @BindView(R.id.tv_guide)
    TextView tv_guide;
    @BindView(R.id.ll_attribute)
    LinearLayout ll_attribute;
    @BindView(R.id.iv_attribute)
    AppCompatImageView iv_attribute;
    @BindView(R.id.progressBar)
    RoundRectProgressBar progressBar;
    @BindView(R.id.ll_progress)
    LinearLayout ll_progress;
    @BindView(R.id.iv_direction)
    AppCompatImageView iv_direction;
    @BindView(R.id.tv_duration)
    TextView tv_duration;
    @BindView(R.id.iv_playState)
    AppCompatImageView iv_playState;
    @BindView(R.id.seekbar)
    SeekBar seekbar;
    @BindView(R.id.tv_current)
    TextView tv_current;
    @BindView(R.id.tv_videoSize)
    TextView tv_videoSize;
    private String videoUrl;
    private AudioManager mAudioManager;
    private boolean progress_turn, attrbute_turn, isLocked;  //isLocked是否锁住屏幕
    private int currentDuration = -1;  //当前播放位置
    private long lastDuration = -1; //最后播放位置（即播放出错时的位置）
    /*** 视频窗口的宽和高*/
    private int playerWidth, playerHeight;
    private int maxVolume, currentVolume = -1;
    private float mBrightness = -1f; // 亮度
    private final int CODE_ATTRBUTE = 1;
    private final int CODE_ENDGESTURE = 2;
    private final int CODE_PROGRESS = 3;

    private boolean isHttp;  //是否是本地文件
    private NetWorkReceiver receiver;
    private boolean openPlayer, isPrepared, isCompleted;

    private boolean isVideoUser, running;   //是否是工作坊音视频，是否在工作坊培训时间内
    private long interval;
    private final int CODE_UPDATE_VIEWTIME = 4;
    private String statusUrl, updateUrl, infoUrl;
    private final int STATE_IDLE = 330;
    private final int STATE_ERROR = 331;
    private final int STATE_PREPARED = 332;
    private final int STATE_PLAYING = 333;
    private final int STATE_PAUSED = 334;
    private final int STATE_COMPLETED = 335;
    private int mCurrentState = STATE_IDLE;

    @Override
    public int setLayoutResID() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        return R.layout.activity_ijkplayer;
    }

    @Override
    public void initView() {
        context = this;
        String videoType = getIntent().getStringExtra("videoType");
        if (videoType != null && videoType.equals("course")) {
            isVideoUser = true;
            running = getIntent().getBooleanExtra("running", false);
            String activityId = getIntent().getStringExtra("activityId");
            String videoId = getIntent().getStringExtra("videoId");
            statusUrl = Constants.OUTRT_NET + "/" + activityId + "/study/m/video/user/" + videoId + "/updateVideoStatus";
            updateUrl = Constants.OUTRT_NET + "/" + activityId + "/study/m/video/user/" + videoId + "/updateViewTime";
            infoUrl = Constants.OUTRT_NET + "/" + activityId + "/study/m/activity/ncts/" + activityId + "/view";
            VideoMobileEntity video = (VideoMobileEntity) getIntent().getSerializableExtra("video");
            /*间隔多少毫秒提交视频观看时间，默认30秒*/
            if (video.getInterval() > 0) {
                interval = video.getInterval() * 1000;
            } else {
                interval = 30 * 1000;
            }
            if (video.getSummary() != null || video.getAttchFiles().size() > 0) {
                setVideo(video);
            }
        } else if (videoType != null && videoType.equals("workshop")) {
            isVideoUser = true;
            running = getIntent().getBooleanExtra("running", false);
            String workshopId = getIntent().getStringExtra("workshopId");
            String activityId = getIntent().getStringExtra("activityId");
            String videoId = getIntent().getStringExtra("videoId");
            statusUrl = Constants.OUTRT_NET + "/student_" + workshopId + "/m/video/user/" + videoId + "/updateVideoStatus";
            updateUrl = Constants.OUTRT_NET + "/student_" + workshopId + "/m/video/user/" + videoId + "/updateViewTime";
            infoUrl = Constants.OUTRT_NET + "/student_" + workshopId + "/m/activity/wsts/" + activityId + "/view";
            VideoMobileEntity video = (VideoMobileEntity) getIntent().getSerializableExtra("video");
            /*间隔多少毫秒提交视频观看时间，默认30秒*/
            if (video.getInterval() > 0) {
                interval = video.getInterval() * 1000;
            } else {
                interval = 30 * 1000;
            }
            if (video.getSummary() != null || video.getAttchFiles().size() > 0) {
                setVideo(video);
            }
        }
        videoUrl = getIntent().getStringExtra("videoUrl");
        String videoTitle = getIntent().getStringExtra("videoTitle");
        if (TextUtils.isEmpty(videoUrl)) {
            toast(context, "视频链接不存在");
            finish();
            return;
        }
        isHttp = videoUrl.startsWith("http") || videoUrl.startsWith("https");
        if (videoTitle == null) {
            videoTitle = Common.getFileName(videoUrl);
        }
        tv_videoName.setText(videoTitle);
        setVideoController();
        videoView.setBufferingIndicator(indicator);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (isHttp) {
            receiver = new NetWorkReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(receiver, filter);
        }
    }

    private void setVideo(final VideoMobileEntity video) {
        tv_guide.setVisibility(View.VISIBLE);
        tv_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeMessages(CODE_ENDGESTURE);
                showRightTopDialog(video);
            }
        });
    }

    private void showRightTopDialog(final VideoMobileEntity video) {
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.dialogFullScreen).create();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_video_summary, null);
        ImageView iv_close = view.findViewById(R.id.iv_close);
        TextView tv_summary = view.findViewById(R.id.tv_summary);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        if (TextUtils.isEmpty(video.getSummary())) {
            tv_summary.setVisibility(View.GONE);
        } else {
            tv_summary.setVisibility(View.VISIBLE);
            tv_summary.setText(video.getSummary());
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        MFileInfoAdapter adapter = new MFileInfoAdapter(video.getAttchFiles());
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                Intent intent = new Intent(context, MFileInfoActivity.class);
                MFileInfo fileInfo = video.getAttchFiles().get(position);
                intent.putExtra("fileInfo", fileInfo);
                startActivity(intent);
            }
        });
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.sendEmptyMessageDelayed(CODE_ENDGESTURE, 5000);
            }
        });
        int width = ScreenUtils.getScreenWidth(context) * 3 / 5;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, RelativeLayout.LayoutParams.MATCH_PARENT);
        dialog.setContentView(view, params);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.RIGHT | Gravity.TOP);
    }

    private class MFileInfoAdapter extends BaseArrayRecyclerAdapter<MFileInfo> {

        public MFileInfoAdapter(List<MFileInfo> mDatas) {
            super(mDatas);
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.fileinfo_item;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, MFileInfo fileInfo, int position) {
            ImageView iv_fileType = holder.obtainView(R.id.iv_fileType);
            TextView tv_mFileName = holder.obtainView(R.id.tv_mFileName);
            TextView tv_mFileSize = holder.obtainView(R.id.tv_mFileSize);
            View divider = holder.obtainView(R.id.divider);
            Common.setFileType(fileInfo.getUrl(), iv_fileType);
            tv_mFileName.setText(fileInfo.getFileName());
            tv_mFileSize.setText(Common.FormetFileSize(fileInfo.getFileSize()));
            tv_mFileName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tv_mFileSize.setTextColor(ContextCompat.getColor(context, R.color.white));
            divider.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
        }
    }

    private void setVideoController() {
        /** 获取视频播放窗口的尺寸 */
        ViewTreeObserver viewObserver = fl_video.getViewTreeObserver();
        viewObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    fl_video.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    fl_video.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                playerWidth = fl_video.getWidth();
                playerHeight = fl_video.getHeight();
            }
        });
        final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private boolean firstTouch;
            private boolean gesture_progress;
            private boolean gesture_volume;
            private boolean gesture_bright;

            @Override
            public boolean onDown(MotionEvent e) {
                firstTouch = true;
                iv_isLocked.setVisibility(View.VISIBLE);
                if (isLocked) {
                    fl_controller.setVisibility(View.GONE);
                } else {
                    fl_controller.setVisibility(View.VISIBLE);
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float mOldX = e1.getX(), mOldY = e1.getY();
                float deltaY = mOldY - e2.getY();
                float deltaX = mOldX - e2.getX();
                if (firstTouch) {
                    gesture_progress = Math.abs(distanceX) >= Math.abs(distanceY);
                    gesture_volume = mOldX > playerWidth * 3.0 / 5; // 音量
                    gesture_bright = mOldX < playerWidth * 2.0 / 5; // 亮度
                    firstTouch = false;
                }
                if (gesture_progress) {
                    float percentage = -deltaX / playerWidth;
                    onProgressSlide(percentage);
                } else {
                    float percent = deltaY / playerHeight;
                    if (gesture_volume) {
                        onVolumeSlide(percent);
                    } else if (gesture_bright) {
                        onBrightnessSlide(percent);
                    }
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }

        });
        fl_video.setClickable(true);
        fl_video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // 手势里除了singleTapUp，没有其他检测up的方法
                if (!isPrepared) return true;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    endGesture();
                }
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void onProgressSlide(float percent) {
        if (ll_progress.getVisibility() != View.VISIBLE) {
            ll_progress.setVisibility(View.VISIBLE);
        }
        //计算并显示 前进后退
        if (!progress_turn) {
            progress_turn = true;
        }
        int position = videoView.getCurrentPosition();
        int duration = videoView.getDuration();
        int deltaMax = Math.min(100 * 1000, duration - position);
        int delta = (int) (deltaMax * percent);
        currentDuration = delta + position;
        if (currentDuration > duration) {
            currentDuration = duration;
        } else if (currentDuration <= 0) {
            currentDuration = 0;
            delta = -position;
        }
        int showDelta = delta / 1000;
        if (showDelta > 0) {
            iv_direction.setImageResource(R.drawable.ic_fast_forward_24dp);
        } else {
            iv_direction.setImageResource(R.drawable.ic_fast_rewind_24dp);
        }
        tv_duration.setText(generateTime(currentDuration) + "/" + generateTime(duration));
    }

    /*滑动改变亮度*/
    private void onBrightnessSlide(float percent) {
        if (!attrbute_turn) {
            attrbute_turn = true;
        }
        if (mBrightness < 0) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f) {
                mBrightness = 0.50f;
            } else if (mBrightness < 0.01f) {
                mBrightness = 0.01f;
            }
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        getWindow().setAttributes(lpa);
        if (ll_attribute.getVisibility() != View.VISIBLE) {
            ll_attribute.setVisibility(View.VISIBLE);
        }
        if (lpa.screenBrightness >= 0.45 && lpa.screenBrightness <= 0.55) {
            iv_attribute.setImageResource(R.drawable.ic_brightness_mid_24dp);
        } else if (lpa.screenBrightness > 0.55) {
            iv_attribute.setImageResource(R.drawable.ic_brightness_high_24dp);
        } else {
            iv_attribute.setImageResource(R.drawable.ic_brightness_low_24dp);
        }
        progressBar.setMax(100);
        progressBar.setProgress((int) (lpa.screenBrightness * 100));
    }

    //加减音量
    private void onVolumeSlide(float percent) {
        if (!attrbute_turn) {
            attrbute_turn = true;
        }
        if (currentVolume < 0) {
            currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
            if (currentVolume < 0) {
                currentVolume = 0;
            }
        }
        int mVolume = (int) (percent * maxVolume) + currentVolume;
        if (mVolume > maxVolume) {
            mVolume = maxVolume;
        } else if (mVolume < 0) {
            mVolume = 0;
        }
        if (ll_attribute.getVisibility() != View.VISIBLE) {
            ll_attribute.setVisibility(View.VISIBLE);
        }
        if (mVolume > 0) {
            iv_attribute.setImageResource(R.drawable.ic_volume_up_24dp);
        } else {
            iv_attribute.setImageResource(R.drawable.ic_volume_off_24dp);
        }
        progressBar.setMax(maxVolume);
        progressBar.setProgress(mVolume);
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolume, 0);
    }

    private void endGesture() {
        currentVolume = -1;
        mBrightness = -1f;
        if (attrbute_turn) {
            attrbute_turn = false;
            handler.removeMessages(CODE_ATTRBUTE);
            handler.sendEmptyMessageDelayed(CODE_ATTRBUTE, 1000);
        }
        if (progress_turn) {
            ll_progress.setVisibility(View.GONE);
            progress_turn = false;
            videoView.seekTo(currentDuration);
        }
        handler.removeMessages(CODE_ENDGESTURE);
        handler.sendEmptyMessageDelayed(CODE_ENDGESTURE, 5000);
    }

    @Override
    public void initData() {
        if (isVideoUser && running) {
            updateVideoStatus();
        }
        if (NetStatusUtil.isWifi(context)) {   //如果是wifi网络环境直接播放视频
            playVideo();
        }
    }

    @Override
    public void setListener() {
        iv_play.setOnClickListener(context);
        iv_back.setOnClickListener(context);
        iv_playState.setOnClickListener(context);
        iv_isLocked.setOnClickListener(context);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !videoView.isPlaying()) {
                    statusChange(STATE_PLAYING);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                videoView.seekTo(seekBar.getProgress());
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_play:
                if (isHttp && NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context)) {
                    netWorkTips();
                } else {
                    if (!openPlayer) {
                        playVideo();
                    } else {
                        statusChange(STATE_PLAYING);
                    }
                }
                break;
            case R.id.iv_playState:
                if (videoView.isPlaying()) {
                    statusChange(STATE_PAUSED);
                } else {
                    if (isHttp && NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context)) {
                        netWorkTips();
                    } else {
                        if (!openPlayer) {
                            playVideo();
                        } else {
                            statusChange(STATE_PLAYING);
                        }
                    }
                }
                break;
            case R.id.iv_isLocked:
                if (isLocked) {
                    iv_isLocked.setImageResource(R.drawable.ic_lock_open_24dp);
                    fl_controller.setVisibility(View.VISIBLE);
                    handler.sendEmptyMessageDelayed(CODE_ENDGESTURE, 5000);
                    isLocked = false;
                } else {
                    iv_isLocked.setImageResource(R.drawable.ic_lock_close_24dp);
                    fl_controller.setVisibility(View.GONE);
                    isLocked = true;
                }
                break;
        }
    }

    private void playVideo() {
        statusChange(STATE_IDLE);
        videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                statusChange(STATE_PREPARED);
                if (lastDuration > 0) {
                    iMediaPlayer.seekTo(lastDuration);
                    lastDuration = -1;
                }
                statusChange(STATE_PLAYING);
                handler.sendEmptyMessage(CODE_PROGRESS);
                int duration = (int) iMediaPlayer.getDuration();
                seekbar.setMax(duration);
                tv_videoSize.setText(generateTime(duration));
            }
        });
        videoView.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int precent) {
                int duration = videoView.getDuration();
                int secondary = precent * duration / 100;
                seekbar.setSecondaryProgress(secondary);
            }
        });
        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int errorCode, int i1) {
                if (lastDuration < 0) {
                    lastDuration = iMediaPlayer.getCurrentPosition();
                }
                statusChange(STATE_ERROR);
                return false;
            }
        });
        videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                statusChange(STATE_COMPLETED);
            }
        });
    }

    private void statusChange(int newStatus) {
        mCurrentState = newStatus;
        if (mCurrentState == STATE_IDLE) {
            idle();
        } else if (mCurrentState == STATE_PREPARED) {
            prepared();
        } else if (mCurrentState == STATE_PLAYING) {
            start();
        } else if (mCurrentState == STATE_PAUSED) {
            pause();
        } else if (mCurrentState == STATE_COMPLETED) {
            completed();
        } else if (mCurrentState == STATE_ERROR) {
            error();
        }
    }

    private void idle() {
        openPlayer = true;
        isCompleted = false;
        iv_play.setVisibility(View.GONE);
        tv_loading.setText("即将播放...");
        tv_loading.setVisibility(View.VISIBLE);
        fl_controller.setEnabled(true);
        videoView.setVideoPath(videoUrl);
    }

    private void prepared() {
        mCurrentState = STATE_PREPARED;
        isPrepared = true;
        tv_loading.setVisibility(View.GONE);
        iv_play.setVisibility(View.GONE);
        ll_netUseless.setVisibility(View.GONE);
    }

    private void start() {
        mCurrentState = STATE_PLAYING;
        iv_play.setVisibility(View.GONE);
        tv_loading.setVisibility(View.GONE);
        videoView.start();
        iv_playState.setImageResource(R.drawable.ic_pause_24dp);
        if (isVideoUser && running) {
            handler.removeMessages(CODE_UPDATE_VIEWTIME);
            handler.sendEmptyMessageDelayed(CODE_UPDATE_VIEWTIME, interval);
        }
    }

    private void pause() {
        mCurrentState = STATE_PAUSED;
        tv_loading.setVisibility(View.GONE);
        iv_play.setVisibility(View.VISIBLE);
        videoView.pause();
        iv_playState.setImageResource(R.drawable.ic_play_arrow_24dp);
        if (isVideoUser && running) {
            handler.removeMessages(CODE_UPDATE_VIEWTIME);
        }
    }

    private void completed() {
        mCurrentState = STATE_COMPLETED;
        isCompleted = true;
        iv_play.setVisibility(View.VISIBLE);
        iv_playState.setImageResource(R.drawable.ic_play_arrow_24dp);
        tv_loading.setVisibility(View.GONE);
        if (isVideoUser && running) {
            handler.removeMessages(CODE_UPDATE_VIEWTIME);
        }
    }

    private void error() {
        mCurrentState = STATE_ERROR;
        release();
        if (!NetStatusUtil.isConnected(context)) {
            netUseless();
        } else {
            toast(context, "视频播放出现了点问题~");
        }
    }

    private String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    private void release() {
        iv_play.setVisibility(View.VISIBLE);
        iv_playState.setImageResource(R.drawable.ic_play_arrow_24dp);
        tv_loading.setVisibility(View.GONE);
        openPlayer = false;
        isPrepared = false;
        iv_isLocked.setVisibility(View.GONE);
        fl_controller.setVisibility(View.GONE);
        fl_controller.setEnabled(false);
        videoView.stopPlayback();
    }

    private void netUseless() {
        iv_play.setVisibility(View.GONE);
        ll_netUseless.setVisibility(View.VISIBLE);
        TextView tv_netUseless = ll_netUseless.findViewById(R.id.tv_netUseless);
        tv_netUseless.setText("网络连接不可用\n点击屏幕打开网络设置");
        ll_netUseless.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); //直接进入手机中的wifi网络设置界面
                } catch (Exception e) {
                    toast(context, "无法打开网络设置界面，请手动打开");
                }
            }
        });
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_ATTRBUTE:
                    ll_attribute.setVisibility(View.GONE);
                    break;
                case CODE_PROGRESS:
                    setProgress();
                    sendEmptyMessageDelayed(CODE_PROGRESS, 1000);
                    break;
                case CODE_ENDGESTURE:
                    iv_isLocked.setVisibility(View.GONE);
                    fl_controller.setVisibility(View.GONE);
                    break;
                case CODE_UPDATE_VIEWTIME:
                    updateViewTime();
                    sendEmptyMessageDelayed(CODE_UPDATE_VIEWTIME, interval);
                    break;
            }
        }
    };

    private void setProgress() {
        int position = videoView.getCurrentPosition();
        seekbar.setProgress(position);
        tv_current.setText(generateTime(position));
    }

    private class NetWorkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (NetStatusUtil.isConnected(context)) {
                    if (ll_netUseless.getVisibility() != View.GONE) {
                        ll_netUseless.setVisibility(View.GONE);
                    }
                    if (mCurrentState == STATE_ERROR || mCurrentState == STATE_COMPLETED) {
                        if (iv_play.getVisibility() != View.VISIBLE) {
                            iv_play.setVisibility(View.VISIBLE);
                        }
                    }
                    if (openPlayer && !NetStatusUtil.isWifi(context)) {
                        //如果播放过程中网络状态进入移动网络，则记录当前播放位置（为下次播放seek）停止播放并回收资源
                        if (lastDuration < 0) {
                            lastDuration = videoView.getCurrentPosition();
                        }
                        release();
                        netWorkTips();
                    }
                }
            }
        }
    }

    private void netWorkTips() {
        if (ll_netG.getVisibility() != View.VISIBLE) {
            iv_play.setVisibility(View.GONE);
            ll_netG.setVisibility(View.VISIBLE);
            TextView tv_netTips = ll_netG.findViewById(R.id.tv_netTips);
            tv_netTips.setText("您正在使用移动网络播放视频\n可能产生较高流量费用");
            TextView tv_cancel = ll_netG.findViewById(R.id.tv_cancel);
            TextView tv_continue = ll_netG.findViewById(R.id.tv_continue);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.tv_cancel:
                            iv_play.setVisibility(View.VISIBLE);
                            ll_netG.setVisibility(View.GONE);
                            break;
                        case R.id.tv_continue:
                            ll_netG.setVisibility(View.GONE);
                            if (!openPlayer) {
                                playVideo();
                            } else {
                                statusChange(STATE_PLAYING);
                            }
                            break;
                    }
                }
            };
            tv_cancel.setOnClickListener(listener);
            tv_continue.setOnClickListener(listener);
        }
    }

    /*更新当前播放视频缓存 */
    private void updateVideoStatus() {
        addSubscription(OkHttpClientManager.getAsyn(context, statusUrl, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {

            }
        }));
    }

    /*更新视频播放时间*/
    private void updateViewTime() {
        int lastUpdateTime = videoView.getCurrentPosition();
        Map<String, String> map = new HashMap<>();
        map.put("lastUpdateTime", String.valueOf(lastUpdateTime));
        map.put("isLimit", "true");
        map.put("_method", "PUT");
        addSubscription(OkHttpClientManager.postAsyn(context, updateUrl, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(BaseResponseResult response) {
                getActivityInfo();
            }
        }, map));
    }

    /*获取活动信息（课程视频学习完成状态）*/
    private void getActivityInfo() {
        addSubscription(OkHttpClientManager.getAsyn(context, infoUrl, new OkHttpClientManager.ResultCallback<AppActivityViewResult>() {
            @Override
            public void onError(Request request, Exception e) {
                onNetWorkError(context);
            }

            @Override
            public void onResponse(AppActivityViewResult response) {
                if (response != null && response.getResponseData() != null
                        && response.getResponseData().getmActivityResult() != null
                        && response.getResponseData().getmActivityResult().getmActivity() != null) {
                    CourseSectionActivity activity = response.getResponseData().getmActivityResult().getmActivity();
                    Intent intent = new Intent();
                    intent.putExtra("activity", activity);
                    setResult(RESULT_OK, intent);
                }
            }
        }));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context)) {
            netWorkTips();
        } else {
            if (openPlayer && !isCompleted) {
                statusChange(STATE_PLAYING);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (openPlayer) {
            statusChange(STATE_PAUSED);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        videoView.stopPlayback();
        handler.removeCallbacksAndMessages(null);
    }
}
