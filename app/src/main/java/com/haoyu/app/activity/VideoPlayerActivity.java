package com.haoyu.app.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Html;
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
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.pili.pldroid.player.widget.PLVideoView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

public class VideoPlayerActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.videoView)
    PLVideoTextureView mVideoView;
    @BindView(R.id.video_layout)
    RelativeLayout videoLayout;
    @BindView(R.id.linear_centercontroll)
    RelativeLayout linear_centercontroll;
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
    ImageView videoPlay;
    @BindView(R.id.video_centerpause)
    ImageView videoCenterPause;
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
    @BindView(R.id.iv_play)
    ImageView iv_play;

    //课前指导
    PopupWindow window;
    ImageView popClose;//关闭课前指导弹出页
    private String summary;
    private String videoId;//视频id
    private String activityId;//活动Id
    private String workshopId;
    private String type;
    private String mVideoPath;
    private String netType;//手机的网络状态,3表示是wifi ，0表示当前没有网络，4表示当前是手机流量2G/3G/4G
    private long newPosition = -1; /*滑动屏幕快进到的新位置*/
    private long mPauseStartTime = 0;
    private long mPausedTime = 0;
    private long videoPosition;
    // 开始拖动时的位置
    private long seekbarStartTrackPosition;
    // 拖动到的位置
    private long seekbarEndTrackPosition;
    private float brightness = -1; //亮度
    private long length;
    //快进的时间
    private long seekTime;
    // 更新时间间隔
    private int interval;
    private int volume = -1;
    private int mMaxVolume;
    private int defaultTime = 5 * 1000;
    private int screenWidthPixels;  //获取屏幕的宽度像素
    private boolean running;
    private boolean isShowing;  //控制栏是否是显示
    private boolean isNet;//判断是否非wifi下播放
    private boolean isReCheck = false;
    private boolean isLocal = false;//判断播放的事本地视频还是网络视频

    private boolean mPause = false; //判断当前是否暂停了
    private boolean isWarn = false;
    private boolean complete = false;
    private boolean isOpen = false;
    private final int VIDEO_HIDECENTERBOX = 1;// 视屏的亮度
    private final int VIDEO_FORWARD = 2;// 滑动屏幕快进
    private final int VIDEO_SEEKBARFORWARD = 3;// 拖动进度条快进
    private final int VIDEO_HIDECONTROLLBAR = 4;// 隐藏控制栏
    private final int UPDATE_SEEKBAR = 0;
    private final int VIDEO_WARN_MESSAGE = 5;//网络消息提
    private List<MFileInfo> mFileInfoList = new ArrayList<>();
    private VideoPlayerActivity context = this;
    private MyHandler videoHandler = new MyHandler(context);
    private AudioManager audioManager;
    private NetReceiver netReceiver;
    private GestureDetector gestureDetector;

    private class MyHandler extends Handler {
        private WeakReference<Context> reference;

        public MyHandler(Context context) {
            reference = new WeakReference<>(context);
        }

        @Override

        public void handleMessage(Message msg) {
            VideoPlayerActivity context = (VideoPlayerActivity) reference.get();
            if (context == null)
                return;
            switch (msg.what) {
                case VIDEO_HIDECENTERBOX:
                    hideCenterBox();
                    hideVideoCenterPause();
                    mVideoView.start();
                    break;
                case VIDEO_FORWARD:
                    hideVideoCenterPause();
                    if (newPosition != 0) {
                        mVideoView.seekTo(newPosition);
                    }

                    break;
                case VIDEO_SEEKBARFORWARD:
                    hideVideoCenterPause();
                    showCenterBox();
                    center_content.setText(MyUtils.generateTime(mVideoView
                            .getCurrentPosition()));
                    Drawable drawable;
                    // / 这一步必须要做,否则不会显示.
                    if (seekbarEndTrackPosition > seekbarStartTrackPosition)
                        drawable = ContextCompat.getDrawable(context,
                                R.drawable.video_btn_fast_forword);
                    else
                        drawable = ContextCompat.getDrawable(context,
                                R.drawable.video_btn_back_forword);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                            drawable.getMinimumHeight());
                    center_content.setCompoundDrawables(drawable, null, null,
                            null);
                    videoHandler.removeMessages(VIDEO_HIDECENTERBOX);
                    videoHandler.removeMessages(VIDEO_SEEKBARFORWARD);
                    videoHandler.sendEmptyMessageDelayed(VIDEO_HIDECENTERBOX,
                            1 * 500);
                    setVideoProgress();
                    seekbarEndTrackPosition = -1;
                    seekbarStartTrackPosition = -1;
                    break;
                case VIDEO_HIDECONTROLLBAR:
                    hideControllBar();
                    break;
                case UPDATE_SEEKBAR:
                    setVideoProgress();
                    break;
                case VIDEO_WARN_MESSAGE:
                    if (NONE.equals(netType)) {
                        showWarnControll();
                    } else {
                        if (videoPosition > 0) {
                            hideWarnControll();
                            videoViewStart();
                            mVideoView.seekTo(videoPosition);
                        }
                    }

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
        mVideoView.setBufferingIndicator(loadingView);
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
        videoCenterPause.setOnClickListener(context);
        videoPlay.setOnClickListener(mStartBtnListener);
        videoPlay.setImageResource(R.drawable.zanting);
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
                    //没有网络
                    isOpen = true;
                    warnContent.setText("没有网络，请开启网络");
                    mVideoView.pause();
                    hideVideoCenterPause();
                    showWarnControll();
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
                    hideWarnControll();
                    if (!isNet && isOpen) {
                        mVideoView.pause();
                        hideVideoCenterPause();
                        hideCenterBox();
                        showWarnControll();
                        warnContent.setText("当前是移动流量，\n您要继续播放吗");
                        if (mVideoView.getDuration() == -1 && videoPosition > 0) {
                            if (!isReCheck) {
                                isReCheck = true;
                                videoViewStart();
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if (isLocal && !NONE.equals(netType)) {
            mVideoView.start();
            hideVideoCenterPause();
            videoPlay.setImageResource(R.drawable.zanting);
        }
        if (!isLocal && netType != null && NONE.equals(netType)) {
            mVideoView.pause();
            showWarnControll();
        }
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
        updateVideoTime(mVideoView.getCurrentPosition());
        mVideoView.stopPlayback();

        videoHandler.removeCallbacksAndMessages(null);
    }

    private void initContent() {
        mVideoPath = getIntent().getStringExtra("videoUrl");
        summary = getIntent().getStringExtra("summary");
        String activityTitle = getIntent().getStringExtra("activityTitle");
        seekTime = (long) getIntent().getDoubleExtra("lastViewTime", 0);
        interval = getIntent().getIntExtra("interval", 30);
        if (summary == null && mFileInfoList.size() == 0) {
            mRead.setVisibility(View.GONE);
        }
        if (activityTitle != null) {
            videoTitle.setText(Html.fromHtml(activityTitle));
        }
        AVOptions options = new AVOptions();
        // 设置链接超时时间
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 20 * 1000);
        options.setInteger(AVOptions.KEY_MEDIACODEC, 0);
        mVideoView.setAVOptions(options);
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnSeekCompleteListener(mOnSeekCompleteListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_PAVED_PARENT);
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
            setVideoPath();
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

    private void setVideoPath() {
        mVideoView.setVideoPath(mVideoPath);
    }

    private void hideIV() {
        iv_play.setVisibility(View.GONE);
    }

    private void showPopWindow() {
        View view = LayoutInflater.from(context).inflate(R.layout.video_courseread_guide, null);
        popClose = getView(view, R.id.pop_close);
        TextView read_guide_content = getView(view, R.id.read_guide_content);
        RecyclerView recyclerView = getView(view, R.id.recyclerView);
        mRead = findViewById(R.id.my_video_zhangjie);

        if (summary != null)
            read_guide_content.setText(Html.fromHtml(summary));

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
                    if (url == null) {
                        toast(context, "文件链接不存在");
                    } else {
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
        window.setOutsideTouchable(true);
        window.setFocusable(true);
        window.setBackgroundDrawable(new BitmapDrawable());
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
    private PLMediaPlayer.OnErrorListener mOnErrorListener = new PLMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(PLMediaPlayer mp, int errorCode) {
            mVideoView.pause();
            String message;
            switch (errorCode) {
                case PLMediaPlayer.MEDIA_ERROR_UNKNOWN:
                    message = "该视频暂时无法播放";
                    showToastTips(message);
                    videoHandler.sendEmptyMessageDelayed(VIDEO_WARN_MESSAGE, 2000);
                    break;
                default:
                    message = "该视频暂时无法播放,请稍后重试";
                    if (!isWarn) {
                        warnContent.setText("当前没有网络，\n请开启手机网络");
                    } else {
                        showToastTips(message);
                    }
                    break;
            }
            showControllBar();
            hideVideoCenterPause();
            return true;
        }
    };

    private void videoViewStart() {
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.setBufferingIndicator(loadingView);
    }

    private void showVideoCenterPause() {
        videoCenterPause.setVisibility(View.VISIBLE);
    }

    private void hideVideoCenterPause() {
        videoCenterPause.setVisibility(View.GONE);
    }

    private void showWarnControll() {
        warnControl.setVisibility(View.VISIBLE);
    }

    private void hideWarnControll() {
        warnControl.setVisibility(View.GONE);
    }

    private PLMediaPlayer.OnCompletionListener mOnCompletionListener = new PLMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(PLMediaPlayer plMediaPlayer) {
            showToastTips("视频播放完成");
            showVideoCenterPause();
            complete = true;
            updateVideoTime(mVideoView.getDuration());
            videoPlay.setImageResource(R.drawable.ic_play);
            clearVideoCatch();

        }
    };
    //缓冲监听
    private PLMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new PLMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(PLMediaPlayer plMediaPlayer, int precent) {
            videoSeekBar.setSecondaryProgress((int) (precent * mVideoView.getDuration()));
        }
    };

    private PLMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new PLMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(PLMediaPlayer plMediaPlayer) {

        }
    };

    private PLMediaPlayer.OnPreparedListener mOnPreparedListener = new PLMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(PLMediaPlayer plMediaPlayer, int i) {
            setVideoProgress();
            updateVideoTime(1);
            isReCheck = false;
            length = mVideoView.getDuration();
            mVideoView.start();
            videoSeekBar.setMax((int) mVideoView.getDuration());
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
            if (!isLocal && NONE.equals(netType)) {
                mVideoView.pause();
                showWarnControll();
            }
            if (mVideoView.getDuration() != -1 && !NONE.equals(netType) || isLocal) {
                mVideoView.seekTo(seekBar.getProgress());
            }
        }
    };

    //播放按钮
    private View.OnClickListener mStartBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isLocal || !NONE.equals(netType)) {
                mPause = !mPause;
                if (mPause) {
                    videoPlay.setImageResource(R.drawable.ic_play);
                    mVideoView.pause();
                    mPauseStartTime = System.currentTimeMillis();
                    showVideoCenterPause();
                } else {
                    videoPlay.setImageResource(R.drawable.zanting);
                    mVideoView.start();
                    mPausedTime += System.currentTimeMillis() - mPauseStartTime;
                    mPauseStartTime = 0;
                    hideVideoCenterPause();
                }
            }
        }
    };
    /**
     * 视频的方向
     */

    private PLMediaPlayer.OnInfoListener mOnInfoListener = new PLMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(PLMediaPlayer plMediaPlayer, int what, int extra) {
            switch (what) {
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    hideVideoCenterPause();
                    break;
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    if (mVideoView.isPlaying())
                        hideVideoCenterPause();
                    else
                        showVideoCenterPause();
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

    class PlayerGestureListener extends
            GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private boolean toSeek;

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
            } else {
                showControllBar();
            }
            return true;
        }
    }

    //手势结束
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

        // 变更进度条
        int i = (int) (index * 1.0 / mMaxVolume * 100);
        String s = i + "%";
        if (i > 0) {
            showImg(R.drawable.ic_voice_max);
        } else {
            showImg(R.drawable.ic_voice_min);
        }
        showMessage(s);
        if (isLocal || !NONE.equals(netType)) {
            // 变更声音
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
            showCenterBox();
            videoHandler.removeMessages(VIDEO_HIDECENTERBOX);
            videoHandler.sendEmptyMessageDelayed(VIDEO_HIDECENTERBOX, defaultTime);
        }
    }

    //滑动屏幕快进
    private void onProgressSlide(float percent) {
        hideVideoCenterPause();
        long position = mVideoView.getCurrentPosition();
        long duration = mVideoView.getDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);

        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition = 0;
            delta = -position;
        }
        int showDelta = (int) delta / 1000;
        if (showDelta != 0) {
            if (showDelta > 0)
                showImg(R.drawable.video_btn_fast_forword);
            else
                showImg(R.drawable.video_btn_back_forword);


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
            showMessage((int) (lpa.screenBrightness * 100) + "%");
            showImg(R.drawable.ic_brightness_6_white_36dp);
            showCenterBox();
            videoHandler.removeMessages(VIDEO_HIDECENTERBOX);
            videoHandler.sendEmptyMessageDelayed(VIDEO_HIDECENTERBOX, defaultTime);
        }
    }


    // 处理点击事件
    private String FLOW = "FLOW";
    private String NONE = "NONE";
    private String WIFI = "WIFI";

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
                        if (mVideoView.getDuration() == -1) {
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

            case R.id.video_centerpause:
                //屏幕中间播放按钮
                if (isLocal || NONE.equals(netType)) {
                    if (mVideoView.getDuration() == -1) {
                        videoViewStart();
                    } else if (complete) {
                        mVideoView.seekTo(0);
                    } else {
                        mVideoView.start();
                    }

                }
                hideVideoCenterPause();
                videoPlay.setImageResource(R.drawable.zanting);
                break;

            case R.id.pop_close:
                //课前指导
                if (window != null && window.isShowing())
                    window.dismiss();
                break;
            case R.id.my_video_zhangjie:
                showPopWindow();
                break;
        }
    }

    // 显示中间按钮
    private void showCenterBox() {
        center_content.setVisibility(View.VISIBLE);
        linear_centercontroll.setVisibility(View.VISIBLE);
    }

    // 隐藏中间按钮
    private void hideCenterBox() {
        center_content.setVisibility(View.INVISIBLE);
        linear_centercontroll.setVisibility(View.INVISIBLE);
    }

    // 设置显示内容
    private void showMessage(String message) {
        if (message != null)
            center_content.setText(message);
    }

    // 设置图片
    private void showImg(int imgId) {
        Drawable drawable = getResources().getDrawable(imgId);
        // / 这一步必须要做,否则不会显示.
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        center_content.setCompoundDrawables(drawable, null, null, null);
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

    // 隐藏控制层
    private void hideControllBar() {
        topControll.setVisibility(View.INVISIBLE);
        bottomControll.setVisibility(View.INVISIBLE);
        isShowing = false;
    }


    // 设置进度条进度
    public int setVideoProgress() {
        if (mVideoView == null)
            return -1;
        int time = (int) mVideoView.getCurrentPosition();
        if (time > 0) {
            videoPosition = time;
        }
        videoSeekBar.setProgress(time);
        //更新播放时间
        if (mVideoView.getCurrentPosition() / 1000 > 0 && mVideoView.getCurrentPosition() / 1000 % interval == 0 && mVideoView.isPlaying()) {
            updateVideoTime(mVideoView.getCurrentPosition());
        }
        currentTime.setText(MyUtils.generateTime(mVideoView.getCurrentPosition()) + "/" + MyUtils.generateTime(length));
        Message msg = new Message();
        msg.what = UPDATE_SEEKBAR;
        if (videoHandler != null)
            videoHandler.sendMessageDelayed(msg, 1000);

        return time;
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
                map.put("lastViewTime", lastUpdateTime + "");
                if (!complete) {
                    map.put("isLimit", "false");
                }
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
                    url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/video/user/{id}/removeVideoStatus";
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
