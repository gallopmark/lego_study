package com.haoyu.app.activity;

import android.app.AlertDialog;
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
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.TeachingMovementEntity;
import com.haoyu.app.fragment.CmtsMovemenFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
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

/**
 * 创建日期：2017/1/12 on 11:44
 * 描述: 社区活动详情
 * 作者:马飞奔 Administrator
 */
public class CmtsMovementActivity extends BaseActivity {
    private CmtsMovementActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    private TeachingMovementEntity entity;
    private String movementId;
    private CmtsMovemenFragment fragment;
    private File uploadFile;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_currency;
    }

    @Override
    public void initView() {
        String title = getResources().getString(R.string.teach_active_detail);
        String empty_text = getResources().getString(R.string.teach_active_emptylist);
        toolBar.setTitle_text(title);
        toolBar.getIv_rightImage().setImageResource(R.drawable.teaching_research_dot);
        tv_empty.setText(empty_text);
        entity = (TeachingMovementEntity) getIntent().getSerializableExtra("entity");
        movementId = entity.getId();
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/movement/view/" + movementId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<TeachingMovementEntity>>() {
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
            public void onResponse(BaseResponseResult<TeachingMovementEntity> singleResult) {
                loadingView.setVisibility(View.GONE);
                if (singleResult != null && singleResult.getResponseData() != null) {
                    updateUI(singleResult.getResponseData());
                } else {
                    tv_empty.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateUI(TeachingMovementEntity entity) {
        if (entity.getCreator() != null && entity.getCreator().getId() != null && entity.getCreator().getId().equals(getUserId())) {
            toolBar.setShow_right_button(true);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = new CmtsMovemenFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("entity", entity);
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commitAllowingStateLoss();
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
                showBottomDialog();
            }
        });
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
    }


    private void showBottomDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_teaching_at, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        TextView tv_video = view.findViewById(R.id.tv_video);
        TextView tv_photo = view.findViewById(R.id.tv_photo);
        TextView tv_delete = view.findViewById(R.id.tv_delete);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_video:
                        picketVideo();
                        break;
                    case R.id.tv_photo:
                        pickerPicture();
                        break;
                    case R.id.tv_delete:
                        showTipsDialog();
                        break;
                }
                dialog.dismiss();
            }
        };
        tv_video.setOnClickListener(listener);
        tv_photo.setOnClickListener(listener);
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

    private void pickerPicture() {
        MediaOption option = new MediaOption.Builder().setSelectType(MediaOption.TYPE_IMAGE)
                .setShowCamera(true)
                .build();
        MediaPicker.getInstance().init(option).selectMedia(context, onSelectMediaListener);
    }

    private void picketVideo() {
        MediaOption option = new MediaOption.Builder().setSelectType(MediaOption.TYPE_VIDEO)
                .setShowCamera(true)
                .build();
        MediaPicker.getInstance().init(option).selectMedia(context, onSelectMediaListener);
    }


    private MediaPicker.onSelectMediaCallBack onSelectMediaListener = new MediaPicker.onSelectMediaCallBack() {
        @Override
        public void onSelected(String path) {
            uploadFile = new File(path);
            upload();
        }
    };

    private void upload() {
        if (uploadFile != null && uploadFile.exists()) {
            String url = Constants.OUTRT_NET + "/m/file/uploadTemp";
            final FileUploadDialog uploadDialog = new FileUploadDialog(context, uploadFile.getName(), "正在上传");
            uploadDialog.setCancelable(false);
            uploadDialog.setCanceledOnTouchOutside(false);
            uploadDialog.show();
            final Disposable mSubscription = Flowable.just(url).map(new Function<String, FileUploadResult>() {
                @Override
                public FileUploadResult apply(String url) throws Exception {
                    return commitFile(url, uploadDialog);
                }
            }).map(new Function<FileUploadResult, FileUploadDataResult>() {
                @Override
                public FileUploadDataResult apply(FileUploadResult mResult) throws Exception {
                    return commitContent(mResult);
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<FileUploadDataResult>() {
                        @Override
                        public void accept(FileUploadDataResult uploadResult) throws Exception {
                            uploadDialog.dismiss();
                            if (uploadResult != null && uploadResult.getResponseData() != null
                                    && uploadResult.getResponseData().getmFileInfos() != null) {
                                List<MFileInfo> fileInfos = uploadResult.getResponseData().getmFileInfos();
                                if (fragment != null) {
                                    fragment.setFile_infos(fileInfos);
                                    toastFullScreen("上传成功", true);
                                }
                            } else {
                                showErrorDialog();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            toastFullScreen("上传失败", false);
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
    private FileUploadResult commitFile(String url, final FileUploadDialog dialog) throws Exception {
        Gson gson = new GsonBuilder().create();
        String resultStr = OkHttpClientManager.post(context, url, uploadFile, uploadFile.getName(), new OkHttpClientManager.ProgressListener() {
            @Override
            public void onProgress(long totalBytes, long remainingBytes, boolean done, File file) {
                Flowable.just(new long[]{totalBytes, remainingBytes}).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<long[]>() {
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
            String url = Constants.OUTRT_NET + "/m/movement/" + movementId + "/upload";
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
    private void showErrorDialog() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("上传结果");
        dialog.setMessage("由于网络问题上传资源失败，您可以点击重新上传再次上传");
        dialog.setNegativeTextColor(ContextCompat.getColor(context, R.color.gray));
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        dialog.setNegativeButton("取消", null);
        dialog.setPositiveButton("重新上传", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                upload();
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
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("你确定删除吗？");
        dialog.setNegativeButton("确定", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                delete();
            }
        });
        dialog.setPositiveButton("取消", null);
        dialog.show();
    }

    /*删除活动*/
    private void delete() {
        String url = Constants.OUTRT_NET + "/m/movement/" + movementId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
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
                    toastFullScreen("已成功删除，返回首页", true);
                    MessageEvent event = new MessageEvent();
                    event.action = Action.DELETE_MOVEMENT;
                    event.obj = entity;
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    toastFullScreen("删除失败", false);
                }
            }
        }, map));
    }

}
