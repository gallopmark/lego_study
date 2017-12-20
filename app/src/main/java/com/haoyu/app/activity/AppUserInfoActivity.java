package com.haoyu.app.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.dialog.FileUploadDialog;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.UserInfoResult;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.pickerlib.CropImageView;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.SharePreferenceHelper;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
 * 创建日期：2017/2/27 on 13:42
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppUserInfoActivity extends BaseActivity {
    private AppUserInfoActivity context;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;       //返回按钮
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.ll_content)
    LinearLayout ll_content;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.ll_userIco)
    LinearLayout ll_userIco;
    @BindView(R.id.userIco)
    ImageView userIco;
    @BindView(R.id.tv_userName)
    TextView tv_userName;
    @BindView(R.id.tv_address)
    TextView tv_address;
    @BindView(R.id.tv_unit)
    TextView tv_unit;
    @BindView(R.id.tv_subject)
    TextView tv_subject;
    @BindView(R.id.tv_email)
    TextView tv_email;
    @BindView(R.id.tv_phone)
    TextView tv_phone;
    String avatar;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_userinfo;
    }

    @Override
    public void initView() {
        context = this;
        setToolBar();
    }

    private void setToolBar() {
        toolBar.setTitle_text("个人信息");
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/user/" + getUserId();
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<UserInfoResult>() {
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
            public void onResponse(UserInfoResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    ll_content.setVisibility(View.VISIBLE);
                    updateUI(response.getResponseData());
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateUI(MobileUser user) {
        if (user.getAvatar() == null) {
            avatar = getAvatar();
        } else {
            avatar = user.getAvatar();
        }
        GlideImgManager.loadCircleImage(getApplicationContext(), avatar, R.drawable.user_default, R.drawable.user_default, userIco);
        tv_userName.setText(user.getRealName());
        if (user.getmDepartment() != null) {
            tv_unit.setText(user.getmDepartment().getDeptName());
        } else {
            tv_unit.setText(user.getDeptName());
        }
        String address = "";
        if (user.getmDepartment() != null) {
            if (user.getmDepartment().getProvince() != null) {
                address += user.getmDepartment().getProvince() + "\u3000";
            }
            if (user.getmDepartment().getCity() != null) {
                address += user.getmDepartment().getCity() + "\u3000";
            }
            if (user.getmDepartment().getCounties() != null) {
                address += user.getmDepartment().getCounties();
            }
        }
        tv_address.setText(address);
        StringBuilder sb = new StringBuilder();
        if (user.getmStage() != null) {
            sb.append(user.getmStage().getTextBookName());
            sb.append("\u3000\u3000");
        }
        if (user.getmSubject() != null) {
            sb.append(user.getmSubject().getTextBookName());
        }
        tv_subject.setText(sb.toString());
        tv_email.setText(user.getEmail());
        tv_phone.setText(user.getPhone());
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.ll_userIco:
                        pickerPicture();
                        break;
                    case R.id.userIco:
                        if (avatar != null && avatar.length() > 0) {
                            Intent intent = new Intent(context, AppMultiImageShowActivity.class);
                            ArrayList<String> imgList = new ArrayList<>();
                            imgList.add(avatar);
                            intent.putStringArrayListExtra("photos", imgList);
                            intent.putExtra("position", 0);
                            intent.putExtra("isUser", true);
                            startActivity(intent);
                            overridePendingTransition(R.anim.zoom_in, 0);
                        }
                        break;
                }
            }
        };
        ll_userIco.setOnClickListener(listener);
        userIco.setOnClickListener(listener);
    }

    private void pickerPicture() {
        MediaOption option = new MediaOption.Builder()
                .setSelectType(MediaOption.TYPE_IMAGE)
                .isMultiMode(false)
                .setCrop(true)
                .setShowCamera(false)
                .setSaveRectangle(true)
                .setStyle(CropImageView.Style.CIRCLE)
                .setFocusWidth(ScreenUtils.getScreenWidth(context) / 4 * 3)
                .setFocusHeight(ScreenUtils.getScreenWidth(context) / 4 * 3)
                .build();
        MediaPicker.getInstance().init(option).selectMedia(context, new MediaPicker.onSelectMediaCallBack() {
            @Override
            public void onSelected(String path) {
                File mFile = new File(path);
                if (mFile.exists()) {
                    uploadUserIco(mFile);
                } else {
                    toast(context, "您选择的图片路径不存在");
                }
            }
        });
    }

    private void uploadUserIco(final File mFile) {
        String url = Constants.OUTRT_NET + "/m/file/uploadFileInfoRemote";
        final FileUploadDialog dialog = new FileUploadDialog(context, mFile.getName(), "正在上传头像");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        final Disposable disposable = Flowable.just(url).map(new Function<String, FileUploadResult>() {
            @Override
            public FileUploadResult apply(String url) throws Exception {
                return commitUserIco(mFile, url, dialog);
            }
        }).map(new Function<FileUploadResult, UserInfoResult>() {
            @Override
            public UserInfoResult apply(FileUploadResult response) throws Exception {
                if (response != null && response.getResponseData() != null) {
                    String avatar = response.getResponseData().getRelativeUrl();
                    return commit(avatar);
                }
                return null;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<UserInfoResult>() {
            @Override
            public void accept(UserInfoResult response) throws Exception {
                dialog.dismiss();
                if (response != null && response.getResponseData() != null && response.getResponseData().getAvatar() != null) {
                    setAvatar(response.getResponseData().getAvatar());
                } else {
                    toastFullScreen("头像上传失败", false);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                dialog.dismiss();
                toastFullScreen("头像上传失败", false);
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                disposable.dispose();
            }
        });
    }


    /*上传资源到临时文件*/
    private FileUploadResult commitUserIco(File mFile, String url, final FileUploadDialog dialog) throws Exception {
        Gson gson = new GsonBuilder().create();
        String json = OkHttpClientManager.post(context, url, mFile, mFile.getName(), new OkHttpClientManager.ProgressListener() {
            @Override
            public void onProgress(long totalBytes, long remainingBytes, boolean done, File file) {
                addSubscription(Flowable.just(new long[]{totalBytes, remainingBytes}).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<long[]>() {
                            @Override
                            public void accept(long[] params) throws Exception {
                                dialog.setUploadProgressBar(params[0], params[1]);
                                dialog.setUploadText(params[0], params[1]);
                            }
                        }));
            }
        });
        return gson.fromJson(json, FileUploadResult.class);
    }

    private UserInfoResult commit(String avatar) throws Exception {
        String url = Constants.OUTRT_NET + "/m/user/" + getUserId();
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("avatar", avatar);
        Gson gson = new GsonBuilder().create();
        String json = OkHttpClientManager.postAsString(context, url, map);
        return gson.fromJson(json, UserInfoResult.class);
    }

    private void setAvatar(String avatar) {
        context.avatar = avatar;
        GlideImgManager.loadCircleImage(getApplicationContext(), avatar, R.drawable.user_default, R.drawable.user_default, userIco);
        saveUserInfo(avatar);
        Intent intent = new Intent();
        intent.putExtra("avatar", avatar);
        setResult(RESULT_OK, intent);
    }

    private void saveUserInfo(String avatar) {
        SharePreferenceHelper sharePreferenceHelper = new SharePreferenceHelper(context);
        Map<String, Object> map = new HashMap<>();
        map.put("avatar", avatar);
        sharePreferenceHelper.saveSharePreference(map);
    }
}
