package com.haoyu.app.base;

import android.app.Activity;
import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.baidu.mobstat.StatService;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.haoyu.app.utils.Constants;
import com.tencent.bugly.crashreport.CrashReport;

import org.wlf.filedownloader.FileDownloadConfiguration;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.CookieJar;


public class LegoApplication extends Application {

    private static LegoApplication application;
    private List<Activity> activities = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        initFileDownloader();
        StatService.start(this);
        setRxJavaErrorHandler();
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

    /**
     * RxJava2 当取消订阅后(dispose())，RxJava抛出的异常后续无法接收(此时后台线程仍在跑，可能会抛出IO等异常),全部由RxJavaPlugin接收，需要提前设置ErrorHandler
     */
    private void setRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.e("application", throwable.getMessage());
            }
        });
    }

    public static void addActivity(Activity activity) {
        application.activities.add(activity);
    }

    public static void exit() {
        for (Activity activity : application.activities) {
            activity.finish();
        }
    }

    public static void remove(Activity activity) {
        application.activities.remove(activity);
    }

    public static CookieJar getCookieJar() {
        return new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(application));
    }

    public static String getDefaultFilesDir() {
        try {
            return application.getExternalFilesDir(null).getAbsolutePath();
        } catch (Exception e) {
            return application.getFilesDir().getAbsolutePath();
        }
    }

    public static String getExternalStorageDir() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return Environment.getRootDirectory().getAbsolutePath();
    }
}
