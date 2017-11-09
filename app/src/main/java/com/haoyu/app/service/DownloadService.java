package com.haoyu.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.haoyu.app.download.DownloadListener;
import com.haoyu.app.download.DownloadManager;
import com.haoyu.app.download.DownloadTask;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.MyUtils;

import java.io.File;

/**
 * Created by acer1 on 2017/10/20.
 */
public class DownloadService extends Service {
    private Notification notification;
    private NotificationManager notificationManager;
    private RemoteViews remoteViews;
    private String savePath;
    private String fileName;
    private String url;
    private final String STATUS_BAR_COVER_CLICK_ACTION = "RETRY";
    private final int NOTIFY = 1;
    private String versionName;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        url = intent.getStringExtra("url");
        versionName = intent.getStringExtra("versionName");
        fileName = "lego_study_" + versionName + ".apk";
        savePath = Constants.fileDownDir;
        download(url, savePath, fileName);
        notification();
        time = System.currentTimeMillis();
        return super.onStartCommand(intent, flags, startId);
    }

    private long time;

    private void download(String url, String downloadPath, final String fileName) {
        DownloadManager.getInstance().create(url).setFilePath(downloadPath).setFileName(fileName).addListener(new DownloadListener() {
            @Override
            public void onProgress(DownloadTask downloadTask, long soFarBytes, long totalBytes) {
                if ((System.currentTimeMillis() - time) > 400 || soFarBytes == totalBytes) {
                    int percent = 0;
                    if (totalBytes > 0) {
                        percent = (int) (soFarBytes * 100 / totalBytes);
                        remoteViews.setProgressBar(R.id.pb_progress, (int) totalBytes, (int) soFarBytes, false);
                        remoteViews.setTextViewText(R.id.tv_percent, percent + "%");
                        notifiy();
                        time = System.currentTimeMillis();
                    }
                }
            }

            @Override
            public void onSuccess(DownloadTask downloadTask, String savePath) {
                MyUtils.installAPK(getApplicationContext(), new File(Constants.fileDownDir + "/" + fileName));

            }

            @Override
            public void onFailed(DownloadTask downloadTask) {
                remoteViews.setViewVisibility(R.id.pb_progress, View.GONE);
                remoteViews.setViewVisibility(R.id.tv_retry, View.VISIBLE);
                notifiy();

            }

            @Override
            public void onPaused(DownloadTask downloadTask) {

            }

            @Override
            public void onCancel(DownloadTask downloadTask) {

            }
        }).start();
    }

    NotificationCompat.Builder builder;

    private void notification() {
        builder =
                new NotificationCompat.Builder(this, null);
        builder.setSmallIcon(R.drawable.lego_ico);
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
        remoteViews.setImageViewResource(R.id.iv_icon, R.drawable.lego_ico);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        remoteViews.setTextViewText(R.id.tv_name, fileName);
        pendingIntent();
        notifiy();
    }

    private void pendingIntent() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(STATUS_BAR_COVER_CLICK_ACTION);
        registerReceiver(onClickReceiver, filter);
        Intent buttonIntent = new Intent(STATUS_BAR_COVER_CLICK_ACTION);
        PendingIntent pendButtonIntent = PendingIntent.getBroadcast(this, 0, buttonIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.tv_retry, pendButtonIntent);
    }


    private void notifiy() {
        builder.setContent(remoteViews);
        notification = builder.build();
        notificationManager.notify(NOTIFY, notification);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (onClickReceiver != null) {
            unregisterReceiver(onClickReceiver);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    BroadcastReceiver onClickReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(STATUS_BAR_COVER_CLICK_ACTION)) {
                //在这里处理点击事件
                download(url, savePath, fileName);
                remoteViews.setViewVisibility(R.id.tv_retry, View.GONE);
                remoteViews.setViewVisibility(R.id.pb_progress, View.VISIBLE);

            }
        }

    };


}
