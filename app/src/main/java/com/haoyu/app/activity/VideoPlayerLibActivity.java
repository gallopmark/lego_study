package com.haoyu.app.activity;

import android.app.Activity;
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
import android.os.Message;
import android.support.v4.content.ContextCompat;
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
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.RoundRectProgressBar;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoTextureView;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 创建日期：2017/11/27.
 * 描述:视频播放器
 * 作者:xiaoma
 */

public class VideoPlayerLibActivity extends BaseActivity implements View.OnClickListener {
    private VideoPlayerLibActivity context;
    @BindView(R.id.fl_video)
    FrameLayout fl_video;
    @BindView(R.id.iv_play)
    ImageView iv_play;
    @BindView(R.id.videoView)
    PLVideoTextureView videoView;

    @BindView(R.id.tv_loading)
    TextView tv_loading;   //提示即将播放
    @BindView(R.id.indicator)
    LoadingView indicator;  //加载进度条

    @BindView(R.id.fl_controller)
    FrameLayout fl_controller;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_videoName)
    TextView tv_videoName;
    @BindView(R.id.ll_attribute)
    LinearLayout ll_attribute;
    @BindView(R.id.iv_attribute)
    ImageView iv_attribute;
    @BindView(R.id.progressBar)
    RoundRectProgressBar progressBar;
    @BindView(R.id.ll_progress)
    LinearLayout ll_progress;
    @BindView(R.id.iv_direction)
    ImageView iv_direction;
    @BindView(R.id.tv_duration)
    TextView tv_duration;
    @BindView(R.id.iv_playState)
    ImageView iv_playState;
    @BindView(R.id.seekbar)
    SeekBar seekbar;
    @BindView(R.id.tv_current)
    TextView tv_current;
    @BindView(R.id.tv_videoSize)
    TextView tv_videoSize;
    private String videoUrl;
    private AudioManager mAudioManager;
    private boolean progress_turn, attrbute_turn;
    private long currentDuration = -1, lastDuration = -1;  //当前播放位置
    /*** 视频窗口的宽和高*/
    private int playerWidth, playerHeight;
    private int maxVolume, currentVolume = -1;
    private float mBrightness = -1f; // 亮度
    private PLHandler handler;
    private final int CODE_ATTRBUTE = 1;
    private final int CODE_ENDGESTURE = 2;
    private Disposable timer;

    private boolean isHttp;  //是否是本地文件
    private NetWorkReceiver receiver;
    private MaterialDialog dialog;
    private boolean openPlayer, isPrepared;
    private boolean openWithMobile = false;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_videoplayerlib;
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
        if (videoUrl.startsWith("http") || videoUrl.startsWith("https")) {
            isHttp = true;
        } else {
            isHttp = false;
        }
        if (videoTitle == null) {
            videoTitle = Common.getFileName(videoUrl);
        }
        tv_videoName.setText(videoTitle);
        setVideoController();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setVideoView();
        handler = new PLHandler();
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
                fl_controller.setVisibility(View.VISIBLE);
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
            iv_direction.setImageResource(R.drawable.video_btn_fast_forword);
        } else {
            iv_direction.setImageResource(R.drawable.video_btn_back_forword);
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
        iv_attribute.setImageResource(R.drawable.ic_brightness);
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
            iv_attribute.setImageResource(R.drawable.ic_voice_max);
        } else {
            iv_attribute.setImageResource(R.drawable.ic_voice_min);
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

    private void setVideoView() {
        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 20 * 1000);
        videoView.setAVOptions(options);
        initIndicator();
        videoView.setBufferingIndicator(indicator);
    }

    private void initIndicator() {
        indicator.setLoadingText("正在加载");
        indicator.setLoadingTextSize(16);
        indicator.setLoadingTextColor(ContextCompat.getColor(context, R.color.white));
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
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

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
        videoView.setOnPreparedListener(mPreparedListener);
        videoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        videoView.setOnErrorListener(onErrorListener);
        videoView.setOnCompletionListener(onCompletionListener);
    }

    private PLMediaPlayer.OnPreparedListener mPreparedListener = new PLMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(PLMediaPlayer plMediaPlayer, int preparedTime) {
            prepared();
            if (lastDuration > 0) {
                videoView.seekTo(lastDuration);
            }
            start();
            long maxDuration = plMediaPlayer.getDuration();
            seekbar.setMax((int) maxDuration);
            tv_videoSize.setText(generateTime(maxDuration));
        }
    };
    //缓冲监听
    private PLMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new PLMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(PLMediaPlayer plMediaPlayer, int precent) {
            seekbar.setSecondaryProgress((int) (precent * videoView.getCurrentPosition()));

        }
    };
    private PLMediaPlayer.OnErrorListener onErrorListener = new PLMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(PLMediaPlayer plMediaPlayer, int errorCode) {
            lastDuration = plMediaPlayer.getCurrentPosition();
            error(errorCode);
            return false;
        }
    };

    private PLMediaPlayer.OnCompletionListener onCompletionListener = new PLMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(PLMediaPlayer plMediaPlayer) {
            completed();
        }
    };

    private void idle() {
        iv_play.setVisibility(View.GONE);
        tv_loading.setText("即将播放...");
        tv_loading.setVisibility(View.VISIBLE);
        videoView.setVideoPath(videoUrl);
    }

    private void prepared() {
        isPrepared = true;
        tv_loading.setVisibility(View.GONE);
        if (tv_loading.getVisibility() != View.GONE) {
            tv_loading.setVisibility(View.GONE);
        }
        timer = Flowable.interval(1000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                long position = videoView.getCurrentPosition();
                long duration = videoView.getDuration();
                if (duration > 0 && position >= duration) {
                    position = duration;
                    completed();
                }
                setProgress(duration, position);
            }
        });
    }

    private void start() {
        iv_play.setVisibility(View.GONE);
        tv_loading.setVisibility(View.GONE);
        videoView.start();
        iv_playState.setImageResource(R.drawable.ic_pause);
    }

    private void pause() {
        tv_loading.setVisibility(View.GONE);
        iv_play.setVisibility(View.VISIBLE);
        videoView.pause();
        iv_playState.setImageResource(R.drawable.ic_play);
    }

    private void completed() {
        cancelTimer();
        tv_loading.setVisibility(View.GONE);
        lastDuration = 0;
        iv_play.setVisibility(View.VISIBLE);
        tv_loading.setText("播放完毕");
        tv_loading.setVisibility(View.VISIBLE);
    }

    private void error(int errorCode) {
        if (errorCode == PLMediaPlayer.ERROR_CODE_IO_ERROR) {
            toast(context, "当前网络不稳定，请检查您的网络设置");
        }
        if (!NetStatusUtil.isConnected(context)) {
            videoView.pause();
        } else {
            if (!NetStatusUtil.isWifi(context)) {
                videoView.pause();
            }
        }
    }

    private String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds).toString();
        }
    }

    private class PLHandler extends Handler {
        private WeakReference<Activity> reference = new WeakReference<Activity>(context);

        @Override
        public void handleMessage(Message msg) {
            VideoPlayerLibActivity activity = (VideoPlayerLibActivity) reference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case CODE_ATTRBUTE:
                    ll_attribute.setVisibility(View.GONE);
                    break;
                case CODE_ENDGESTURE:
                    fl_controller.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void setProgress(long duration, long position) {
        seekbar.setProgress((int) position);
        tv_current.setText(generateTime(position));
        tv_duration.setText(generateTime(duration));
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
        cancelTimer();
        videoView.stopPlayback();
        handler.removeCallbacksAndMessages(null);
    }

    private void cancelTimer() {
        if (timer != null && !timer.isDisposed()) {
            timer.dispose();
            timer = null;
        }
    }
}
