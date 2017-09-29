package com.haoyu.app.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.FileUploadDialog;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.MWorkshopActivity;
import com.haoyu.app.entity.WorkshopActivityResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.Request;

/**
 * 创建日期：2017/5/27 on 9:46
 * 描述:工作坊添加教学观摩活动
 * 作者:马飞奔 Administrator
 */
public class WSTeachingEmulateActivity extends BaseActivity implements View.OnClickListener {
    private WSTeachingEmulateActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_name)
    EditText et_name;   //视频名称
    @BindView(R.id.et_time)
    EditText et_time;    //视频观看时间
    @BindView(R.id.rl_videoLayout)
    RelativeLayout rl_videoLayout;   //添加视频布局
    @BindView(R.id.iv_addVideo)
    ImageView iv_addVideo;
    @BindView(R.id.iv_video)
    ImageView iv_video;
    @BindView(R.id.iv_grid)
    ImageView iv_grid;
    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;
    @BindView(R.id.tv_submit)
    TextView tv_submit;
    private String workshopId, workSectionId;
    private File videoFile;
    private FileUploadResult fileResult;
    private MWorkshopActivity activity;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_ws_teaching_emulate;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        workSectionId = getIntent().getStringExtra("workSectionId");
        LinearLayout.LayoutParams imgparms = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.getScreenHeight(context) / 7 * 2);
        rl_videoLayout.setLayoutParams(imgparms);
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        iv_addVideo.setOnClickListener(context);
        iv_cancel.setOnClickListener(context);
        tv_submit.setOnClickListener(context);
        TextWatcher watcher = new TextWatcher() {
            private CharSequence temp;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                temp = charSequence.toString().trim();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (temp != null && temp.equals(editable.toString().trim())) {
                    if (activity != null)
                        activity = null;
                }
            }
        };
        et_name.addTextChangedListener(watcher);
    }

    @Override
    public void onClick(View view) {
        Common.hideSoftInput(context);
        switch (view.getId()) {
            case R.id.iv_addVideo:
                picketVideo();
                break;
            case R.id.iv_cancel:
                iv_video.setVisibility(View.GONE);
                iv_grid.setVisibility(View.GONE);
                iv_addVideo.setVisibility(View.VISIBLE);
                iv_cancel.setVisibility(View.GONE);
                fileResult = null;
                activity = null;
                break;
            case R.id.tv_submit:
                String videoName = et_name.getText().toString().trim();
                String videoTime = et_time.getText().toString().trim();
                if (videoName.length() == 0) {
                    showMaterialDialog("提示", "请输入视频名称");
                } else if (videoTime.length() == 0) {
                    showMaterialDialog("提示", "请输入视频观看时间");
                } else {
                    if (videoFile == null) {
                        showMaterialDialog("提示", "请选择视频文件");
                    } else {
                        if (!videoFile.exists()) {
                            showMaterialDialog("提示", "您选择的视频文件不存在");
                        } else {
                            if (fileResult == null)
                                uploadFile(videoName, videoTime);
                            else {
                                if (activity == null) {
                                    String fileId = fileResult.getResponseData().getId();
                                    String fileName = fileResult.getResponseData().getFileName();
                                    String fileUrl = fileResult.getResponseData().getUrl();
                                    submitEmulate(videoName, fileId, fileName, fileUrl, videoTime);
                                } else {
                                    setEmulateTime(videoTime, activity.getId());
                                }
                            }
                        }
                    }
                }
                break;
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
            public void onSelected(String path) {
                videoFile = new File(path);
                if (videoFile.exists()) {
                    Glide.with(context).load(path).into(iv_video);
                    iv_video.setVisibility(View.VISIBLE);
                    iv_grid.setVisibility(View.VISIBLE);
                    iv_addVideo.setVisibility(View.GONE);
                    iv_cancel.setVisibility(View.VISIBLE);
                    fileResult = null;
                    activity = null;
                } else {
                    videoFile = null;
                    showMaterialDialog("提示", "视频文件不存在");
                }
            }
        });
    }

    private void uploadFile(final String videoName, final String videoTime) {
        String url = Constants.OUTRT_NET + "/m/file/uploadTemp";
        final FileUploadDialog fileUploadDialog = new FileUploadDialog(context, videoFile.getName(), "正在上传视频");
        final Disposable disposable = OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<FileUploadResult>() {
            @Override
            public void onBefore(Request request) {
                fileUploadDialog.show();
            }

            @Override
            public void onError(Request request, Exception e) {
                fileUploadDialog.dismiss();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(FileUploadResult response) {
                fileUploadDialog.dismiss();
                if (response != null && response.getResponseData() != null) {
                    fileResult = response;
                    String fileId = fileResult.getResponseData().getId();
                    String fileName = fileResult.getResponseData().getFileName();
                    String fileUrl = fileResult.getResponseData().getUrl();
                    submitEmulate(videoName, fileId, fileName, fileUrl, videoTime);
                }
            }
        }, videoFile, videoFile.getName(), new OkHttpClientManager.ProgressListener() {
            @Override
            public void onProgress(long totalBytes, long remainingBytes, boolean done, File file) {
                Flowable.just(new Long[]{totalBytes, remainingBytes}).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long[]>() {
                            @Override
                            public void accept(Long[] params) throws Exception {
                                fileUploadDialog.setUploadProgressBar(params[0], params[1]);
                                fileUploadDialog.setUploadText(params[0], params[1]);
                            }
                        });
            }
        });
        fileUploadDialog.setCancelListener(new FileUploadDialog.CancelListener() {
            @Override
            public void cancel() {
                disposable.dispose();
            }
        });
        addSubscription(disposable);
    }

    private void submitEmulate(String videoName, String fileId, String fileName, String fileUrl, final String videoTime) {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId() + "/m/activity/wsts";
        Map<String, String> map = new HashMap<>();
        map.put("activity.relation.id", workSectionId);
        map.put("activity.type", "video");
        map.put("video.videoRelations[0].relation.id", workshopId);
        map.put("video.title", videoName);
        map.put("video.fileInfos[0].id", fileId);
        map.put("video.fileInfos[0].fileName", fileName);
        map.put("video.fileInfos[0].url", fileUrl);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<WorkshopActivityResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(WorkshopActivityResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    activity = response.getResponseData();
                    setEmulateTime(videoTime, activity.getId());
                }
            }
        }, map));
    }

    //设置教学观摩指标
    private void setEmulateTime(String time, String activityId) {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId() + "/m/activity/wsts/" + activityId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("activity.attributeMap[view_time].attrValue", time);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    Intent intent = new Intent();
                    intent.putExtra("activity", activity);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }, map));
    }
}
