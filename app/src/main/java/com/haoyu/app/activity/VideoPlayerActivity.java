package com.haoyu.app.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.haoyu.app.adapter.MFileInfoAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.VideoMobileEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.MyUtils;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.OkHttpClientManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.widget.IjkVideoView;

public class VideoPlayerActivity extends BaseActivity implements View.OnClickListener {
    private VideoPlayerActivity context = this;
    @BindView(R.id.videoView)
    IjkVideoView mVideoView;
    @BindView(R.id.video_layout)
    RelativeLayout videoLayout;
    @BindView(R.id.linear_centercontroll)
    LinearLayout linear_centercontroll;
    @BindView(R.id.bottomControll)
    RelativeLayout bottomControll;
    @BindView(R.id.topControll)
    RelativeLayout topControll;
    @BindView(R.id.warn_controll)
    LinearLayout warnControl;// 当发生错误时的提示
    @BindView(R.id.video_title)
    TextView videoTitle;
    @BindView(R.id.currentTime)
    TextView currentTime;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.video_play)
    AppCompatImageView videoPlay;
    @BindView(R.id.iv_play)
    ImageView iv_play;
    @BindView(R.id.video_seekbar)
    SeekBar videoSeekBar;
    @BindView(R.id.video_framelayout)
    FrameLayout framelayout;
    @BindView(R.id.center_content)
    TextView center_content;
    @BindView(R.id.loadingbar)
    LinearLayout loadingView;
    @BindView(R.id.my_video_zhangjie)
    TextView mRead;//课前阅读
    @BindView(R.id.warn_continue)
    TextView warnContinue;
    @BindView(R.id.warn_content)
    TextView warnContent;
    @BindView(R.id.iv_center)
    AppCompatImageView iv_center;

    private boolean running;
    private boolean isShowing;  //控制栏是否是显示
    private boolean mPause = false; //判断当前是否暂停了
    private boolean isLocal = false;//判断播放的事本地视频还是网络视频

    private boolean isWarn = false;

    private boolean isOpen = false;
    // 处理点击事件
    private boolean isNet;//判断是否非wifi下播放
    private int newPosition = -1; /*滑动屏幕快进到的新位置*/
    private int videoPosition;
    private long mPauseStartTime = 0;
    private long mPausedTime = 0;
    // 开始拖动时的位置
    private long seekbarStartTrackPosition;
    // 拖动到的位置
    private long seekbarEndTrackPosition;
    private float brightness = -1; //亮度
    private int volume = -1;
    private int defaultTime = 5 * 1000;
    private int screenWidthPixels;  //获取屏幕的宽度像素
    private int mMaxVolume;
    private String mVideoPath;
    private String summary;
    private String videoId;//视频id
    private String activityId;//活动Id
    private String workshopId;
    private String type;
    private String netType;//手机的网络状态,3表示是wifi ，0表示当前没有网络，4表示当前是手机流量2G/3G/4G
    private String FLOW = "FLOW";
    private String NONE = "NONE";
    private String WIFI = "WIFI";
    private final int VIDEO_HIDECENTERBOX = 1;// 视屏的亮度
    private final int VIDEO_FORWARD = 2;// 滑动屏幕快进
    private final int VIDEO_SEEKBARFORWARD = 3;// 拖动进度条快进
    private final int VIDEO_HIDECONTROLLBAR = 4;// 隐藏控制栏
    public final int UPDATE_SEEKBAR = 0;
    public final int VIDEO_WARN_MESSAGE = 5;//网络消息提
    private NetReceiver netReceiver;
    private GestureDetector gestureDetector;
    private AudioManager audioManager;
    //课前指导
    private PopupWindow window;
    private ImageView popClose;//关闭课前指导弹出页
    private List<MFileInfo> mFileInfoList = new ArrayList<>();
    private MyHandler videoHandler = new MyHandler(context);

    private class MyHandler extends Handler {
        private WeakReference<Context> reference;

        public MyHandler(Context context) {
            reference = new WeakReference<>(context);
        }

        @Override

        public void handleMessage(Message msg) {
            VideoPlayerActivity context = (VideoPlayerActivity) reference.get();
            if (context == null) {
                return;
            }
            switch (msg.what) {
                case VIDEO_HIDECENTERBOX:
                    hideCenterBox();
                    hideVideoCenterPause();
                    mVideoView.start();
                    break;
                case VIDEO_FORWARD:
                    videoForward();
                    break;
                case VIDEO_SEEKBARFORWARD:
                    videoForward();
                    break;
                case VIDEO_HIDECONTROLLBAR:
                    hideControllBar();
                    break;

                case VIDEO_WARN_MESSAGE:
                    if (NONE.equals(netType)) {
                        showWarnControll();
                    } else {
                        if (videoPosition > 0) {
                            hideWarnControll();
                            videoViewStart();
                        }
                    }
                    break;
                case TIME_CODE:
                    setVideoProgress();
                    videoHandler.sendEmptyMessageDelayed(TIME_CODE, 1000);
                    break;
                case TIME_DIS:
                    videoHandler.removeMessages(TIME_CODE);
                    break;
            }
        }
    }

    @Override
    public int setLayoutResID() {
        return R.layout.activity_videoplayer;
    }

    @Override
    public void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        videoId = getIntent().getStringExtra("videoId");

        activityId = getIntent().getStringExtra("activityId");
        workshopId = getIntent().getStringExtra("workshopId");
        type = getIntent().getStringExtra("type");
        running = getIntent().getBooleanExtra("running", false);
        VideoMobileEntity entity = (VideoMobileEntity) getIntent().getSerializableExtra("attach");
        if (entity != null && entity.getAttchFiles() != null && entity.getAttchFiles().size() > 0) {
            mFileInfoList.addAll(entity.getAttchFiles());
        }
        initContent();
        linear_centercontroll.getBackground().setAlpha(80);
        screenWidthPixels = MyUtils.screenWidthPixels(context);
        topControll.getBackground().setAlpha(80);
        bottomControll.getBackground().setAlpha(80);
        videoSeekBar.setEnabled(true);
        audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        updateVideoCatch();
        //开启播放
        mVideoView.setBufferingIndicator(loadingView);

        MyUtils.Land(context);//取消手机的状态栏
        MyUtils.hideBottomUIMenu(context);//如果手机又虚拟按键则隐藏改虚拟按键

        isLocal = !(mVideoPath != null && (mVideoPath.startsWith("http://") || mVideoPath.startsWith("https://")));
        netReceiver = new NetReceiver();
        IntentFilter fileter = new IntentFilter(Constants.speedAction);
        fileter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(netReceiver, fileter);
        registRxBus();
    }

    @Override
    public void setListener() {
        mRead.setOnClickListener(context);
        warnContinue.setOnClickListener(context);
        framelayout.setClickable(true);
        iv_back.setOnClickListener(context);
        iv_play.setOnClickListener(context);
        videoPlay.setOnClickListener(mStartBtnListener);
        videoPlay.setImageResource(R.drawable.ic_pause_24dp);
        videoSeekBar.setOnSeekBarChangeListener(mSeekBarListener);
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        String action = event.getAction();
        if (action.equals(Constants.speedAction)) {
            String obj = event.getObj().toString();
            if (!isLocal) {
                netType = obj;
                if (NONE.equals(obj)) {
                    isOpen = true;
                    //没有网络
                    mVideoView.pause();
                    if (!isWarn) {
                        isWarn = true;
                        isNet = false;
                        showWarnControll();
                        hideVideoCenterPause();
                        hideCenterBox();
                        warnContent.setText("当前没有网络，\n请开启手机网络");
                    }

                } else if (WIFI.equals(obj)) {
                    isOpen = true;
                    hideWarnControll();
                    if (mVideoView.getDuration() != -1) {
                        if (!mVideoView.isPlaying()) {
                            mVideoView.start();
                        }
                    }
                    if (mVideoView.getDuration() == -1 && videoPosition > 0) {
                        videoViewStart();
                    }
                } else {

                    if (!isNet && isOpen) {
                        mVideoView.pause();
                        hideVideoCenterPause();
                        hideCenterBox();
                        showWarnControll();
                        warnContent.setText("当前是移动流量，\n您要继续播放吗");
                        if (videoPosition > 0 && mVideoView.getDuration() == -1) {
                            videoViewStart();
                            mVideoView.seekTo(videoPosition);

                        }
                    }
                }
            }

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!NONE.equals(netType) || isLocal) {
            mVideoView.start();
            hideVideoCenterPause();

            showVideoPlayImg(R.drawable.ic_pause_24dp);
        }
        if (!isLocal && netType != null && NONE.equals(netType)) {
            mVideoView.pause();
            showWarnControll();
        }
    }

    private void showVideoCenterPause() {
        iv_play.setVisibility(View.VISIBLE);

    }

    private void hideVideoCenterPause() {
        iv_play.setVisibility(View.GONE);
    }


    private void showWarnControll() {
        warnControl.setVisibility(View.VISIBLE);
    }

    private void hideWarnControll() {
        warnControl.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        mVideoView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (window != null && window.isShowing()) {
            window.dismiss();
        }
        unregisterReceiver(netReceiver);
        updateVideoTime(length);
        mVideoView.stopPlayback();
        videoHandler.removeCallbacksAndMessages(null);
    }

    private int seekTime;
    private double interval;//更新时间间隔
    private String activityTitle;

    private void initContent() {
        mVideoPath = getIntent().getStringExtra("videoUrl");
        summary = getIntent().getStringExtra("summary");
        activityTitle = getIntent().getStringExtra("activityTitle");
        seekTime = getIntent().getIntExtra("lastViewTime", 0);
        interval = getIntent().getIntExtra("interval", 30);
        videoTitle.setText(activityTitle);
        if (summary == null && mFileInfoList.size() == 0) {
            mRead.setVisibility(View.GONE);
        }

        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);

        mVideoView.setKeepScreenOn(true);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        mVideoView.setOnPreparedListener(mOnPreparedListener);
        //   mVideoView.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_PAVED_PARENT);
        // 手势监听
        gestureDetector = new GestureDetector(context, new PlayerGestureListener());
        framelayout.setClickable(true);
        framelayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                // TODO Auto-generated method stub
                if (gestureDetector.onTouchEvent(motionEvent))
                    return true;
                // 处理手势结束
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        endGesture();
                        break;
                }
                return false;
            }
        });
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (NetStatusUtil.isWifi(context)) {
            videoViewStart();
        } else {
            iv_play.setVisibility(View.VISIBLE);
            iv_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showWarnControll();
                    warnContent.setText("当前是移动流量，\n您要继续播放吗");
                    isOpen = true;
                    hideIV();
                }
            });
        }
    }


    private void hideIV() {
        iv_play.setVisibility(View.GONE);
    }

    private void showPopWindow() {
        View view = LayoutInflater.from(context).inflate(R.layout.video_courseread_guide, null);
        popClose = getView(view, R.id.pop_close);
        TextView read_guide_content = getView(view, R.id.read_guide_content);
        RecyclerView recyclerView = getView(view, R.id.recyclerView);
        if (summary != null) {
            read_guide_content.setText(summary);
        }
        if (mFileInfoList.size() > 0) {
            LinearLayoutManager manager = new LinearLayoutManager(context);
            manager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(manager);
            MFileInfoAdapter adapter = new MFileInfoAdapter(mFileInfoList);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                    MFileInfo mFileInfo = mFileInfoList.get(position);
                    String url = mFileInfo.getUrl();
                    if (url == null)
                        toast(context, "文件链接不存在");
                    else {
                        Intent intent = new Intent(context, MFileInfoActivity.class);
                        intent.putExtra("fileInfo", mFileInfo);
                        startActivity(intent);

                    }
                }
            });
        } else {
            recyclerView.setVisibility(View.GONE);
        }
        if (window == null) {
            window = new PopupWindow(view, MyUtils.getWidth(context) * 3 / 5, MyUtils.getHeight(context));
        }
        window.setTouchable(true);
        window.setBackgroundDrawable(new BitmapDrawable());
        window.setOutsideTouchable(true);
        window.setFocusable(true);

        popClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (window != null && window.isShowing()) {
                    window.dismiss();
                    window = null;
                }
            }
        });
        window.showAsDropDown(topControll, MyUtils.getWidth(context) * 2 / 5, 0);
    }

    //显示弹出内容
    private void showToastTips(String toast) {
        toast(context, toast);
    }

    //视频错误内容
    IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int errorCode, int i1) {
            mVideoView.pause();
            String message;
            switch (errorCode) {
                case IMediaPlayer.MEDIA_ERROR_IO:
                    message = "该视频暂时无法播放,请稍后重试";
                    showToastTips(message);
                    videoHandler.sendEmptyMessageDelayed(VIDEO_WARN_MESSAGE, 2000);
                    break;
                case IMediaPlayer.MEDIA_ERROR_TIMED_OUT:
                    message = "连接超时,请稍后重试";
                    showToastTips(message);
                    videoHandler.sendEmptyMessageDelayed(VIDEO_WARN_MESSAGE, 2000);
                    break;
                case IMediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                    message = "不支持该格式视频";
                    showToastTips(message);
                    //videoHandler.sendEmptyMessageDelayed(VIDEO_WARN_MESSAGE, 2000);
                    break;
                default:
                    message = "该视频暂时无法播放,请稍后重试";
                    showToastTips(message);
                    videoHandler.sendEmptyMessageDelayed(VIDEO_WARN_MESSAGE, 2000);
                    break;
            }
            return false;
        }
    };

    IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            showToastTips("视频播放完成");
            showVideoCenterPause();
            updateVideoTime(mVideoView.getDuration());
            showVideoPlayImg(R.drawable.ic_play_arrow_24dp);
            clearVideoCatch();
        }
    };

    //缓冲监听
    IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int precent) {
            videoSeekBar.setSecondaryProgress(precent * mVideoView.getDuration() / 100);
        }
    };

    IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            mVideoView.start();
            length = mVideoView.getDuration();
            videoSeekBar.setMax(mVideoView.getDuration());
            if (videoPosition > 0) {
                mVideoView.seekTo(videoPosition);
            } else if (seekTime > 0) {
                mVideoView.seekTo(seekTime);
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener mSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seekbarStartTrackPosition = mVideoView.getCurrentPosition();

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mVideoView.getDuration() != -1) {
                mVideoView.seekTo(seekBar.getProgress());
            }
        }
    };

    //播放按钮
    private View.OnClickListener mStartBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPause = !mPause;
            if (mPause) {
                showVideoPlayImg(R.drawable.ic_play_arrow_24dp);
                mVideoView.pause();
                mPauseStartTime = System.currentTimeMillis();
                showVideoCenterPause();
            } else {
                showVideoPlayImg(R.drawable.ic_pause_24dp);
                mVideoView.start();
                mPausedTime += System.currentTimeMillis() - mPauseStartTime;
                mPauseStartTime = 0;
                hideVideoCenterPause();
            }

        }
    };

    IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int i1) {
            switch (what) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    hideCenterBox();
                    iv_play.setVisibility(View.GONE);
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    if (mVideoView.isPlaying()) {
                        hideVideoCenterPause();
                    } else {
                        showVideoCenterPause();
                    }
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    mVideoView.start();
                    break;

            }
            return false;
        }
    };


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MyUtils.Land(context);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) videoLayout.getLayoutParams();
        int width = MyUtils.getWidth(context);
        int height = MyUtils.getHeight(context);
        params.width = width;
        params.height = height;
        videoLayout.setLayoutParams(params);
    }

    private final int TIME_CODE = 11;
    private final int TIME_DIS = 12;

    class PlayerGestureListener extends
            GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private boolean toSeek;

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstTouch = true;
            return super.onDown(e);
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            float deltaY = mOldY - e2.getY();
            float deltaX = mOldX - e2.getX();
            if (firstTouch) {
                toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                volumeControl = mOldX > screenWidthPixels * 0.5f;
                firstTouch = false;
            }
            if (toSeek) {
                onProgressSlide(-deltaX / framelayout.getWidth());

            } else {
                float percent = deltaY / framelayout.getHeight();
                if (volumeControl) {
                    onVolumeSlide(percent);
                } else {
                    onBrightnessSlide(percent);
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isShowing) {
                hideControllBar();
                videoHandler.sendEmptyMessage(TIME_DIS);
            } else {
                showControllBar();
                videoHandler.sendEmptyMessage(TIME_CODE);
            }
            return true;
        }
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        volume = -1;
        brightness = -1f;
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        hideVideoCenterPause();
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;
        }

        int index = (int) (percent * mMaxVolume) + volume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        if (isLocal || !NONE.equals(netType)) {
            // 变更声音
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
            // 变更进度条
            int i = (int) (index * 1.0 / mMaxVolume * 100);
            String s = i + "%";
            showMessage(s);
            if (i > 0) {
                showImg(R.drawable.ic_volume_up_24dp);
            } else {
                showImg(R.drawable.ic_volume_off_24dp);
            }
            showCenterBox();
            videoHandler.removeMessages(VIDEO_HIDECENTERBOX);
            videoHandler.sendEmptyMessageDelayed(VIDEO_HIDECENTERBOX, defaultTime);
        }

    }

    //滑动屏幕快进
    private void onProgressSlide(float percent) {
        hideVideoCenterPause();
        int position = mVideoView.getCurrentPosition();
        int duration = mVideoView.getDuration();
        int deltaMax = Math.min(100 * 1000, duration - position);
        int delta = (int) (deltaMax * percent);

        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition = 0;
            delta = -position;
        }
        int showDelta = delta / 1000;
        if (showDelta != 0) {
            if (showDelta > 0)
                showImg(R.drawable.ic_fast_forward_24dp);
            else
                showImg(R.drawable.ic_fast_rewind_24dp);

            showMessage(MyUtils.generateTime(newPosition) + "/"
                    + MyUtils.generateTime(mVideoView.getDuration()));
            if (isLocal || !NONE.equals(netType)) {
                showCenterBox();
                videoHandler.removeMessages(VIDEO_FORWARD);
                videoHandler.sendEmptyMessageDelayed(VIDEO_FORWARD, 1 * 500);
                videoHandler.removeMessages(VIDEO_HIDECENTERBOX);
                videoHandler.sendEmptyMessageDelayed(VIDEO_HIDECENTERBOX, 1 * 500);

            }
        }
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        hideVideoCenterPause();
        hideCenterBox();
        if (brightness < 0) {
            brightness = getWindow().getAttributes().screenBrightness;
            if (brightness <= 0.00f) {
                brightness = 0.50f;
            } else if (brightness < 0.01f) {
                brightness = 0.01f;
            }
        }

        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }

        if (isLocal || !NONE.equals(netType)) {
            getWindow().setAttributes(lpa);
            int alpha = (int) (lpa.screenBrightness * 100);

            if (alpha <= 0) {
                showImg(R.drawable.ic_brightness_low_24dp);

            } else if (alpha <= 50) {
                showImg(R.drawable.ic_brightness_mid_24dp);
            } else {
                showImg(R.drawable.ic_brightness_high_24dp);
            }
            showMessage(alpha + "%");
            showCenterBox();
            videoHandler.removeMessages(VIDEO_HIDECENTERBOX);
            videoHandler.sendEmptyMessageDelayed(VIDEO_HIDECENTERBOX, defaultTime);
        }
    }

    private void videoViewStart() {
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.setBufferingIndicator(loadingView);
    }

    private void videoForward() {
        hideVideoCenterPause();
        showCenterBox();
        center_content.setText(MyUtils.generateTime(mVideoView
                .getCurrentPosition()) + "/" + MyUtils.generateTime(mVideoView.getDuration()));
        if (seekbarEndTrackPosition > seekbarStartTrackPosition) {
            showImg(R.drawable.ic_fast_forward_24dp);
        } else {
            showImg(R.drawable.ic_fast_rewind_24dp);
        }

        videoHandler.removeMessages(VIDEO_HIDECENTERBOX);
        videoHandler.removeMessages(VIDEO_SEEKBARFORWARD);
        videoHandler.sendEmptyMessageDelayed(VIDEO_HIDECENTERBOX,
                1 * 500);
        videoHandler.sendEmptyMessage(TIME_CODE);
        seekbarEndTrackPosition = -1;
        seekbarStartTrackPosition = -1;
        hideVideoCenterPause();
        if (newPosition != 0) {
            mVideoView.seekTo(newPosition);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.warn_continue://屏幕提醒
                if (!isLocal) {
                    hideWarnControll();
                    if (FLOW.equals(netType)) {
                        isNet = true;
                        if (mVideoView.getCurrentPosition() == 0 || mVideoView.getDuration() == -1) {
                            videoViewStart();
                        } else if (isOpen) {
                            mVideoView.start();
                        }
                    } else if (NONE.equals(netType)) {
                        Intent intent;
                        //判断手机系统的版本  即API大于10 就是3.0或以上版本
                        if (android.os.Build.VERSION.SDK_INT > 10) {
                            intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                        } else {
                            intent = new Intent();
                            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                            intent.setComponent(component);
                            intent.setAction("android.intent.action.VIEW");
                        }
                        context.startActivity(intent);
                    }
                }
                break;


            case R.id.iv_play:
                //屏幕中间播放按钮
                if (mVideoView.getCurrentPosition() == mVideoView.getDuration()) {
                    mVideoView.seekTo(0);
                } else {
                    if (mVideoView.getCurrentPosition() > 0) {
                        mVideoView.start();
                    } else {
                        videoViewStart();
                    }
                }
                hideVideoCenterPause();
                showVideoPlayImg(R.drawable.ic_pause_24dp);
                break;
            case R.id.pop_close:
                //课前指导
                if (window != null && window.isShowing())
                    window.dismiss();
                break;
            case R.id.my_video_zhangjie:
                showPopWindow();
                break;
            default:
                break;
        }
    }


    // 显示中间按钮
    private void showCenterBox() {
        center_content.setVisibility(View.VISIBLE);
        iv_center.setVisibility(View.VISIBLE);
        linear_centercontroll.setVisibility(View.VISIBLE);

    }

    // 隐藏中间按钮
    private void hideCenterBox() {
        center_content.setVisibility(View.INVISIBLE);
        iv_center.setVisibility(View.INVISIBLE);
        linear_centercontroll.setVisibility(View.INVISIBLE);
    }

    // 设置显示内容
    private void showMessage(String message) {
        if (message != null)
            center_content.setText(message);
    }

    // 设置图片
    private void showImg(int imgId) {
        iv_center.setImageResource(imgId);
    }

    private void showVideoPlayImg(int imgId) {
        videoPlay.setImageResource(imgId);
    }


    // 显示控制层
    private void showControllBar() {
        topControll.setVisibility(View.VISIBLE);
        bottomControll.setVisibility(View.VISIBLE);
        isShowing = true;
        videoHandler.removeMessages(VIDEO_HIDECONTROLLBAR);
        videoHandler
                .sendEmptyMessageDelayed(VIDEO_HIDECONTROLLBAR, defaultTime);
    }

    // 隐藏控制层o'n
    private void hideControllBar() {
        topControll.setVisibility(View.INVISIBLE);
        bottomControll.setVisibility(View.INVISIBLE);
        isShowing = false;
    }

    private long length;

    // 设置进度条进度
    public void setVideoProgress() {
        int time = mVideoView.getCurrentPosition();
        videoSeekBar.setProgress(time);
        if (time > 0) {
            videoPosition = time;
        }
        //更新播放时间
        if (mVideoView.getCurrentPosition() / 1000 > 0 && mVideoView.getCurrentPosition() / 1000 % interval == 0 && mVideoView.isPlaying()) {
            updateVideoTime(mVideoView.getCurrentPosition());
        }
        currentTime.setText(MyUtils.generateTime(mVideoView.getCurrentPosition()) + "/" + MyUtils.generateTime(length));
    }

    //更新当前播放视频缓存
    private void updateVideoCatch() {
        if (running) {
            if (type != null) {
                if (type.equals("course")) {
                    url = Constants.OUTRT_NET + "/" + activityId + "/study/m/video/user/" + videoId + "/updateVideoStatus";
                } else if (type.equals("workshop")) {
                    url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/video/user/" + videoId + "/updateVideoStatus";
                }
                addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {

                    }
                }));
            }
        }
    }

    //更新视频的观看时间
    private void updateVideoTime(long lastUpdateTime) {
        if (running) {
            if (type != null) {
                if (type.equals("course")) {
                    url = Constants.OUTRT_NET + "/" + activityId + "/study/m/video/user/" + videoId + "/updateViewTime";
                } else if (type.equals("workshop")) {
                    url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/video/user/" + videoId + "/updateViewTime";
                }
                Map<String, String> map = new HashMap<>();
                map.put("lastUpdateTime", String.valueOf(lastUpdateTime));
                map.put("isLimit", "true");
                map.put("_method", "PUT");
                addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {
                    }

                    @Override
                    public void onResponse(String response) {

                    }
                }, map));

            }
        }
    }

    String url;

    //清除当前播放视频缓存
    private void clearVideoCatch() {
        if (running) {
            if (type != null) {
                if (type.equals("course")) {
                    url = Constants.OUTRT_NET + "/" + activityId + "/study/m/video/user/" + videoId + "/removeVideoStatus";
                } else if (type.equals("workshop")) {
                    url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/video/user/" + videoId + "/removeVideoStatus";
                }
                addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {

                    }
                }));
            }
        }
    }

    class NetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                int mNetType = getNetworkType(getApplicationContext());
                MessageEvent event = new MessageEvent();
                event.setAction(Constants.speedAction);
                String netWorkType = "";
                if (3 == mNetType) {
                    netWorkType = WIFI;
                } else if (mNetType == 2 || mNetType == 4) {
                    netWorkType = FLOW;
                } else {
                    netWorkType = NONE;
                }
                if (netType != netWorkType) {
                    event.obj = netWorkType;
                    RxBus.getDefault().post(event);
                }
            }

        }

    }

    public static int getNetworkType(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            /** 没有任何网络 */
            return 0;
        }
        if (!networkInfo.isConnected()) {
            /** 网络断开或关闭 */
            return 1;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
            /** 以太网网络 */
            return 2;
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            /** wifi网络，当激活时，默认情况下，所有的数据流量将使用此连接 */
            return 3;
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            /** 移动数据连接,不能与连接共存,如果wifi打开，则自动关闭 */
            switch (networkInfo.getSubtype()) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    /** 2G网络 */
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    /** 3G网络 */
                case TelephonyManager.NETWORK_TYPE_LTE:
                    /** 4G网络 */
                    return 4;
            }
        }
        /** 未知网络 */
        return -1;
    }

}
