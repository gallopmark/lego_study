package com.haoyu.app.base;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.baidu.mobstat.StatService;
import com.haoyu.app.activity.MainActivity;
import com.haoyu.app.activity.AppSplashActivity;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.SharePreferenceHelper;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.wlf.filedownloader.FileDownloadConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class GdeiApplication extends Application {

    private static GdeiApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ZXingLibrary.initDisplayOpinion(this);
        initFileDownloader();
        StatService.start(this);
//        // 以下用来捕获程序崩溃异常
//        if (!Config.DEBUG) {
//            Thread.setDefaultUncaughtExceptionHandler(restartHandler); // 程序崩溃时触发线程
//        }
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

    public static GdeiApplication getInstance() {
        return instance;
    }

    // 创建服务用于捕获崩溃异常
    private Thread.UncaughtExceptionHandler restartHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            writeError(ex);
            restartApp();//发生崩溃异常时,重启应用
        }
    };

    private void writeError(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            //用于格式化日期,作为日志文件名的一部分
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String time = formatter.format(new Date());
            String fileName = time + ".log";
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String logPath = Constants.exceptionCrash;
                File file = new File(logPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(logPath + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
        } catch (Exception e) {
        }
    }

    // 重启应用
    @SuppressWarnings("WrongConstant")
    public void restartApp() {
        Intent intent = new Intent();
        if (!SharePreferenceHelper.getPassWord(this).equals(""))
            intent.setClass(this, MainActivity.class);
        else
            intent.setClass(this, AppSplashActivity.class);
        PendingIntent restartIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                restartIntent); // 1秒钟后重启应用
        ExitApplication.getInstance().exit();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
