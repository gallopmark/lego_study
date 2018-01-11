package com.haoyu.app.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.widget.AppCompatImageView;
import android.text.Html;
import android.text.Spanned;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.view.RoundRectProgressBar;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.widget.IjkVideoView;

/**
 * 创建日期：2018/1/2.
 * 描述:视频播放fragment 小屏全屏切换
 * 作者:xiaoma
 */

public class IJKPlayerFragment extends BaseFragment implements View.OnClickListener {
    private Activity activity;
    private FrameLayout fl_video;
    private ImageView iv_play;
    private LinearLayout ll_netG;    //网络为移动网络时提醒
    private LinearLayout ll_netUseless;   //网络不可用时提醒
    private AppCompatImageView iv_netWifi;
    private IjkVideoView videoView;

    private TextView tv_loading;   //提示即将播放
    private View indicator;  //加载提示框

    private AppCompatImageView iv_isLocked;
    private FrameLayout fl_controller;
    private TextView tv_videoTitle;
    private LinearLayout ll_attribute;
    private AppCompatImageView iv_attribute;
    private RoundRectProgressBar progressBar;
    private LinearLayout ll_progress;
    private AppCompatImageView iv_direction;
    private TextView tv_duration;
    private AppCompatImageView iv_playState;
    private SeekBar seekbar;
    private TextView tv_current;
    private TextView tv_videoSize;
    private AppCompatImageView iv_expand;

    private String videoUrl, videoTitle;
    private boolean isFullScreen;
    private int dp_120, dp_160, dp_25, dp_30, dp_40, dp_80;
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

    private NetWorkReceiver receiver;
    private boolean openPlayer, isPrepared, isCompleted;

    private OnRequestedOrientation onRequestedOrientation;

