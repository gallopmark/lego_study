package com.haoyu.app.fragment;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.CircularProgressView;
import com.haoyu.app.view.RoundRectProgressBar;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoTextureView;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * 创建日期：2017/11/8.
 * 描述:
 * 作者:gallop mark
 */
public class VideoPlayerFragment extends BaseFragment implements View.OnClickListener {
    private Activity activity;
    @BindView(R.id.fl_video)
    FrameLayout fl_video;
    @BindView(R.id.iv_play)
    ImageView iv_play;
    @BindView(R.id.videoView)
    PLVideoTextureView videoView;

    @BindView(R.id.tv_loading)
    TextView tv_loading;   //提示即将播放
    @BindView(R.id.cpvLoading)
    CircularProgressView cpvLoading;  //加载进度条

    @BindView(R.id.fl_controller)
    FrameLayout fl_controller;
    @BindView(R.id.tv_videoTitle)
    TextView tv_videoTitle;
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
    @BindView(R.id.ll_playState)
    LinearLayout ll_playState;
    @BindView(R.id.iv_playState)
    ImageView iv_playState;
    @BindView(R.id.seekbar)
    SeekBar seekbar;
    @BindView(R.id.tv_current)
    TextView tv_current;
    @BindView(R.id.tv_videoSize)
    TextView tv_videoSize;
    @BindView(R.id.iv_expand)
    ImageView iv_expand;

    private String videoUrl, videoTitle;
    private boolean isFullScreen;
    private AudioManager mAudioManager;
    private boolean progress_turn, attrbute_turn;
    private long currentDuration = -1, lastDuration;  //当前播放位置
    private int currentVolume;
    private float mBrightness = -1f; // 亮度
    private String errorMsg;

    private NetWorkReceiver receiver;
    private MaterialDialog materialDialog;
    private boolean openPlayer;
    private boolean openWithMobile = false;

    private final int CODE_ATTRBUTE = 1;
    private final int CODE_ENDGESTURE = 2;

