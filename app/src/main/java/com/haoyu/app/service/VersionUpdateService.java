package com.haoyu.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.haoyu.app.lego.student.BuildConfig;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.AppToast;
import com.haoyu.app.utils.Common;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;

/**
 * 创建日期：2017/11/23.
 * 描述:版本更新服务
 * 作者:xiaoma
 */

public class VersionUpdateService extends Service {
    //定义notification实用的ID
    private static final int NOTIFICATION_ID = 0x3;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private Notification notification;
    private String channedlId = "lego_study";
    private RemoteViews remoteViews;
    private String savePath;
    private String url, versionName;
    private String fileName = "study_";
    private boolean isError;
    private String ACTION_DELETE = "ACTION_DELETE";
    private String ACTION_CLICK = "ACTION_CLICK";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_DELETE)) {
                stopSelf();
            } else if (intent.getAction().equals(ACTION_CLICK)) {
                if (isError) {
                    excute();
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        savePath = getSavePath();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this, channedlId);
        builder.setSmallIcon(R.drawable.lego_ico);
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_remoteview);
        builder.setContent(remoteViews);
        notification = builder.build();
//        notification.flags = Notification.FLAG_NO_CLEAR;// 可让通知栏点清除键不消失
        PendingIntent ivIntent = PendingIntent.getBroadcast(this, 1, new Intent(ACTION_DELETE), 0);
        remoteViews.setOnClickPendingIntent(R.id.iv_delete, ivIntent);
        Intent intent = new Intent(ACTION_CLICK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2, intent, 0);
        notification.contentIntent = pendingIntent;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DELETE);
        filter.addAction(ACTION_CLICK);
        registerReceiver(receiver, filter);
        FileDownloader.setup(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        url = intent.getStringExtra("url");
        versionName = intent.getStringExtra("versionName");
        fileName = "study_";
        if (versionName != null) {
            fileName += versionName + ".apk";
        } else {
            fileName += System.currentTimeMillis() + ".apk";
        }
        savePath += (File.separator + fileName);
        excute();
        return super.onStartCommand(intent, flags, startId);
    }

    private String getSavePath() {
        String savePath;
        try {
            savePath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "apk";
        } catch (Exception e) {
            savePath = getFilesDir().getAbsolutePath() + File.separator + "apk";
        }
        File file = new File(savePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public void excute() {
        FileDownloader.getImpl().create(url)
                .setPath(savePath, false)
                .setAutoRetryTimes(1)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void retry(BaseDownloadTask task, Throwable ex, int retryingTimes, int soFarBytes) {
                        String error_text = "下载失败，正在重试！";
                        remoteViews.setTextViewText(R.id.tv_tips, error_text);
                    }

                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        remoteViews.setTextViewText(R.id.tv_tips, fileName);
                        remoteViews.setImageViewResource(R.id.iv_ico, R.drawable.lego_ico);
                        notifyId();
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        remoteViews.setProgressBar(R.id.downloadBar, totalBytes, soFarBytes, false);
                        String progres_text = "已下载 " + Common.accuracy(soFarBytes, totalBytes, 0);
                        remoteViews.setTextViewText(R.id.tv_progress, progres_text);
                        notifyId();
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        stopSelf();
                        String savePath = task.getTargetFilePath();
                        installAPK(new File(savePath));
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        isError = true;
                        String error_text = "下载失败，请点击重试！";
                        remoteViews.setTextViewText(R.id.tv_tips, error_text);
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                }).start();
    }

    private void notifyId() {
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void installAPK(File apkFile) {
        Intent intent = new Intent();
        try {//判断是否是AndroidN以及更高的版本
            String type = "application/vnd.android.package-archive";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(Intent.ACTION_VIEW);
                Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", apkFile);
                intent.setDataAndType(contentUri, type);
            } else {
                //设置intent的data和Type属性。
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apkFile), type);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            startActivity(intent);
        } catch (Exception e) {
            toast(this, "安装失败");
        }
    }

    private void toast(Context context, String text) {
        View v = LayoutInflater.from(context).inflate(R.layout.app_layout_toast, null);
        TextView textView = v.findViewById(R.id.tv_text);
        textView.setText(text);
        AppToast mToast = new AppToast(context, R.style.AppToast);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.setView(v);
        mToast.show();
    }

    private void release() {
        int downloadId = FileDownloadUtils.generateId(url, savePath, false);
        FileDownloader.getImpl().pause(downloadId);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        unregisterReceiver(receiver);
    }

}
