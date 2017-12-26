package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.dialog.FileUploadDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.filePicker.FileUtils;
import com.haoyu.app.filePicker.LFilePicker;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.swipe.OnActivityTouchListener;
import com.haoyu.app.swipe.RecyclerTouchListener;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
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
import io.reactivex.schedulers.Schedulers;


/**
 * Created by acer1 on 2017/2/13.
 * 学员提交作业
 */
public class AppSubmitAssignmentActivity extends BaseActivity implements RecyclerTouchListener.RecyclerTouchListenerHelper, View.OnClickListener {
    private AppSubmitAssignmentActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    private String aid;
    private String auid;
    private String uid;
    private FileAdapter adapter;
    //要上传的文件对象
    private List<File> files = new ArrayList<>();
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.iv_add)
    ImageView iv_add;
    private String[] types;
    private String fileType;
    private RecyclerTouchListener onTouchListener;
    private OnActivityTouchListener touchListener;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_submit_assignment;
    }

    @Override
    public void initView() {
        aid = getIntent().getStringExtra("aid");
        auid = getIntent().getStringExtra("auid");
        uid = getIntent().getStringExtra("uid");
        fileType = getIntent().getStringExtra("fileType");
        types = fileType.split(",");
        adapter = new FileAdapter(files);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        onTouchListener = new RecyclerTouchListener(context, recyclerView);
        recyclerView.addOnItemTouchListener(onTouchListener);
    }

    @Override
    public void setListener() {
        iv_add.setOnClickListener(context);
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                if (files.size() > 0) {
                    commitAssignment();
                } else {
                    toast(context, "请选择提交的作业");
                }
            }
        });
        onTouchListener.setSwipeOptionViews(R.id.tv_delete).setSwipeable(R.id.ll_rowFG, R.id.tv_delete, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
            @Override
            public void onSwipeOptionClicked(int viewID, int position) {
                files.remove(position);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void addFilePath(List<String> filePaths) {
        for (int i = 0; i < filePaths.size(); i++) {
            String filePath = filePaths.get(i);
            if (new File(filePath).exists()) {
                files.add(new File(filePath));
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add:
                openFilePicker();
                break;
        }
    }

    private void openFilePicker() {
        new LFilePicker()
                .withActivity(context)
                .withRequestCode(1)
                .withMutilyMode(true)
                .withFileFilter(types)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            List<String> list = data.getStringArrayListExtra(RESULT_INFO);
            if (list != null && list.size() > 0) {
                addFilePath(list);
            }
        }
    }

    private void commitAssignment() {
        final FileUploadDialog uploadDialog = new FileUploadDialog(context, "", "提交中");
        uploadDialog.setCancelable(false);
        uploadDialog.setCanceledOnTouchOutside(false);
        uploadDialog.show();
        final Disposable disposable = Flowable.fromCallable(new Callable<ArrayMap<Integer, Boolean>>() {
            @Override
            public ArrayMap<Integer, Boolean> call() throws Exception {
                return getUploadResult(uploadDialog);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ArrayMap<Integer, Boolean>>() {
            @Override
            public void accept(ArrayMap<Integer, Boolean> resultMap) throws Exception {
                uploadDialog.dismiss();
                int failNum = 0;
                for (Integer index : resultMap.keySet()) {
                    if (resultMap.get(index) != null && resultMap.get(index) == false) {
                        failNum++;
                    }
                }
                if (failNum < resultMap.size()) {
                    if (failNum == 0) {
                        toastFullScreen("提交成功", true);
                    } else {
                        toastFullScreen((resultMap.size() - failNum) + "份提交成功，" + failNum + "份提交失败", false);
                    }
                    MessageEvent event = new MessageEvent();
                    event.setAction(Action.SUBMIT_COURSE_ASSIGNMENT);
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    toastFullScreen("提交失败", false);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                uploadDialog.dismiss();
                toastFullScreen("提交失败", false);
            }
        });
        uploadDialog.setCancelListener(new FileUploadDialog.CancelListener() {
            @Override
            public void cancel() {
                tipDialog(uploadDialog, disposable);
            }
        });
    }

    private ArrayMap<Integer, Boolean> getUploadResult(final FileUploadDialog uploadDialog) {
        ArrayMap<Integer, Boolean> resultMap = new ArrayMap<>();
        String url1 = Constants.OUTRT_NET + "/m/file/uploadTemp";
        String url2 = Constants.OUTRT_NET + "/" + aid + "/study/unique_uid_" + uid + "/m/assignment/user/" + auid;
        Gson gson = new Gson();
        for (int i = 0; i < files.size(); i++) {
            try {
                File file = files.get(i);
                String strREsult = OkHttpClientManager.post(context, url1, file, file.getName(), new OkHttpClientManager.ProgressListener() {
                    @Override
                    public void onProgress(final long totalBytes, final long remainingBytes, boolean done, final File file) {
                        Flowable.just(this).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<OkHttpClientManager.ProgressListener>() {
                            @Override
                            public void accept(OkHttpClientManager.ProgressListener progressListener) throws Exception {
                                uploadDialog.setUploadProgressBar(totalBytes, remainingBytes);
                                uploadDialog.setUploadText(totalBytes, remainingBytes);
                                uploadDialog.setFileName(file.getName());
                            }
                        });
                    }
                });
                FileUploadResult fileUploadResult = gson.fromJson(strREsult, FileUploadResult.class);
                if (fileUploadResult != null && fileUploadResult.getResponseData() != null) {
                    Map<String, String> map = new HashMap<>();
                    map.put("state", "commit");
                    map.put("_method", "put");
                    map.put("fileInfos[" + i + "].id", fileUploadResult.getResponseData().getId());
                    map.put("fileInfos[" + i + "].url", fileUploadResult.getResponseData().getUrl());
                    map.put("fileInfos[" + i + "].fileName", fileUploadResult.getResponseData().getFileName());
                    String json = OkHttpClientManager.postAsString(context, url2, map);
                    BaseResponseResult result = gson.fromJson(json, BaseResponseResult.class);
                    if (result != null && result.getResponseCode() != null && result.getResponseCode().equals("00")) {
                        resultMap.put(i, true);
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        return resultMap;
    }

    private void tipDialog(final FileUploadDialog uploadDialog, final Disposable disposable) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("您确定取消上传吗？");
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.gray));
        dialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                dialog.dismiss();
                disposable.dispose();
            }
        });
        dialog.setNegativeButton("取消", new MaterialDialog.ButtonClickListener() {
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

    @Override
    public void setOnActivityTouchListener(OnActivityTouchListener listener) {
        touchListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (touchListener != null) touchListener.getTouchCoordinates(ev);
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    private class FileAdapter extends BaseArrayRecyclerAdapter<File> {

        public FileAdapter(List<File> mDatas) {
            super(mDatas);
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.file_item;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, File file, int position) {
            ImageView iv_fileType = holder.obtainView(R.id.iv_fileType);
            TextView tv_mFileName = holder.obtainView(R.id.tv_mFileName);
            TextView tv_mFileSize = holder.obtainView(R.id.tv_mFileSize);
            Common.setFileType(file.getAbsolutePath(), iv_fileType);
            tv_mFileName.setText(file.getName());
            tv_mFileSize.setText(FileUtils.getReadableFileSize(file.length()));
        }
    }
}
