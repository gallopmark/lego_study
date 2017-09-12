package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.DictEntryAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.FileUploadDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.DictEntryMobileEntity;
import com.haoyu.app.entity.DictEntryResult;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.WorkShopMobileEntity;
import com.haoyu.app.filePicker.LFilePicker;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

/**
 * 创建日期：2017/5/24 on 15:22
 * 描述:创建个人工作坊
 * 作者:马飞奔 Administrator
 */
public class WorkshopCreateActivity extends BaseActivity implements View.OnClickListener {
    private WorkshopCreateActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;
    @BindView(R.id.add_picture)
    RelativeLayout add_picture;   //添加封面布局
    @BindView(R.id.iv_img)
    ImageView iv_img;     //显示封面
    @BindView(R.id.tv_addPic)
    TextView tv_addPic;    //显示添加封面
    @BindView(R.id.iv_deletePic)
    ImageView iv_deletePic;   //取消封面选择
    @BindView(R.id.et_name)
    EditText et_name;   //工作坊名称
    @BindView(R.id.line_name)
    View line_name;
    @BindView(R.id.et_content)
    EditText et_content;  //工作坊内容
    @BindView(R.id.line_content)
    View line_content;
    @BindView(R.id.tv_subject)
    TextView tv_subject;   //选择学段学科
    @BindView(R.id.tv_selectFile)
    TextView tv_selectFile;   //上传研修方案
    @BindView(R.id.fileContent)
    LinearLayout fileContent;  //显示选择的研修方案
    @BindView(R.id.iv_fileType)
    ImageView iv_fileType;   //文件类型
    @BindView(R.id.tv_fileName)
    TextView tv_fileName;  //文件名称
    @BindView(R.id.iv_delete)
    ImageView iv_delete;  //取消文件选择
    private String trainId;
    private File imgFile, docFile;
    private List<DictEntryMobileEntity> subjectDatas = new ArrayList<>(); // 学科集合
    private List<DictEntryMobileEntity> stageDatas = new ArrayList<>(); // 学段集合
    private boolean isInit;
    private int selectStage = -1, selectSubject = -1;
    private String stageValue, stageName, subjectValue, subjectName;
    private boolean isLastCommit;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_create_workshop;
    }

    @Override
    public void initView() {
        trainId = getIntent().getStringExtra("trainId");
        LinearLayout.LayoutParams imgparms = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.getScreenHeight(context) / 3);
        add_picture.setLayoutParams(imgparms);
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
                commit();
            }
        });
        tv_addPic.setOnClickListener(context);
        iv_deletePic.setOnClickListener(context);
        tv_subject.setOnClickListener(context);
        tv_selectFile.setOnClickListener(context);
        iv_delete.setOnClickListener(context);
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    switch (view.getId()) {
                        case R.id.et_name:
                            scrollView.smoothScrollTo(0, (int) line_name.getY());
                            break;
                        case R.id.et_content:
                            scrollView.smoothScrollTo(0, (int) line_content.getY());
                            break;
                    }
                }
            }
        };
        et_name.setOnFocusChangeListener(onFocusChangeListener);
        et_content.setOnFocusChangeListener(onFocusChangeListener);
        et_content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && v.getParent() != null) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP && v.getParent() != null) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL && v.getParent() != null) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_addPic:
                pickerPicture();
                break;
            case R.id.iv_deletePic:
                imgFile = null;
                iv_img.setVisibility(View.GONE);
                iv_img.setImageResource(0);
                tv_addPic.setVisibility(View.VISIBLE);
                iv_deletePic.setVisibility(View.GONE);
                break;
            case R.id.tv_subject:
                if (isInit) {
                    showSsDialog();
                } else {
                    initStageSubject();
                }
                break;
            case R.id.tv_selectFile:
                openFilePicker();
                break;
            case R.id.iv_delete:
                docFile = null;
                iv_fileType.setImageResource(0);
                tv_selectFile.setVisibility(View.VISIBLE);
                fileContent.setVisibility(View.GONE);
                break;
        }
    }

    private void pickerPicture() {
        MediaOption option = new MediaOption.Builder()
                .setSelectType(MediaOption.TYPE_IMAGE)
                .setShowCamera(true)
                .build();
        MediaPicker.getInstance().init(option).selectMedia(context, new MediaPicker.onSelectMediaCallBack() {
            @Override
            public void onSelected(String path) {
                Glide.with(context).load(path).into(iv_img);
                tv_addPic.setVisibility(View.GONE);
                iv_img.setVisibility(View.VISIBLE);
                iv_deletePic.setVisibility(View.VISIBLE);
                imgFile = new File(path);
            }

        });
    }

    private void openFilePicker() {
        //文件格式必须为doc,docx,wps,pdf
        new LFilePicker()
                .withActivity(context)
                .withRequestCode(1)
                .withMutilyMode(false)
                .withFileFilter(new String[]{"doc", "docx", "wps", "pdf", "pdf"})
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            List<String> list = data.getStringArrayListExtra(RESULT_INFO);
            if (list != null && list.size() > 0 && new File(list.get(0)).exists()) {
                String filePath = list.get(0);
                docFile = new File(filePath);
                tv_selectFile.setVisibility(View.GONE);
                fileContent.setVisibility(View.VISIBLE);
                Common.setFileType(filePath, iv_fileType);
                tv_fileName.setText(Common.getFileName(filePath));
            } else {
                showMaterialDialog("提示", "选择的文件不存在");
            }
        }
    }

    private void initStageSubject() {
        showTipDialog();
        addSubscription(Flowable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return getStageSubject();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) throws Exception {
                        hideTipDialog();
                        isInit = success;
                        if (success) {
                            showSsDialog();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        hideTipDialog();
                        onNetWorkError(context);
                    }
                }));
    }

    private boolean getStageSubject() {
        String url1 = Constants.OUTRT_NET + "/m/textBook?textBookTypeCode=STAGE";
        String url2 = Constants.OUTRT_NET + "/m/textBook?textBookTypeCode=SUBJECT";
        Gson gson = new GsonBuilder().create();
        try {
            String stageStr = OkHttpClientManager.getAsString(context, url1);
            DictEntryResult response = gson.fromJson(stageStr, DictEntryResult.class);
            if (response != null && response.getResponseData() != null) {
                stageDatas.addAll(response.getResponseData());
            }
            String subjectStr = OkHttpClientManager.getAsString(context, url2);
            response = gson.fromJson(subjectStr, DictEntryResult.class);
            if (response != null && response.getResponseData() != null) {
                subjectDatas.addAll(response.getResponseData());
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private void showSsDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_dict_select, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCanceledOnTouchOutside(false);
        View contentView = view.findViewById(R.id.contentView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ScreenUtils.getScreenWidth(context) / 6 * 5,
                ScreenUtils.getScreenHeight(context) / 3 * 2);
        contentView.setLayoutParams(params);
        ListView stageList = view.findViewById(R.id.stageList);
        final ListView subjectList = view.findViewById(R.id.subjectList);
        Button makesure = view.findViewById(R.id.makesure);
        Button cancel = view.findViewById(R.id.cancel);
        final DictEntryAdapter stageAdapter = new DictEntryAdapter(context, stageDatas, selectStage);
        stageList.setAdapter(stageAdapter);
        final DictEntryAdapter subjectAdapter = new DictEntryAdapter(context, subjectDatas, selectSubject);
        subjectList.setAdapter(subjectAdapter);
        stageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectStage = position;
                stageAdapter.setSelectItem(position);
                stageValue = stageDatas.get(position).getTextBookValue();
                stageName = stageDatas.get(position).getTextBookName();
            }
        });
        subjectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectSubject = position;
                subjectAdapter.setSelectItem(position);
                subjectValue = subjectDatas.get(position).getTextBookValue();
                subjectName = subjectDatas.get(position).getTextBookName();
            }
        });
        makesure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_subject.setText(stageName + "\u3000" + subjectName);
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.setContentView(view);
    }

    private void commit() {
        String name = et_name.getText().toString().trim();
        String content = et_content.getText().toString().trim();
        String subject = tv_subject.getText().toString().trim();
        if (imgFile == null) {
            showDialog("请选择工作坊封面");
        } else if (name.length() == 0) {
            showDialog("请输入工作坊名称");
        } else if (content.length() == 0) {
            showDialog("请输入工作坊简介");
        } else if (subject.length() == 0) {
            showDialog("请选择学段学科");
        } else {
            if (isLastCommit) {
                lastCommit();
            } else {
                commitWorkshop();
            }
        }
    }

    private void showDialog(String message) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage(message);
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        dialog.setPositiveButton("我知道了", null);
        dialog.show();
    }

    private FileUploadResult imgResult, docResult;

    private void commitWorkshop() {
        final String url1 = Constants.OUTRT_NET + "/m/file/uploadTemp";
        final FileUploadDialog fileUploadDialog = new FileUploadDialog(context, imgFile.getName(), "提交中");
        fileUploadDialog.show();
        final Disposable subscription = Flowable.just(url1).map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String url) {
                try {
                    String resultStr = OkHttpClientManager.post(context, url1, imgFile, imgFile.getName(), new OkHttpClientManager.ProgressListener() {
                        @Override
                        public void onProgress(long totalBytes, long remainingBytes, boolean done, final File file) {
                            Flowable.just(new Long[]{totalBytes, remainingBytes}).observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<Long[]>() {
                                        @Override
                                        public void accept(Long[] params) throws Exception {
                                            fileUploadDialog.setFileName(imgFile.getName());
                                            fileUploadDialog.setUploadProgressBar(params[0], params[1]);
                                            fileUploadDialog.setUploadText(params[0], params[1]);
                                        }
                                    });
                        }
                    });
                    imgResult = new GsonBuilder().create().fromJson(resultStr, FileUploadResult.class);
                    if (docFile != null) {
                        resultStr = OkHttpClientManager.post(context, url1, docFile, docFile.getName(), new OkHttpClientManager.ProgressListener() {
                            @Override
                            public void onProgress(long totalBytes, long remainingBytes, boolean done, File file) {
                                Flowable.just(new Long[]{totalBytes, remainingBytes}).observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Long[]>() {
                                            @Override
                                            public void accept(Long[] params) throws Exception {
                                                fileUploadDialog.setFileName(docFile.getName());
                                                fileUploadDialog.setUploadProgressBar(params[0], params[1]);
                                                fileUploadDialog.setUploadText(params[0], params[1]);
                                            }
                                        });
                            }
                        });
                        docResult = new GsonBuilder().create().fromJson(resultStr, FileUploadResult.class);
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) throws Exception {
                        fileUploadDialog.dismiss();
                        if (success) {
                            lastCommit();
                        } else {
                            toastFullScreen("上传封面或研修方案失败", false);
                        }
                    }
                });
        fileUploadDialog.setCancelListener(new FileUploadDialog.CancelListener() {
            @Override
            public void cancel() {
                subscription.dispose();
            }
        });
    }

    /**
     * title	工作坊名称	String	Y
     * summary	简介	String	N
     * stage	学段	String	N
     * subject	学科	String	N
     * image.id	封面图片的临时id	String	N
     * image.url	封面图片的临时url	String	N
     * image.fileName	封面图片的临时文件名	String	N
     * workshopRelation.relation.id	培训id	String	Y
     * solutions[0].id	研修方案文件的临时id	String	N
     * solutions[0].url	研修方案文件的临时url	String	N
     * solutions[0].fileName	研修方案文件的临时文件名	String	N
     */
    private void lastCommit() {
        String title = et_name.getText().toString().trim();
        String summary = et_content.getText().toString().trim();
        String url = Constants.OUTRT_NET + "/m/workshop";
        Map<String, String> map = new HashMap<>();
        if (trainId != null) {
            map.put("workshopRelation.relation.id", trainId);
        }
        map.put("title", title);
        map.put("summary", summary);
        map.put("stage", stageValue);
        map.put("subject", subjectValue);
        if (imgResult != null && imgResult.getResponseData() != null) {
            map.put("image.id", imgResult.getResponseData().getId());
            map.put("image.url", imgResult.getResponseData().getUrl());
            map.put("image.fileName", imgResult.getResponseData().getFileName());
        }
        if (docResult != null && docResult.getResponseData() != null) {
            map.put("solutions[0].id", docResult.getResponseData().getId());
            map.put("solutions[0].url", docResult.getResponseData().getUrl());
            map.put("solutions[0].fileName", docResult.getResponseData().getFileName());
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<WorkShopMobileEntity>>() {
            @Override
            public void onBefore(Request request) {
                showLoadingDialog("正在提交");
            }

            @Override
            public void onError(Request request, Exception e) {
                hideLoadingDialog();
                onNetWorkError(context);
                isLastCommit = true;
            }

            @Override
            public void onResponse(BaseResponseResult<WorkShopMobileEntity> response) {
                hideLoadingDialog();
                if (response != null && response.getResponseData() != null) {
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_WORKSHOP;
                    RxBus.getDefault().post(event);
                    Intent intent = new Intent(context, WorkshopHomeActivity.class);
                    intent.putExtra("workshopId", response.getResponseData().getId());
                    intent.putExtra("workshopTitle", response.getResponseData().getTitle());
                    startActivity(intent);
                    finish();
                } else if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("01")) {
                    tipDialog();
                } else {
                    isLastCommit = true;
                    toastFullScreen("创建失败", false);
                }
            }
        }, map));
    }

    private void tipDialog() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.setTitle("提交结果");
        dialog.setMessage("您已经创建过个人工作坊");
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        dialog.setPositiveButton("我知道了", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                finish();
            }
        });
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
    }
}