    private OnRequestedOrientation onRequestedOrientation;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public int createView() {
        return R.layout.fragment_videoplayer;
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            videoUrl = bundle.getString("videoUrl");
            videoTitle = bundle.getString("videoTitle");
        }
        if (videoTitle != null && videoTitle.trim().length() > 0) {
            Spanned spanned = Html.fromHtml(videoTitle);
            tv_videoTitle.setText(spanned);
            tv_videoTitle.setVisibility(View.VISIBLE);
        }
        mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 20 * 1000);
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 20 * 1000);
        videoView.setAVOptions(options);
        videoView.setScreenOnWhilePlaying(true);
        receiver = new NetWorkReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        activity.registerReceiver(receiver, filter);
    }

    @Override
    public void setListener() {
        iv_play.setOnClickListener(this);
        iv_playState.setOnClickListener(this);
        iv_expand.setOnClickListener(this);
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
        final GestureDetector detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private boolean firstTouch;
            private boolean volumeControl;
            private boolean toSeek;

            @Override
            public boolean onDown(MotionEvent e) {
                firstTouch = true;
                fl_controller.setVisibility(View.VISIBLE);
                return super.onDown(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float mOldX = e1.getX(), mOldY = e1.getY();
                float deltaY = mOldY - e2.getY();
                final float deltaX = mOldX - e2.getX();
                int screenWidthPixels = ScreenUtils.getScreenWidth(context);
                if (firstTouch) {
                    toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                    volumeControl = mOldX > screenWidthPixels * 0.5f;
                    firstTouch = false;
                }
                if (toSeek) {
                    onProgressSlide(-deltaX / fl_video.getWidth());
                } else {
                    float percent = deltaY / fl_video.getHeight();
                    if (volumeControl) {
                        onVolumeSlide(deltaY);
                    } else {
                        onBrightnessSlide(percent);
                    }
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
        fl_video.setClickable(true);
        fl_video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (!openPlayer) return true;
                view.getParent().requestDisallowInterceptTouchEvent(true);
                if (detector.onTouchEvent(event)) {
                    return true;
                }
                // 处理手势结束
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        endGesture();
                        break;
                }
                return false;
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
        tv_duration.setText(formatDate(currentDuration) + "/" + formatDate(duration));
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
        iv_attribute.setImageResource(R.drawable.ic_brightness);
        progressBar.setMax(100);
        progressBar.setProgress((int) (lpa.screenBrightness * 100));
    }

    //加减音量
    private void onVolumeSlide(float deltaY) {
        if (!attrbute_turn) {
            attrbute_turn = true;
        }
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume == -1) {
            currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (currentVolume < 0)
                currentVolume = 0;
        }
        if (deltaY > 0) {
            currentVolume++;
            if (currentVolume > maxVolume) {
                currentVolume = maxVolume;
            }
        } else {
            currentVolume--;
            if (currentVolume < 0) {
                currentVolume = 0;
            }
        }
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
        if (ll_attribute.getVisibility() != View.VISIBLE) {
            ll_attribute.setVisibility(View.VISIBLE);
        }
        if (currentVolume > 0) {
            iv_attribute.setImageResource(R.drawable.ic_voice_max);
        } else {
            iv_attribute.setImageResource(R.drawable.ic_voice_min);
        }
        progressBar.setMax(maxVolume);
        progressBar.setProgress(currentVolume);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_play:
                if (NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context)) {
                    netStateTips();
                } else {
                    if (!openPlayer) {
                        openPlayer = true;
                        iv_play.setVisibility(View.GONE);
                        playVideo();
                    } else {
                        iv_play.setVisibility(View.GONE);
                        start();
                    }
                }
                break;
            case R.id.iv_playState:
                if (videoView.isPlaying()) {
                    pause();
                } else {
                    if (NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context)) {
                        netStateTips();
                    } else {
                        start();
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
                    iv_play.setVisibility(View.GONE);
                    start();
                }
            });
            dialog.setPositiveButton("取消", null);
            dialog.show();
        } else {
            start();
            toast("当前网络为非Wi-FI环境，请注意您的流量使用情况");
        }
    }

    private void playVideo() {
        idle();
        videoView.setVideoPath(videoUrl);
        videoView.setOnPreparedListener(new PLMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(PLMediaPlayer plMediaPlayer) {
                prepared();
                start();
                videoView.seekTo(lastDuration);
                long maxDuration = plMediaPlayer.getDuration();
                seekbar.setMax((int) maxDuration);
                tv_videoSize.setText(formatDate(maxDuration));
            }
        });
        videoView.setOnInfoListener(new PLMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(PLMediaPlayer plMediaPlayer, int what, int extra) {
                switch (what) {
                    case PLMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        loading();
                        break;
                    case PLMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        playing();
                        break;
                    case PLMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        playing();
                        break;
                }
                return false;
            }
        });
        videoView.setOnBufferingUpdateListener(new PLMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(PLMediaPlayer plMediaPlayer, int progress) {
                long maxDuration = plMediaPlayer.getDuration();
                long secondary = (maxDuration * progress) / 100;
                seekbar.setSecondaryProgress((int) secondary);
            }
        });
        videoView.setOnErrorListener(new PLMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(PLMediaPlayer plMediaPlayer, int errorCode) {
                error(errorCode);
                return false;
            }
        });
        videoView.setOnCompletionListener(new PLMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(PLMediaPlayer plMediaPlayer) {
                completed();
            }
        });
        addSubscription(Flowable.interval(1000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                if (videoView.isPlaying()) {
                    long currentDuration = videoView.getCurrentPosition();
                    seekbar.setProgress((int) currentDuration);
                    tv_current.setText(formatDate(currentDuration));
                }
            }
        }));
    }

    private void idle() {
        if (onRequestedOrientation != null) {
            onRequestedOrientation.onRequested(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
        tv_loading.setText("即将播放...");
        tv_loading.setVisibility(View.VISIBLE);
        if (cpvLoading.getVisibility() != View.GONE) {
            cpvLoading.setVisibility(View.GONE);
        }
    }

    private void prepared() {
        tv_loading.setVisibility(View.GONE);
        if (tv_loading.getVisibility() != View.GONE) {
            tv_loading.setVisibility(View.GONE);
        }
    }

    private void loading() {
        tv_loading.setVisibility(View.GONE);
        cpvLoading.setVisibility(View.VISIBLE);
    }

    private void start() {
        videoView.start();
        iv_playState.setImageResource(R.drawable.ic_pause);
    }

    private void playing() {
        tv_loading.setVisibility(View.GONE);
        cpvLoading.setVisibility(View.GONE);
    }

    private void pause() {
        videoView.pause();
        iv_playState.setImageResource(R.drawable.ic_play);
    }

    private void completed() {
        tv_loading.setVisibility(View.GONE);
        cpvLoading.setVisibility(View.GONE);
        iv_play.setVisibility(View.VISIBLE);
        if (seekbar.getProgress() >= seekbar.getMax()) {
            lastDuration = 0;
        } else {
            lastDuration = seekbar.getProgress();
            if (errorMsg != null) {
                tv_loading.setText(errorMsg);
                tv_loading.setVisibility(View.VISIBLE);
            }
        }
    }

    private void error(int errorCode) {
        if (errorCode == PLMediaPlayer.ERROR_CODE_IO_ERROR) {
            errorMsg = "当前网络不稳定，请检查网络设置";
        } else if (errorCode == PLMediaPlayer.ERROR_CODE_CONNECTION_TIMEOUT) {
            errorMsg = "当前网络不稳定，请检查网络设置";
        } else if (errorCode == PLMediaPlayer.ERROR_CODE_INVALID_URI) {
            errorMsg = "视频文件已失效";
        } else if (errorCode == PLMediaPlayer.ERROR_CODE_404_NOT_FOUND) {
            errorMsg = "视频文件不存在";
        } else {
            errorMsg = "视频播放出错了~";
        }
        cpvLoading.setVisibility(View.GONE);
        iv_play.setVisibility(View.VISIBLE);
    }

    private String formatDate(long ms) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");//初始化Formatter的转换格式。
        return formatter.format(ms);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_ATTRBUTE:
                    ll_attribute.setVisibility(View.GONE);
                    break;
                case CODE_ENDGESTURE:
                    fl_controller.setVisibility(View.GONE);
                    break;
            }
        }
    };

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
        if (materialDialog != null) {
            materialDialog.dismiss();
        }
        if (!openWithMobile) {
            if (videoView.isPlaying()) {
                pause();
                materialDialog = new MaterialDialog(context);
                materialDialog.setTitle("网络提醒");
                materialDialog.setMessage("使用2G/3G/4G网络观看视频会消耗较多流量。确定要开启吗？");
                materialDialog.setNegativeButton("开启", new MaterialDialog.ButtonClickListener() {
                    @Override
                    public void onClick(View v, AlertDialog dialog) {
                        openWithMobile = true;
                        start();
                        dialog.dismiss();
                    }
                });
                materialDialog.setPositiveButton("取消", null);
                materialDialog.show();
            }
        } else {
            if (NetStatusUtil.isConnected(context)) {
                if (!NetStatusUtil.isWifi(context)) {
                    start();
                    toast("当前网络为非Wi-FI环境，请注意您的流量使用情况");
                }
            } else {
                toast("当前网络不稳定，请检查您的网络设置");
            }
        }
    }

    public void setFullScreen(boolean isFullScreen) {
        this.isFullScreen = isFullScreen;
        if (!isFullScreen) {
            iv_expand.setImageResource(R.drawable.quanping);
        } else {
            iv_expand.setImageResource(R.drawable.xiaoping);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }

    @Override
    public void onPause() {
        super.onPause();
        pause();
    }

    @Override
    public void onDestroyView() {
        handler.removeMessages(CODE_ATTRBUTE);
        handler.removeMessages(CODE_ENDGESTURE);
        handler.removeCallbacksAndMessages(null);
        videoView.stopPlayback();
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