    private final int STATE_IDLE = 330;
    private final int STATE_ERROR = 331;
    private final int STATE_PREPARED = 332;
    private final int STATE_PLAYING = 333;
    private final int STATE_PAUSED = 334;
    private final int STATE_COMPLETED = 335;
    private int mCurrentState = STATE_IDLE;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
        dp_120 = PixelFormat.dp2px(activity, 120);
        dp_160 = PixelFormat.dp2px(activity, 160);
        dp_25 = PixelFormat.dp2px(activity, 25);
        dp_30 = PixelFormat.dp2px(activity, 30);
        dp_40 = PixelFormat.dp2px(activity, 40);
        dp_80 = PixelFormat.dp2px(activity, 80);
    }

    @Override
    public int createView() {
        return R.layout.fragment_ijkplayer;
    }

    @Override
    public void initView(View view) {
        findViewById(view);
        Bundle bundle = getArguments();
        if (bundle != null) {
            videoUrl = bundle.getString("videoUrl");
            videoTitle = bundle.getString("videoTitle");
        }
        if (videoTitle != null && videoTitle.trim().length() > 0) {
            Spanned spanned = Html.fromHtml(videoTitle);
            tv_videoTitle.setText(spanned);
        }
        setVideoAttrsParma();
        setVideoController();
        videoView.setBufferingIndicator(indicator);
        mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        receiver = new NetWorkReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        activity.registerReceiver(receiver, filter);
    }

    private void findViewById(View view) {
        fl_video = view.findViewById(R.id.fl_video);
        iv_play = view.findViewById(R.id.iv_play);
        ll_netG = view.findViewById(R.id.ll_netG);
        ll_netUseless = view.findViewById(R.id.ll_netUseless);
        iv_netWifi = view.findViewById(R.id.iv_netWifi);
        videoView = view.findViewById(R.id.videoView);
        tv_loading = view.findViewById(R.id.tv_loading);
        indicator = view.findViewById(R.id.indicator);
        iv_isLocked = view.findViewById(R.id.iv_isLocked);
        fl_controller = view.findViewById(R.id.fl_controller);
        tv_videoTitle = view.findViewById(R.id.tv_videoTitle);
        ll_attribute = view.findViewById(R.id.ll_attribute);
        iv_attribute = view.findViewById(R.id.iv_attribute);
        progressBar = view.findViewById(R.id.progressBar);
        ll_progress = view.findViewById(R.id.ll_progress);
        iv_direction = view.findViewById(R.id.iv_direction);
        tv_duration = view.findViewById(R.id.tv_duration);
        iv_playState = view.findViewById(R.id.iv_playState);
        seekbar = view.findViewById(R.id.seekbar);
        tv_current = view.findViewById(R.id.tv_current);
        tv_videoSize = view.findViewById(R.id.tv_videoSize);
        iv_expand = view.findViewById(R.id.iv_expand);
    }

    private void setVideoAttrsParma() {
        FrameLayout.LayoutParams indiParams = (FrameLayout.LayoutParams) indicator.getLayoutParams();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ll_attribute.getLayoutParams();
        LinearLayout.LayoutParams ivParams = (LinearLayout.LayoutParams) iv_attribute.getLayoutParams();
        LinearLayout.LayoutParams wifiParams = (LinearLayout.LayoutParams) iv_netWifi.getLayoutParams();
        if (isFullScreen) {     //全屏
            params.width = dp_160;
            ivParams.width = ivParams.height = dp_30;
            indiParams.width = indiParams.height = dp_40;
            wifiParams.width = wifiParams.height = dp_80;
            tv_videoTitle.setVisibility(View.VISIBLE);
        } else {
            params.width = dp_120;
            ivParams.width = ivParams.height = dp_25;
            indiParams.width = indiParams.height = dp_30;
            wifiParams.width = wifiParams.height = dp_40;
            tv_videoTitle.setVisibility(View.GONE);
        }
        indicator.setLayoutParams(indiParams);
        ll_attribute.setLayoutParams(params);
        iv_attribute.setLayoutParams(ivParams);
        iv_netWifi.setLayoutParams(wifiParams);
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
                if (isFullScreen) {
                    iv_isLocked.setVisibility(View.VISIBLE);
                } else {
                    iv_isLocked.setVisibility(View.GONE);
                }
                if (isLocked) {
                    fl_controller.setVisibility(View.GONE);
                } else {
                    fl_controller.setVisibility(View.VISIBLE);
                }
                handler.sendEmptyMessage(CODE_PROGRESS);
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
                if (fl_video.getParent() != null) {
                    fl_video.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    endGesture();
                    if (fl_video.getParent() != null) {
                        fl_video.getParent().requestDisallowInterceptTouchEvent(false);
                    }
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
            mBrightness = activity.getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f) {
                mBrightness = 0.50f;
            } else if (mBrightness < 0.01f) {
                mBrightness = 0.01f;
            }
        }
        WindowManager.LayoutParams lpa = activity.getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        activity.getWindow().setAttributes(lpa);
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
    public void setListener() {
        iv_play.setOnClickListener(this);
        iv_playState.setOnClickListener(this);
        iv_expand.setOnClickListener(this);
        iv_isLocked.setOnClickListener(this);
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
            case R.id.iv_play:
                if (NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context)) {
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
                    if (NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context)) {
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
            case R.id.iv_expand:
                if (!isFullScreen) {
                    if (onRequestedOrientation != null) {
                        onRequestedOrientation.onRequested(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                } else {
                    if (onRequestedOrientation != null) {
                        onRequestedOrientation.onRequested(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
        isPrepared = true;
        tv_loading.setVisibility(View.GONE);
        iv_play.setVisibility(View.GONE);
        ll_netUseless.setVisibility(View.GONE);
    }

    private void start() {
        iv_play.setVisibility(View.GONE);
        tv_loading.setVisibility(View.GONE);
        videoView.start();
        iv_playState.setImageResource(R.drawable.ic_pause_24dp);
    }

    private void pause() {
        tv_loading.setVisibility(View.GONE);
        iv_play.setVisibility(View.VISIBLE);
        videoView.pause();
        iv_playState.setImageResource(R.drawable.ic_play_arrow_24dp);
    }

    private void completed() {
        isCompleted = true;
        iv_play.setVisibility(View.VISIBLE);
        iv_playState.setImageResource(R.drawable.ic_play_arrow_24dp);
        tv_loading.setVisibility(View.GONE);
    }

    private void error() {
        release();
        if (!NetStatusUtil.isConnected(context)) {
            netUseless();
        } else {
            toast("视频播放出现了点问题~");
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
                    toast("无法打开网络设置界面，请手动打开");
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
                    fl_controller.setVisibility(View.GONE);
                    removeMessages(CODE_PROGRESS);
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

    public void setFullScreen(boolean isFullScreen) {
        this.isFullScreen = isFullScreen;
        if (!isFullScreen) {
            iv_expand.setImageResource(R.drawable.ic_fullscreen_24dp);
            iv_isLocked.setVisibility(View.GONE);
            iv_isLocked.setImageResource(R.drawable.ic_lock_open_24dp);
            isLocked = false;
        } else {
            iv_expand.setImageResource(R.drawable.ic_fullscreen_exit_24dp);
            iv_isLocked.setVisibility(View.VISIBLE);
        }
        setVideoAttrsParma();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (openPlayer) {
            if (NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context)) {
                netWorkTips();
            } else {
                if (!isCompleted) {
                    statusChange(STATE_PLAYING);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (openPlayer) {
            statusChange(STATE_PAUSED);
        }
    }

    @Override
    public void onDestroyView() {
        videoView.stopPlayback();
        handler.removeCallbacksAndMessages(null);
        activity.unregisterReceiver(receiver);
        super.onDestroyView();
    }

    public interface OnRequestedOrientation {
        void onRequested(int orientation);
    }

    public void setOnRequestedOrientation(OnRequestedOrientation onRequestedOrientation) {
        this.onRequestedOrientation = onRequestedOrientation;
    }
}
