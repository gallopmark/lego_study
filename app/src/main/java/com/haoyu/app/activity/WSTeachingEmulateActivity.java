package com.haoyu.app.activity;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
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
                            if (activity == null) {
                                commit(videoName, videoTime);
                            } else {
                                setEmulateTime(videoTime, activity.getId());
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
                } else {
                    videoFile = null;
                    showMaterialDialog("提示", "视频文件不存在");
                }
            }
        });
    }

    private void commit(final String videoName, final String videoTime) {
        String url = Constants.OUTRT_NET + "/m/file/uploadTemp";
        final FileUploadDialog fileUploadDialog = new FileUploadDialog(context, videoFile.getName(), "提交中");
        fileUploadDialog.show();
        Flowable.just(url).map(new Function<String, FileUploadResult>() {
            @Override
            public FileUploadResult apply(String url) throws Exception {
                String resultStr = OkHttpClientManager.post(context, url, videoFile, videoFile.getName(), new OkHttpClientManager.ProgressListener() {
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
                FileUploadResult mResult = new GsonBuilder().create().fromJson(resultStr, FileUploadResult.class);
                return mResult;
            }
        }).map(new Function<FileUploadResult, WorkshopActivityResult>() {
            @Override
            public WorkshopActivityResult apply(FileUploadResult mResult) throws Exception {
                if (mResult != null && mResult.getResponseData() != null) {
                    String fileId = mResult.getResponseData().getId();
                    String fileName = mResult.getResponseData().getFileName();
                    String fileUrl = mResult.getResponseData().getUrl();
                    WorkshopActivityResult result = commitEmulate(videoName, fileId, fileName, fileUrl);
                    return result;
                }
                return null;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WorkshopActivityResult>() {
                    @Override
                    public void accept(WorkshopActivityResult response) throws Exception {
                        fileUploadDialog.dismiss();
                        if (response != null && response.getResponseData() != null) {
                            activity = response.getResponseData();
                            setEmulateTime(videoTime, activity.getId());
                        }
                    }
                });
    }

    //上传文件成功后上传内容
    private WorkshopActivityResult commitEmulate(String videoName, String fileId, String fileName, String fileUrl) throws Exception {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId() + "/m/activity/wsts";
        Map<String, String> map = new HashMap<>();
        map.put("activity.relation.id", workSectionId);
        map.put("activity.type", "video");
        map.put("video.videoRelations[0].relation.id", workshopId);
        map.put("video.title", videoName);
        map.put("video.fileInfos[0].id", fileId);
        map.put("video.fileInfos[0].fileName", fileName);
        map.put("video.fileInfos[0].url", fileUrl);
        String strResult = OkHttpClientManager.postAsString(context, url, map);
        Gson gson = new Gson();
        WorkshopActivityResult result = gson.fromJson(strResult, WorkshopActivityResult.class);
        return result;
    }

    //设置教学观摩指标
    private void setEmulateTime(String time, String id) {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + context.getUserId() + "/m/activity/wsts/" + id;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("activity.attributeMap[view_time].attrValue", time);
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(BaseResponseResult response) {
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    Intent intent = new Intent();
                    intent.putExtra("activity", activity);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }
}
