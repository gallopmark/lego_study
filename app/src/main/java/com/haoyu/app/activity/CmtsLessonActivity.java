package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.FileUploadDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.FileUploadDataResult;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.TeachingLessonAttribute;
import com.haoyu.app.entity.TeachingLessonData;
import com.haoyu.app.entity.TeachingLessonEntity;
import com.haoyu.app.filePicker.LFilePicker;
import com.haoyu.app.fragment.CmtsLessonFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

import static android.R.attr.id;

/**
 * 创建日期：2017/10/25 on 17:54
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CmtsLessonActivity extends BaseActivity {
    private CmtsLessonActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.empty_detail)
    TextView empty_detail;
    private TeachingLessonEntity lessonEntity;
    private String lessonId;
    private CmtsLessonFragment fragment;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_cmts_detail;
    }

    @Override
    public void initView() {
        String title = getResources().getString(R.string.gen_class_detail);
        String empty_text = getResources().getString(R.string.gen_class_emptyDetail);
        toolBar.setTitle_text(title);
        empty_detail.setText(empty_text);
    }

    @Override
    public void initData() {
        lessonEntity = (TeachingLessonEntity) getIntent().getSerializableExtra("entity");
        lessonId = lessonEntity.getId();
        setSupportToolBar();
        final String url = Constants.OUTRT_NET + "/m/lesson/cmts/view/" + lessonId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<TeachingLessonData>>() {
            @Override
            public void onBefore(Request request) {
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(BaseResponseResult<TeachingLessonData> result) {
                loadingView.setVisibility(View.VISIBLE);
                if (result != null && result.getResponseData() != null) {
                    setSupportFragment(result.getResponseData());
                } else {
                    empty_detail.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void setSupportToolBar() {
        if (lessonEntity.getCreator() != null && lessonEntity.getCreator().getId() != null && lessonEntity.getCreator().getId().equals(getUserId())) {
            toolBar.setShow_right_button(true);
        }
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                showBottomDialog();
            }
        });
    }

    private void showBottomDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_teaching_cc, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        TextView tv_upload = view.findViewById(R.id.tv_upload);
        TextView tv_delete = view.findViewById(R.id.tv_delete);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_upload:
                        openFilePicker();
                        break;
                    case R.id.tv_delete:
                        showTipsDialog();
                        break;
                    case R.id.tv_cancel:
                        break;
                }
                dialog.dismiss();
            }
        };
        tv_upload.setOnClickListener(listener);
        tv_delete.setOnClickListener(listener);
        tv_cancel.setOnClickListener(listener);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(ScreenUtils.getScreenWidth(context), LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setWindowAnimations(R.style.dialog_anim);
        window.setContentView(view);
        window.setGravity(Gravity.BOTTOM);
    }

    private void openFilePicker() {
        new LFilePicker()
                .withActivity(context)
                .withRequestCode(1)
                .withMutilyMode(false)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            List<String> list = data.getStringArrayListExtra(RESULT_INFO);
            if (list != null && list.size() > 0) {
                String filePath = list.get(0);
                File file = new File(filePath);
                uploadFile(file);
            }
        }
    }

    private void uploadFile(final File file) {
        if (file != null && file.exists()) {
            String url = Constants.OUTRT_NET + "/m/file/uploadTemp";
            final FileUploadDialog uploadDialog = new FileUploadDialog(context, file.getName(), "正在上传");
            uploadDialog.setCancelable(false);
            uploadDialog.setCanceledOnTouchOutside(false);
            uploadDialog.show();
            final Disposable mSubscription = Flowable.just(url).map(new Function<String, FileUploadResult>() {
                @Override
                public FileUploadResult apply(String url) throws Exception {
                    return commitFile(url, uploadDialog, file);
                }
            }).map(new Function<FileUploadResult, FileUploadDataResult>() {
                @Override
                public FileUploadDataResult apply(FileUploadResult mResult) throws Exception {
                    return commitContent(mResult);
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<FileUploadDataResult>() {
                        @Override
                        public void accept(FileUploadDataResult response) throws Exception {
                            uploadDialog.dismiss();
                            if (response != null && response.getResponseCode() != null &&
                                    response.getResponseCode().equals("00")) {
                                MessageEvent event = new MessageEvent();
                                event.action = "fileUpload";
                                RxBus.getDefault().post(event);
                            } else {
                                showErrorDialog(file);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            uploadDialog.dismiss();
                        }
                    });
            uploadDialog.setCancelListener(new FileUploadDialog.CancelListener() {
                @Override
                public void cancel() {
                    showCancelDialog(mSubscription, uploadDialog);
                }
            });
        } else {
            showMaterialDialog("提示", "上传的文件不存在，请重新选择文件");
        }
    }

    /*上传资源到临时文件*/
    private FileUploadResult commitFile(String url, final FileUploadDialog dialog, File file) throws Exception {
        Gson gson = new GsonBuilder().create();
        String resultStr = OkHttpClientManager.post(context, url, file, file.getName(), new OkHttpClientManager.ProgressListener() {
            @Override
            public void onProgress(long totalBytes, long remainingBytes, boolean done, File file) {
                Flowable.just(new long[]{totalBytes, remainingBytes}).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<long[]>() {
                            @Override
                            public void accept(long[] params) throws Exception {
                                dialog.setUploadProgressBar(params[0], params[1]);
                                dialog.setUploadText(params[0], params[1]);
                            }
                        });
            }
        });
        FileUploadResult mResult = gson.fromJson(resultStr, FileUploadResult.class);
        return mResult;
    }

    /*拿到上传临时文件返回的结果再次提交到创课表*/
    private FileUploadDataResult commitContent(FileUploadResult mResult) throws Exception {
        if (mResult != null && mResult.getResponseData() != null) {
            String url = Constants.OUTRT_NET + "/m/lesson/cmts/" + id + "/upload";
            Gson gson = new GsonBuilder().create();
            Map<String, String> map = new HashMap<>();
            map.put("fileInfos[0].id", mResult.getResponseData().getId());
            map.put("fileInfos[0].url", mResult.getResponseData().getUrl());
            map.put("fileInfos[0].fileName", mResult.getResponseData().getFileName());
            String responseStr = OkHttpClientManager.postAsString(context, url, map);
            FileUploadDataResult uploadResult = gson.fromJson(responseStr, FileUploadDataResult.class);
            return uploadResult;
        }
        return null;
    }

    /*上传失败显示dialog*/
    private void showErrorDialog(final File file) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("上传结果");
        dialog.setMessage("由于网络问题上传资源失败，您可以点击重新上传再次上传");
        dialog.setNegativeTextColor(ContextCompat.getColor(context, R.color.gray));
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        dialog.setNegativeButton("取消", null);
        dialog.setPositiveButton("重新上传", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                uploadFile(file);
            }
        });
        dialog.show();
    }

    /*取消上传显示dialog*/
    private void showCancelDialog(final Disposable mSubscription, final FileUploadDialog uploadDialog) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("你确定取消本次上传吗？");
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        dialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                dialog.dismiss();
                mSubscription.dispose();
            }
        });
        dialog.setNegativeButton("关闭", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                dialog.dismiss();
                if (uploadDialog != null && !uploadDialog.isShowing()) {
                    uploadDialog.show();
                }
            }
        });
        dialog.show();
    }

    private void showTipsDialog() {
        MaterialDialog materialDialog = new MaterialDialog(context);
        materialDialog.setTitle("提示");
        materialDialog.setMessage("你确定删除吗？");
        materialDialog.setNegativeButton("确定", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                deleteCc();
            }
        });
        materialDialog.setPositiveButton("取消", null);
        materialDialog.show();
    }

    /*删除创课*/
    private void deleteCc() {
        String url = Constants.OUTRT_NET + "/m/lesson/cmts/" + lessonId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                onNetWorkError(context);
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    MessageEvent event = new MessageEvent();
                    event.action = Action.DELETE_GEN_CLASS;
                    event.obj = lessonEntity;
                    finish();
                } else {
                    toast(context, "删除失败");
                }
            }
        }, map));
    }

    private void setSupportFragment(TeachingLessonData responseData) {
        TeachingLessonEntity mLesson = responseData.getmLesson();
        if (mLesson == null) {
            empty_detail.setVisibility(View.VISIBLE);
            return;
        }
        TeachingLessonAttribute attribute = responseData.getmLessonAttribute();
        fragment = new CmtsLessonFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("entity", lessonEntity);
        bundle.putSerializable("mLesson", mLesson);
        bundle.putSerializable("attribute", attribute);
        fragment.setArguments(bundle);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, fragment).commit();
    }
}
