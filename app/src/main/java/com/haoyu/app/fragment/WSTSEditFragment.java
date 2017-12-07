package com.haoyu.app.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.haoyu.app.activity.SearchUsersActivity;
import com.haoyu.app.adapter.DictEntryAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.DictEntryMobileEntity;
import com.haoyu.app.entity.DictEntryResult;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MWorkshopActivity;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.WorkshopActivityResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.RoundRectProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.disposables.Disposable;
import okhttp3.Request;

/**
 * 创建日期：2017/12/1.
 * 描述:描述:工作坊添加听课评课
 * 作者:xiaoma
 */

public class WSTSEditFragment extends BaseFragment implements View.OnClickListener {
    private Activity context;
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.et_content)
    EditText et_content;//标题，内容
    @BindView(R.id.tv_stage)
    TextView tv_stage;
    @BindView(R.id.tv_subject)
    TextView tv_subject;
    @BindView(R.id.et_textBook)
    EditText et_textBook;
    @BindView(R.id.ll_lecturer)
    LinearLayout ll_lecturer;
    @BindView(R.id.tv_lecture)
    TextView tv_lecturer;//授课人
    @BindView(R.id.cb_field)
    CheckBox cb_field;   //现场评课
    @BindView(R.id.cb_record)
    CheckBox cb_record;  //实录评课
    /*添加视频附件*/
    @BindView(R.id.fl_addVideo)
    FrameLayout fl_addVideo;
    @BindView(R.id.fl_video)
    FrameLayout fl_video;
    @BindView(R.id.iv_video)
    ImageView iv_video;
    @BindView(R.id.ll_videoProgress)
    LinearLayout ll_videoProgress;
    @BindView(R.id.tv_videoName)
    TextView tv_videoName;
    @BindView(R.id.videoProgressBar)
    RoundRectProgressBar videoProgressBar;
    @BindView(R.id.tv_videoProgress)
    TextView tv_videoProgress;
    @BindView(R.id.tv_videoError)
    TextView tv_videoError;
    @BindView(R.id.iv_deleteVideo)
    ImageView iv_deleteVideo;
    @BindView(R.id.bt_next)
    Button bt_next;
    private String workshopId, workSectionId;
    private List<DictEntryMobileEntity> stageList;//学段集合
    private List<DictEntryMobileEntity> subjectList;//学科集合
    private String stageId, subjectId, lecturerId;
    private int stageIndex = -1, subjectIndex = -1;
    private int REQUEST_USER = 1;
    private OnNextListener onNextListener;
    private boolean isOpenMobile;
    private Disposable mVideoPosable;
    private boolean isNeedFile = true, isUploading;  //是否是现场评课，视频是否正在上传
    private MFileInfo mVideoInfo;
    private final int CODE_VIDEO = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_VIDEO:
                    Bundle bundle = msg.getData();
                    long totalBytes = bundle.getLong("totalBytes");
                    long remainingBytes = bundle.getLong("remainingBytes");
                    videoProgressBar.setMax((int) totalBytes);
                    videoProgressBar.setProgress((int) (totalBytes - remainingBytes));
                    long progress = (totalBytes - remainingBytes) * 100 / totalBytes;
                    tv_videoProgress.setText("上传中" + "\u2000" + progress + "%");
                    break;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (Activity) context;
        Bundle bundle = getArguments();
        workshopId = bundle.getString("workshopId");
        workSectionId = bundle.getString("workSectionId");
        lecturerId = getUserId();
    }

    @Override
    public int createView() {
        return R.layout.fragment_wstsedit;
    }

    @Override
    public void initView(View view) {
        tv_lecturer.setText(getRealName());
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                final EditText et = (EditText) view;
                if (canVerticalScroll(et)) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                return false;
            }
        };
        et_title.setOnTouchListener(onTouchListener);
        et_content.setOnTouchListener(onTouchListener);
        et_textBook.setOnTouchListener(onTouchListener);
    }

    private boolean canVerticalScroll(EditText editText) {
        //滚动的距离
        int scrollY = editText.getScrollY();
        //控件内容的总高度
        int scrollRange = editText.getLayout().getHeight();
        //控件实际显示的高度
        int scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() - editText.getCompoundPaddingBottom();
        //控件内容总高度与实际显示高度的差值
        int scrollDifference = scrollRange - scrollExtent;
        if (scrollDifference == 0) {
            return false;
        }
        return (scrollY > 0) || (scrollY < scrollDifference - 1);
    }


    @Override
    public void setListener() {
        tv_stage.setOnClickListener(this);
        tv_subject.setOnClickListener(this);
        ll_lecturer.setOnClickListener(this);
        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
                switch (cb.getId()) {
                    case R.id.cb_field:
                        if (isChecked) {
                            isNeedFile = false;
                            cb_record.setChecked(false);
                        } else {
                            isNeedFile = true;
                            cb_record.setChecked(true);
                        }
                        break;
                    case R.id.cb_record:
                        if (isChecked) {
                            isNeedFile = true;
                            cb_field.setChecked(false);
                            fl_addVideo.setVisibility(View.VISIBLE);
                        } else {
                            isNeedFile = false;
                            cb_field.setChecked(true);
                            fl_addVideo.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        };
        cb_field.setOnCheckedChangeListener(onCheckedChangeListener);
        cb_record.setOnCheckedChangeListener(onCheckedChangeListener);
        fl_addVideo.setOnClickListener(this);
        iv_deleteVideo.setOnClickListener(this);
        bt_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Common.hideSoftInput(context);
        switch (view.getId()) {
            case R.id.tv_stage:
                if (stageList == null) {
                    initStage();
                } else {
                    showPopWindow(1, tv_stage, stageList);
                }
                break;
            case R.id.tv_subject:
                if (subjectList == null) {
                    initSubject();
                } else {
                    showPopWindow(2, tv_subject, subjectList);
                }
                break;
            case R.id.ll_lecturer:  /*授课人*/
                Intent intent = new Intent(context, SearchUsersActivity.class);
                startActivityForResult(intent, REQUEST_USER);
                break;
            case R.id.fl_addVideo:
                picketVideo();
                break;
            case R.id.iv_deleteVideo:
                MaterialDialog videoDialog = new MaterialDialog(context);
                videoDialog.setTitle("提示");
                videoDialog.setMessage("确定删除此附件吗？");
                videoDialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
                    @Override
                    public void onClick(View v, AlertDialog dialog) {
                        deleteVideo();
                    }
                });
                videoDialog.setNegativeButton("取消", null);
                videoDialog.show();
                break;
            case R.id.bt_next:
                if (checkOut()) {
                    submitLcec();
                }
                break;
        }
    }

    /**
     * 访问学段条目
     */
    private void initStage() {
        String url = Constants.OUTRT_NET + "/m/textBook?textBookTypeCode=STAGE";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<DictEntryResult>() {

            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError();
            }

            @Override
            public void onResponse(DictEntryResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    stageList = new ArrayList<>();
                    DictEntryMobileEntity entity = new DictEntryMobileEntity();
                    entity.setTextBookName("所有学段");
                    stageList.add(entity);
                    stageList.addAll(response.getResponseData());
                    showPopWindow(1, tv_stage, stageList);
                }
            }
        }));
    }

    /*获取学科 */
    private void initSubject() {
        String url = Constants.OUTRT_NET + "/m/textBook?textBookTypeCode=SUBJECT";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<DictEntryResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError();
            }

            @Override
            public void onResponse(DictEntryResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    subjectList = new ArrayList<>();
                    DictEntryMobileEntity entity = new DictEntryMobileEntity();
                    entity.setTextBookName("所有学科");
                    subjectList.add(entity);
                    subjectList.addAll(response.getResponseData());
                    showPopWindow(2, tv_subject, subjectList);
                }
            }
        }));
    }

    private void showPopWindow(final int type, final TextView tv, final List<DictEntryMobileEntity> mDatas) {
        Drawable shouqi = ContextCompat.getDrawable(context, R.drawable.course_dictionary_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(), shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context, R.drawable.course_dictionary_xiala);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(), zhankai.getMinimumHeight());
        tv.setCompoundDrawables(null, null, shouqi, null);
        ListView listView = new ListView(context);
        listView.setDivider(null);
        listView.setBackgroundResource(R.drawable.dictionary_background);
        final PopupWindow popupWindow = new PopupWindow(listView, tv.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        DictEntryAdapter adapter;
        if (type == 1) {
            adapter = new DictEntryAdapter(context, mDatas, stageIndex);
        } else {
            adapter = new DictEntryAdapter(context, mDatas, subjectIndex);
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (type == 1) {
                    stageIndex = position;
                    stageId = mDatas.get(position).getTextBookValue();
                } else {
                    subjectIndex = position;
                    subjectId = mDatas.get(position).getTextBookValue();
                }
                popupWindow.dismiss();
                tv.setText(mDatas.get(position).getTextBookName());
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv.setCompoundDrawables(null, null, zhankai, null);
            }
        });
        popupWindow.showAsDropDown(tv);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USER && resultCode == Activity.RESULT_OK && data != null) {
            MobileUser mUser = (MobileUser) data.getSerializableExtra("user");
            tv_lecturer.setText(mUser.getRealName());
            lecturerId = mUser.getId();
        }
    }

    /*选择视频文件*/
    private void picketVideo() {
        MediaOption option = new MediaOption.Builder()
                .setSelectType(MediaOption.TYPE_VIDEO)
                .setShowCamera(true)
                .build();
        MediaPicker.getInstance().init(option).selectMedia(context, new MediaPicker.onSelectMediaCallBack() {
            @Override
            public void onSelected(final String path) {
                if (new File(path).exists()) {
                    if (NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context) && !isOpenMobile) {
                        MaterialDialog dialog = new MaterialDialog(context);
                        dialog.setTitle("网络提醒");
                        dialog.setMessage("使用2G/3G/4G网络上传视频会消耗较多流量。确定要开启吗？");
                        dialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
                            @Override
                            public void onClick(View v, AlertDialog dialog) {
                                isOpenMobile = true;
                                setVideoFile(path);
                            }
                        });
                        dialog.setNegativeButton("取消", null);
                        dialog.show();
                    } else {
                        setVideoFile(path);
                    }
                } else {
                    toast("视频文件不存在");
                }
            }
        });
    }

    private void setVideoFile(String path) {
        File mVideo = new File(path);
        fl_addVideo.setVisibility(View.GONE);
        fl_video.setVisibility(View.VISIBLE);
        Glide.with(context).load(path).centerCrop().into(iv_video);
        tv_videoName.setText(mVideo.getName());
        uploadVideo(mVideo);
    }

    private void uploadVideo(final File mVideo) {
        String url = Constants.OUTRT_NET + "/m/file/uploadTemp";
        mVideoPosable = OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<FileUploadResult>() {
            @Override
            public void onBefore(Request request) {
                isUploading = true;
                if (ll_videoProgress.getVisibility() != View.VISIBLE) {
                    ll_videoProgress.setVisibility(View.VISIBLE);
                }
                if (tv_videoError.getVisibility() != View.GONE) {
                    tv_videoError.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                toast("课堂录像上传失败");
                setVideoError(mVideo);
            }

            @Override
            public void onResponse(FileUploadResult response) {
                isUploading = false;
                handler.removeMessages(CODE_VIDEO);
                if (response != null && response.getResponseData() != null) {
                    mVideoInfo = response.getResponseData();
                    tv_videoProgress.setText("已上传");
                }
            }
        }, mVideo, mVideo.getName(), new OkHttpClientManager.ProgressListener() {
            @Override
            public void onProgress(final long totalBytes, final long remainingBytes, boolean done, File file) {
                Message message = new Message();
                message.what = CODE_VIDEO;
                Bundle bundle = new Bundle();
                bundle.putLong("totalBytes", totalBytes);
                bundle.putLong("remainingBytes", remainingBytes);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        });
    }

    private void setVideoError(final File mVideo) {
        isUploading = false;
        ll_videoProgress.setVisibility(View.GONE);
        tv_videoError.setVisibility(View.VISIBLE);
        if (mVideoPosable != null && !mVideoPosable.isDisposed()) {
            mVideoPosable.dispose();
        }
        handler.removeMessages(CODE_VIDEO);
        tv_videoError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadVideo(mVideo);
            }
        });
    }

    private void deleteVideo() {
        isUploading = false;
        mVideoInfo = null;
        iv_video.setImageResource(0);
        fl_video.setVisibility(View.GONE);
        fl_addVideo.setVisibility(View.VISIBLE);
        if (mVideoPosable != null) {
            mVideoPosable.dispose();
        }
        handler.removeMessages(CODE_VIDEO);
    }

    public interface OnNextListener {
        void onNext(MWorkshopActivity activity);
    }

    public void setOnNextListener(OnNextListener onNextListener) {
        this.onNextListener = onNextListener;
    }

    private boolean checkOut() {
        String title = et_title.getText().toString().trim();
        String content = et_content.getText().toString().trim();
        String textBook = et_textBook.getText().toString().trim();
        if (title.length() == 0) {
            showMaterialDialog("请输入课程主题");
            return false;
        } else if (content.length() == 0) {
            showMaterialDialog("请输入评课方向");
            return false;
        } else if (stageId == null) {
            showMaterialDialog("请选择学段");
            return false;
        } else if (subjectId == null) {
            showMaterialDialog("请选择学科");
            return false;
        } else if (textBook.length() == 0) {
            showMaterialDialog("请填写教材版本");
            return false;
        } else if (isNeedFile && isUploading) {
            showMaterialDialog("请等待课堂录像上传完毕");
            return false;
        } else if (isNeedFile && mVideoInfo == null) {
            showMaterialDialog("请选择上传的课堂录像");
            return false;
        }
        return true;
    }

    private void showMaterialDialog(String message) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage(message);
        dialog.setPositiveButton("确定", null);
        dialog.show();
    }

    private void submitLcec() {
        String title = et_title.getText().toString().trim();
        String content = et_content.getText().toString().trim();
        String bookVersion = et_textBook.getText().toString().trim();
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId() + "/m/activity/wsts";
        Map<String, String> map = new HashMap<>();
        map.put("activity.relation.id", workSectionId);
        map.put("activity.type", "lcec");
        map.put("lcec.coursewareRelations[0].relation.id", workshopId);
        map.put("lcec.title", title);
        map.put("lcec.content", content);
        map.put("lcec.stage", stageId);
        map.put("lcec.subject", subjectId);
        map.put("lcec.textbook", bookVersion);
        map.put("lcec.teacher.id", lecturerId);
        map.put("lcec.type", "onLine");
        if (mVideoInfo != null) {
            map.put("lcec.video.id", mVideoInfo.getId());
            map.put("lcec.video.fileName", mVideoInfo.getFileName());
            map.put("lcec.video.url", mVideoInfo.getUrl());
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<WorkshopActivityResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError();
            }

            @Override
            public void onResponse(WorkshopActivityResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    MWorkshopActivity activity = response.getResponseData();
                    if (onNextListener != null) {
                        onNextListener.onNext(activity);
                    }
                }
            }
        }, map));
    }

    @Override
    public void onDestroyView() {
        if (mVideoPosable != null) {
            mVideoPosable.dispose();
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
}
