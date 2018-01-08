package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
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

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.view.RoundRectProgressBar;

import butterknife.BindView;
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
    AppCompatImageView iv_play;
    @BindView(R.id.ijkVideoView)
    IjkVideoView videoView;

    @BindView(R.id.tv_loading)
    TextView tv_loading;   //提示即将播放
    @BindView(R.id.indicator)
    View indicator;  //加载进度条

    @BindView(R.id.iv_isLocked)
    AppCompatImageView iv_isLocked;
    @BindView(R.id.fl_controller)
    FrameLayout fl_controller;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_videoName)
    TextView tv_videoName;
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
    private long currentDuration = -1, lastDuration = -1;  //当前播放位置
    /*** 视频窗口的宽和高*/
    private int playerWidth, playerHeight;
    private int maxVolume, currentVolume = -1;
    private float mBrightness = -1f; // 亮度
    private final int CODE_ATTRBUTE = 1;
    private final int CODE_ENDGESTURE = 2;
    private final int CODE_PROGRESS = 3;

    private boolean isHttp;  //是否是本地文件
    private NetWorkReceiver receiver;
    private MaterialDialog dialog;
    private boolean openPlayer, isPrepared;
    private boolean openWithMobile = false;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_ijkplayer;
    }

    @Override
    public void initView() {
        context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
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
        long position = videoView.getCurrentPosition();
        long duration = videoView.getDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);
        currentDuration = delta + position;
        if (currentDuration > duration) {
            currentDuration = duration;
        } else if (currentDuration <= 0) {
            currentDuration = 0;
            delta = -position;
        }
        int showDelta = (int) delta / 1000;
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
            videoView.seekTo((int) currentDuration);
        }
        handler.removeMessages(CODE_ENDGESTURE);
        handler.sendEmptyMessageDelayed(CODE_ENDGESTURE, 5000);
    }

    @Override
    public void initData() {
        if (NetStatusUtil.isWifi(context)) {   //如果是wifi网络环境直接播放视频
            openPlayer = true;
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
                    start();
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
                    netStateTips();
                } else {
                    if (!openPlayer) {
                        openPlayer = true;
                        playVideo();
                    } else {
                        start();
                    }
                }
                break;
            case R.id.iv_playState:
                if (videoView.isPlaying()) {
                    pause();
                } else {
                    if (isHttp && NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context)) {
                        netStateTips();
                    } else {
                        start();
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

    private void netStateTips() {
        if (!openWithMobile) {
            MaterialDialog dialog = new MaterialDialog(context);
            dialog.setTitle("网络提醒");
            dialog.setMessage("使用2G/3G/4G网络观看视频会消耗较多流量。确定要开启吗？");
            dialog.setNegativeButton("开启", new MaterialDialog.ButtonClickListener() {
                @Override
                public void onClick(View v, AlertDialog dialog) {
                    openWithMobile = true;
                    if (!openPlayer) {
                        openPlayer = true;
                        playVideo();
                    } else {
                        start();
                    }
                }
            });
            dialog.setPositiveButton("取消", null);
            dialog.show();
        } else {
            start();
            toast(context, "当前网络为非Wi-FI环境，请注意您的流量使用情况");
        }
    }

    private void playVideo() {
        idle();
        videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                prepared();
                if (lastDuration > 0) {
                    videoView.seekTo((int) lastDuration);
                }
                start();
                long duration = iMediaPlayer.getDuration();
                seekbar.setMax((int) duration);
                tv_videoSize.setText(generateTime(duration));
            }
        });
        videoView.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int precent) {
                long duration = iMediaPlayer.getDuration();
                long secondary = precent * duration / 100;
                seekbar.setSecondaryProgress((int) secondary);
            }
        });
        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int errorCode, int i1) {
                lastDuration = iMediaPlayer.getCurrentPosition();
                error(errorCode);
                return false;
            }
        });
        videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                completed();
            }
        });
    }

    private void idle() {
        iv_play.setVisibility(View.GONE);
        tv_loading.setText("即将播放...");
        tv_loading.setVisibility(View.VISIBLE);
        videoView.setVideoPath(videoUrl);
    }

    private void prepared() {
        isPrepared = true;
        tv_loading.setVisibility(View.GONE);
    }

    private void start() {
        iv_play.setVisibility(View.GONE);
        tv_loading.setVisibility(View.GONE);//测试专家一
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
        iv_play.setVisibility(View.VISIBLE);
        tv_loading.setVisibility(View.GONE);
    }

    private void error(int errorCode) {
        if (errorCode == -10000) {
            tv_loading.setText("无法播放此视频~");
            tv_loading.setTextColor(ContextCompat.getColor(context, R.color.redcolor));
            tv_loading.setVisibility(View.VISIBLE);
        } else if (errorCode == IMediaPlayer.MEDIA_ERROR_IO) {
            toast(context, "当前网络不顺畅，请检查您的网络设置");
            tv_loading.setVisibility(View.GONE);
        } else {
            tv_loading.setVisibility(View.GONE);
        }
    }

    private String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
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
                    removeMessages(CODE_PROGRESS);
                    break;
            }
        }
    };

    private void setProgress() {
        long position = videoView.getCurrentPosition();
        seekbar.setProgress((int) position);
        tv_current.setText(generateTime(position));
    }

    private class NetWorkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    typeChange();
                }
            }
        }

    }

    private void typeChange() {
        if (!openWithMobile) {
            pause();
            netWorkTips();
        } else {
            if (NetStatusUtil.isConnected(context)) {
                if (!NetStatusUtil.isWifi(context)) {
                    start();
                    toast(context, "当前网络为非Wi-FI环境，请注意您的流量使用情况");
                }
            }
        }
    }

    private void netWorkTips() {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = new MaterialDialog(context);
        dialog.setTitle("网络提醒");
        dialog.setMessage("使用2G/3G/4G网络观看视频会消耗较多流量。确定要开启吗？");
        dialog.setNegativeButton("开启", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                openWithMobile = true;
                if (!openPlayer) {
                    openPlayer = true;
                    playVideo();
                } else {
                    start();
                }
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton("取消", null);
        dialog.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context)) {
            netWorkTips();
        } else {
            start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
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
