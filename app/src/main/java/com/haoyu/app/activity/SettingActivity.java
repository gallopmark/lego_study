package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.LegoApplication;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.VersionEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.service.VersionUpdateService;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.FileCacheUtils;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.SharePreferenceHelper;
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
 * 设置界面(清除缓存,意见反馈，去AppStore评分，关于我们，退出登录)
 *
 * @author xiaoma
 */
public class SettingActivity extends BaseActivity implements OnClickListener {
    private SettingActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar; // 返回
    @BindView(R.id.ll_clear_the_cache)
    LinearLayout ll_clear_the_cache; // 清除缓存
    @BindView(R.id.ll_clear_the_download)
    LinearLayout ll_clear_the_download; //清除离线下载内容
    @BindView(R.id.tv_cacheSize)
    TextView tv_cacheSize; // 缓存大小
    @BindView(R.id.tv_cacheDownloadSize)
    TextView tv_cacheDownloadSize;  //离线文件大小
    @BindView(R.id.tv_feedback)
    TextView tv_feedback; // 意见反馈
    @BindView(R.id.tv_version)
    TextView tv_version;//版本检测
    @BindView(R.id.tv_about_us)
    TextView tv_about_us; // 关于我们
    @BindView(R.id.bt_logout)
    Button bt_logout; // 退出登录
    private File fileCache, courseWare, videoCache, compressorCache;
    private long cacheSize, offLineSize;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_setting;
    }

    @Override
    public void initView() {
        cacheSize = FileCacheUtils.getFolderSize(getExternalCacheDir());
        fileCache = new File(Constants.fileDownDir);
        courseWare = new File(Constants.coursewareDir);
        compressorCache = new File(Constants.compressor);
        if (fileCache.exists())
            cacheSize += FileCacheUtils.getFolderSize(fileCache);
        if (courseWare.exists())
            cacheSize += FileCacheUtils.getFolderSize(courseWare);
        if (compressorCache.exists())
            cacheSize += FileCacheUtils.getFolderSize(compressorCache);
        if (cacheSize > 0)
            tv_cacheSize.setText(FileCacheUtils.getFormatSize(cacheSize));
        videoCache = new File(Constants.videoCache);
        if (videoCache.exists())
            offLineSize = FileCacheUtils.getFolderSize(new File(Constants.videoCache));
        if (offLineSize > 0)
            tv_cacheDownloadSize.setText(FileCacheUtils.getFormatSize(offLineSize));
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        ll_clear_the_cache.setOnClickListener(context);
        ll_clear_the_download.setOnClickListener(context);
        tv_feedback.setOnClickListener(context);
        tv_about_us.setOnClickListener(context);
        bt_logout.setOnClickListener(context);
        tv_version.setOnClickListener(context);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_clear_the_cache:
                if (cacheSize > 0)
                    dialogTip();
                else
                    toast(context, "无缓存文件");
                break;
            case R.id.ll_clear_the_download:
                if (offLineSize > 0)
                    deleteVideoCache();
                else
                    toast(context, "无离线下载内容");
                break;
            case R.id.tv_feedback:
                startActivity(new Intent(context, FeedbackActivity.class));
                return;
            case R.id.tv_about_us:
                startActivity(new Intent(context, AboutUsActivity.class));
                break;
            case R.id.bt_logout:
                LegoApplication.getInstance().exit();
                SharePreferenceHelper helper = new SharePreferenceHelper(context);
                Map<String, Object> map = new HashMap<>();
                map.put("firstLogin", true);
                helper.saveSharePreference(map);
                startActivity(new Intent(context, LoginActivity.class));
                break;
            case R.id.tv_version:
                getVersion();
                break;
        }
    }

    private void dialogTip() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("清空本地缓存文件？");
        dialog.setPositiveButton("删除", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                showTipDialog();
                File[] files = new File[]{getExternalCacheDir(), fileCache, courseWare};
                Flowable.just(files).map(new Function<File[], Boolean>() {
                    @Override
                    public Boolean apply(File[] files) throws Exception {
                        for (File file : files) {
                            if (file != null && file.exists() && file.isDirectory()) {
                                for (File item : file.listFiles()) {
                                    item.delete();
                                }
                            }
                        }
                        return true;
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) throws Exception {
                        hideTipDialog();
                        cacheSize = 0;
                        tv_cacheSize.setText(null);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        hideTipDialog();
                    }
                });
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    private void deleteVideoCache() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("离线下载的文件会被清空，您确定要删除吗？");
        dialog.setPositiveButton("删除", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                showTipDialog();
                Flowable.just(videoCache).map(new Function<File, Boolean>() {
                    @Override
                    public Boolean apply(File directory) throws Exception {
                        if (directory != null && directory.exists() && directory.isDirectory()) {
                            for (File item : directory.listFiles()) {
                                item.delete();
                            }
                        }
                        return true;
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) throws Exception {
                        hideTipDialog();
                        if (success) {
                            offLineSize = 0;
                            tv_cacheDownloadSize.setText(null);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        hideTipDialog();
                    }
                });
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    private void getVersion() {
        addSubscription(OkHttpClientManager.getAsyn(context, Constants.updateUrl, new OkHttpClientManager.ResultCallback<VersionEntity>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(VersionEntity entity) {
                if (entity.getVersionCode() > Common.getVersionCode(context)) {
                    updateTips(entity);
                } else {
                    toast(context, "已经是最新版本啦");
                }
            }
        }));
    }

    private void updateTips(final VersionEntity entity) {
        final MaterialDialog dialog = new MaterialDialog(context);
        dialog.setMessage(entity.getUpdateLog());
        dialog.setTitle("发现新版本");
        dialog.setNegativeButton("稍后下载", null);
        dialog.setPositiveButton("立即下载", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                startService(entity);
            }
        });
        dialog.show();
    }

    private void startService(VersionEntity entity) {
        Intent intent = new Intent(context, VersionUpdateService.class);
        intent.putExtra("url", entity.getDownurl());
        intent.putExtra("versionName", entity.getVersionName());
        startService(intent);
        if (!Common.isNotificationEnabled(context)) {
            openTips();
        }
    }

    private void openTips() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("通知已关闭，是否允许应用推送通知？");
        dialog.setPositiveButton("开启", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                Common.openSettings(context);
            }
        });
        dialog.setNegativeButton("取消", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                toast(context, "已进入后台更新");
            }
        });
        dialog.show();
    }
}
