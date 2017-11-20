package com.haoyu.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.activity.AppMultiImageShowActivity;
import com.haoyu.app.base.BaseFragment;
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

/**
 * 创建日期：2017/11/20.
 * 描述:用户资料信息fragment
 * 作者:xiaoma
 */

public class UserInfoFragment extends BaseFragment {

    private Activity activity;
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
    private MobileUser user;
    private String icoUrl;

    @Override
    public int createView() {
        return R.layout.fragment_userinfo;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
        Bundle bundle = getArguments();
        user = (MobileUser) bundle.getSerializable("user");
    }

    @Override
    public void initView(View view) {
        if (user.getAvatar() == null) {
            icoUrl = getAvatar();
        } else {
            icoUrl = user.getAvatar();
        }
        GlideImgManager.loadCircleImage(activity.getApplicationContext(), icoUrl, R.drawable.user_default,
                R.drawable.user_default, userIco);
        tv_userName.setText(user.getRealName());
        if (user.getmDepartment() != null)
            tv_unit.setText(user.getmDepartment().getDeptName());
        else
            tv_unit.setText(user.getDeptName());
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
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.ll_userIco:
                        pickerPicture();
                        break;
                    case R.id.userIco:
                        if (icoUrl != null && icoUrl.length() > 0) {
                            Intent intent = new Intent(context, AppMultiImageShowActivity.class);
                            ArrayList<String> imgList = new ArrayList<>();
                            imgList.add(icoUrl);
                            intent.putStringArrayListExtra("photos", imgList);
                            intent.putExtra("position", 0);
                            intent.putExtra("isUser", true);
                            startActivity(intent);
                            activity.overridePendingTransition(R.anim.zoom_in, 0);
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
                .setFocusWidth(ScreenUtils.getScreenWidth(activity) / 4 * 3)
                .setFocusHeight(ScreenUtils.getScreenWidth(activity) / 4 * 3)
                .build();
        MediaPicker.getInstance().init(option).selectMedia(activity, new MediaPicker.onSelectMediaCallBack() {
            @Override
            public void onSelected(String path) {
                File mFile = new File(path);
                if (mFile.exists()) {
                    uploadUserIco(mFile);
                } else {
                    toast("您选择的图片路径不存在");
                }
            }
        });
    }

    private void uploadUserIco(final File mFile) {
        String url = Constants.OUTRT_NET + "/m/file/uploadFileInfoRemote";
        final FileUploadDialog uploadDialog = new FileUploadDialog(context, mFile.getName(), "正在上传头像");
        uploadDialog.setCancelable(false);
        uploadDialog.setCanceledOnTouchOutside(false);
        uploadDialog.show();
        final Disposable disposable = Flowable.just(url).map(new Function<String, FileUploadResult>() {
            @Override
            public FileUploadResult apply(String url) throws Exception {
                return commitUserIco(mFile, url, uploadDialog);
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
                uploadDialog.dismiss();
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                } else {
                    toastFullScreen("头像上传失败", false);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                uploadDialog.dismiss();
                toastFullScreen("头像上传失败", false);
            }
        });
        uploadDialog.setCancelListener(new FileUploadDialog.CancelListener() {
            @Override
            public void cancel() {
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

    private void updateUI(MobileUser user) {
        GlideImgManager.loadCircleImage(activity.getApplicationContext(), user.getAvatar(), R.drawable.user_default, R.drawable.user_default, userIco);
        saveUserInfo(user.getAvatar());
        Intent intent = new Intent();
        intent.putExtra("avatar", user.getAvatar());
        activity.setResult(Activity.RESULT_OK, intent);
    }

    private void saveUserInfo(String avatar) {
        SharePreferenceHelper sharePreferenceHelper = new SharePreferenceHelper(context);
        Map<String, Object> map = new HashMap<>();
        map.put("avatar", avatar);
        sharePreferenceHelper.saveSharePreference(map);
    }
}
