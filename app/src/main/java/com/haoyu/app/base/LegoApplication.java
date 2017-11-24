package com.haoyu.app.base;

import android.app.Activity;
import android.app.Application;

import com.baidu.mobstat.StatService;
import com.haoyu.app.utils.Constants;
import com.tencent.bugly.crashreport.CrashReport;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.wlf.filedownloader.FileDownloadConfiguration;

import java.util.LinkedList;
import java.util.List;


public class LegoApplication extends Application {

    private static LegoApplication instance;
    private List<Activity> activities = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ZXingLibrary.initDisplayOpinion(this);
        initFileDownloader();
        StatService.start(this);
        CrashReport.initCrashReport(this);
    }

    private void initFileDownloader() {
        // 1、创建Builder
        FileDownloadConfiguration.Builder builder = new FileDownloadConfiguration.Builder(this);
//// 2.配置Builder
// 配置下载文件保存的文件夹
        builder.configFileDownloadDir(Constants.videoCache);
// 配置同时下载任务数量，如果不配置默认为2
        builder.configDownloadTaskSize(3);
// 配置失败时尝试重试的次数，如果不配置默认为0不尝试
        builder.configRetryDownloadTimes(5);
// 开启调试模式，方便查看日志等调试相关，如果不配置默认不开启
        builder.configDebugMode(false);
// 配置连接网络超时时间，如果不配置默认为15秒
        builder.configConnectTimeout(25000);// 25秒
// 3、使用配置文件初始化FileDownloader
        FileDownloadConfiguration configuration = builder.build();
        org.wlf.filedownloader.FileDownloader.init(configuration);
    }

    public static LegoApplication getInstance() {
        return instance;
    }


    public void addActivity(Activity activity) {
        this.activities.add(activity);
    }

    public void exit() {
        for (Activity activity : activities) {
            activity.finish();
        }
    }

    public void remove(Activity activity) {
        activities.remove(activity);
    }
}
