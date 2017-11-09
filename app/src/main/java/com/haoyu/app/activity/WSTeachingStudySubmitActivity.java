package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
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
import com.haoyu.app.dialog.DateTimePickerDialog;
import com.haoyu.app.dialog.FileUploadDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.MWorkshopActivity;
import com.haoyu.app.entity.WorkshopActivityResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

/**
 * 创建日期：2017/5/27 on 10:34
 * 描述:工作坊听课评课活动
 * 作者:马飞奔 Administrator
 */
public class WSTeachingStudySubmitActivity extends BaseActivity implements View.OnClickListener {
    private WSTeachingStudySubmitActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.ll_addScore)
    LinearLayout ll_addScore;  //添加评分项
    @BindView(R.id.ll_parent)
    LinearLayout ll_parent;
    @BindView(R.id.ll_start_time)
    LinearLayout ll_start_time;
    @BindView(R.id.tv_start)
    TextView tv_start;
    @BindView(R.id.ll_end_time)
    LinearLayout ll_end_time;
    @BindView(R.id.tv_end)
    TextView tv_end;
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
    private String workshopId, workSectionId;
    private String title;
    private String content;
    private String bookVersion;//教程版本
    private String subjectId;//学科
    private String stageId;//学段
    private String lecturerId;//授课人Id
    private boolean needFile;
    private String scoreContent;
    private File videoFile;
    private FileUploadResult fileResult;
    private MWorkshopActivity activity;
    private int nowYear, nowMonth, nowDay;
    private int startYear, endYear, startDay;
    private int startMonth, endMonth, endDay;
    private String startTime, endTime;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_ws_teaching_study;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        workSectionId = getIntent().getStringExtra("workSectionId");
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        bookVersion = getIntent().getStringExtra("bookversion");
        lecturerId = getIntent().getStringExtra("lectureId");
        stageId = getIntent().getStringExtra("stageId");
        subjectId = getIntent().getStringExtra("subjectId");
        needFile = getIntent().getBooleanExtra("needFile", false);
        if (needFile) {
            rl_videoLayout.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams imgparms = new LinearLayout
                    .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.getScreenHeight(context) / 3);
            rl_videoLayout.setLayoutParams(imgparms);
        } else {
            rl_videoLayout.setVisibility(View.GONE);
        }
        Calendar c = Calendar.getInstance();
        nowYear = c.get(Calendar.YEAR);
        nowMonth = c.get(Calendar.MONTH) + 1;
        nowDay = c.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public void setListener() {
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                String startTime = tv_start.getText().toString().trim();
                String endTime = tv_end.getText().toString().trim();
                if (maps.size() == 0) {
                    showMaterialDialog("提示", "至少填写一项评分项");
                } else if (startTime.length() == 0) {
                    showMaterialDialog("提示", "请设置开始时间");
                } else if (endTime.length() == 0) {
                    showMaterialDialog("提示", "请设置结束");
                } else {
                    if (needFile) {
                        if (videoFile == null || !videoFile.exists()) {
                            showMaterialDialog("提示", "请选择视频文件");
                        } else {
                            if (fileResult == null)
                                uploadFile();
                            else {
                                if (activity == null) {
                                    String fileId = fileResult.getResponseData().getId();
                                    String fileName = fileResult.getResponseData().getFileName();
                                    String fileUrl = fileResult.getResponseData().getUrl();
                                    submitLcec(fileId, fileName, fileUrl);
                                } else {
                                    lastSubmit(activity.getId());
                                }
                            }
                        }
                    } else {
                        if (activity == null)
                            submitLcec();
                        else
                            lastSubmit(activity.getId());
                    }
                }
            }
        });
        ll_addScore.setOnClickListener(context);
        ll_start_time.setOnClickListener(context);
        ll_end_time.setOnClickListener(context);
        iv_addVideo.setOnClickListener(context);
        iv_cancel.setOnClickListener(context);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_addScore:
                addView();
                break;
            case R.id.ll_start_time:
                setStartTime(tv_start);
                break;
            case R.id.ll_end_time:
                setEndTime(tv_end);
                break;
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
        }
    }


    private Map<Integer, String> maps = new HashMap<>();

    private void addView() {
        final View view = getLayoutInflater().inflate(R.layout.ws_teaching_study_score_item, null);
        final EditText et_score = getView(view, R.id.et_score);
        ImageView iv_delete = getView(view, R.id.iv_delete);
        et_score.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int index = ll_parent.indexOfChild(view);
                scoreContent = editable.toString();
                maps.put(index, scoreContent);
            }
        });
        ll_parent.addView(view);
        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = ll_parent.indexOfChild(view);
                if (maps.containsKey(index)) {
                    maps.remove(index);
                }
                ll_parent.removeView(view);
            }
        });
    }

    private void setStartTime(final TextView tv_start) {
        DateTimePickerDialog pickerDialog = new DateTimePickerDialog(context);
        pickerDialog.setTitle("选择开始时间");
        pickerDialog.setDateListener(new DateTimePickerDialog.DateListener() {
            @Override
            public void Date(int year, int month, int day) {
                startYear = year;
                startMonth = month;
                startDay = day;
                if (month < 10) {
                    startTime = year + "-0" + month;
                } else {
                    startTime = year + "-" + month;
                }
                if (day < 10) {
                    startTime += "-0" + startDay;
                } else {
                    startTime += "-" + startDay;
                }
                if (chckDate()) {
                    tv_start.setText(startTime);
                }
            }

            @Override
            public void Time(int hour, int minute) {

            }
        });
        pickerDialog.show();
    }

    private void setEndTime(final TextView tv_end) {
        DateTimePickerDialog pickerDialog = new DateTimePickerDialog(context);
        pickerDialog.setTitle("选择结束时间");
        pickerDialog.setDateListener(new DateTimePickerDialog.DateListener() {
            @Override
            public void Date(int year, int month, int day) {
                endYear = year;
                endMonth = month;
                endDay = day;
                if (chckDate()) {
                    setEndTime(tv_end, endYear, endMonth, endDay);
                }
            }

            @Override
            public void Time(int hour, int minute) {

            }
        });
        pickerDialog.show();
    }

    private void setEndTime(TextView tv_end, int year, int month, int day) {
        if (month < 10) {
            endTime = year + "-0" + month;
        } else {
            endTime = year + "-" + month;
        }
        if (day < 10) {
            endTime += "-0" + day;
        } else {
            endTime += "-" + day;
        }
        if (chckDate()) {
            tv_end.setText(endTime);
        }

    }

    private boolean chckDate() {
        if ((startYear != 0 && endYear != 0) && (endYear < nowYear ||
                (endYear == nowYear && endMonth < nowMonth) || (endYear == nowYear && endMonth == nowMonth && endDay < nowDay) || endYear < startYear || (startYear == endYear && endMonth < startMonth) || (startYear == endYear && endMonth == startMonth && endDay < startDay))) {
            toastMsg("<p>您选择的时间不符合要求，请重新选择！<br />" +
                    "时间选择要求：<br />1.结束年份不可以小于开始年份；<br />" +
                    "2.结束年份不可以小于当前年份；<br />" +
                    "3.当选择的年份相同时，选择的结束月份不可以小于开始月份。</p>");
            return false;
        } else {
            return true;
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

    private void uploadFile() {
        String url = Constants.OUTRT_NET + "/m/file/uploadTemp";
        final FileUploadDialog fileUploadDialog = new FileUploadDialog(context, videoFile.getName(), "提交中");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<FileUploadResult>() {
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
                    submitLcec(fileId, fileName, fileUrl);
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
        }));
    }

    private void submitLcec(String fileId, String fileName, String fileUrl) {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + context.getUserId() + "/m/activity/wsts";
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
        map.put("lcec.video.id", fileId);
        map.put("lcec.video.fileName", fileName);
        map.put("lcec.video.url", fileUrl);
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
                if (response != null && response.getResponseData() != null) {
                    activity = response.getResponseData();
                    lastSubmit(activity.getId());
                }
            }
        }, map));
    }

    private void lastSubmit(final String activityId) {
        showTipDialog();
        addSubscription(Flowable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return submitTime(startTime, endTime, activityId) && submitEvaluate(activityId);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean success) throws Exception {
                hideTipDialog();
                if (success) {
                    Intent intent = new Intent();
                    intent.putExtra("activity", activity);
                    setResult(RESULT_OK, intent);
                    finish();
                } else
                    toastFullScreen("提交失败", false);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                hideTipDialog();
                toastFullScreen("提交失败", false);
            }
        }));
    }

    //提交时间
    private boolean submitTime(String startTime, final String endTime, final String activityId) {
        String uid = getUserId();
        //活动时间
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + uid + "/m/activity/wsts/" + activityId;
        Map<String, String> map = new HashMap<>();
        map.put("activity.startTime", startTime);
        map.put("activity.endTime", endTime);
        map.put("_method", "put");
        try {
            String json = OkHttpClientManager.postAsString(context, url, map);
            BaseResponseResult result = new Gson().fromJson(json, BaseResponseResult.class);
            if (result != null && result.getResponseCode() != null && result.getResponseCode().equals("00"))
                return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean submitEvaluate(final String activityId) {
        int count = 0;
        for (String content : maps.values()) {
            if (evaluateContent(activityId, content))
                count++;
        }
        if (count == maps.size())
            return true;
        else
            return false;
    }

    //评分选项
    private boolean evaluateContent(String activityId, String content) {
        try {
            String url = Constants.OUTRT_NET + "/master_/" + workshopId + "/unique_uid_" + context.getUserId() + "/m/evaluate_item";
            Map<String, String> map = new HashMap<>();
            map.put("aid", activityId);
            map.put("content", content);
            String json = OkHttpClientManager.postAsString(context, url, map);
            BaseResponseResult result = new GsonBuilder().create().fromJson(json, BaseResponseResult.class);
            if (result != null && result.getResponseCode() != null && result.getResponseCode().equals("00"))
                return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    //无视频时上传
    private void submitLcec() {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + context.getUserId() + "/m/activity/wsts";
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
        map.put("lcec.type", "offLine");
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
                if (response != null && response.getResponseData() != null) {
                    activity = response.getResponseData();
                    lastSubmit(activity.getId());
                }
            }
        }, map));
    }

    private void toastMsg(String msg) {
        Spanned spanned = Html.fromHtml(msg);
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage(spanned);
        dialog.setPositiveButton("重新选择", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                dialog.dismiss();

            }
        });
        dialog.setNegativeButton("取消选择", null);
        dialog.show();
    }
}
