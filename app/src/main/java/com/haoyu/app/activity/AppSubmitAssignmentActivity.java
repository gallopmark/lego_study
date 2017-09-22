package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;


import com.google.gson.Gson;
import com.haoyu.app.adapter.FileSubmitAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.FileUploadDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.filePicker.LFilePicker;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
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
public class AppSubmitAssignmentActivity extends BaseActivity implements View.OnClickListener {
    private AppSubmitAssignmentActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    private String aid;
    private String auid;
    private String uid;
    private FileSubmitAdapter adapter;
    //要上传的文件对象
    private List<MFileInfo> filePathList = new ArrayList<>();
    private List<String> pathList = new ArrayList<>();
    @BindView(R.id.fileList)
    RecyclerView recyclerView;
    @BindView(R.id.iv_add)
    ImageView iv_add;
    private String[] types;
    private String fileType;

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
        adapter = new FileSubmitAdapter(filePathList);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void setListener() {
        iv_add.setOnClickListener(context);

        adapter.setDisposeCallBack(new FileSubmitAdapter.onDisposeCallBack() {
            @Override
            public void onDelete(int position) {
                filePathList.remove(position);
                pathList.remove(position);
                adapter.notifyDataSetChanged();
            }
        });

        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        toolBar.setOnRightClickListener(new AppToolBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                if (filePathList.size() > 0) {
                    commitAssignment();
                } else {
                    toast(context, "请选择上传的文件");
                }

            }
        });

    }

    private void addFilePath(List<String> filePaths) {
        for (int i = 0; i < filePaths.size(); i++) {
            String filePath = filePaths.get(i);
            boolean flag = false;
            for (int j = 0; j < types.length; j++) {
                if (filePath.endsWith(types[j])) {
                    flag = true;
                    break;
                }
            }
            if (flag && !pathList.contains(filePath)) {
                MFileInfo entity = new MFileInfo();
                File file = new File(filePath);
                entity.setFileSize(file.length());
                entity.setFileName(file.getName());
                entity.setUrl(filePath);
                pathList.add(filePath);
                filePathList.add(entity);
                adapter.notifyDataSetChanged();
            }
        }
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
        for (int i = 0; i < pathList.size(); i++) {
            try {
                File file = new File(pathList.get(i));
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

}
