package com.haoyu.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Constants;

import java.util.Timer;
import java.util.TimerTask;

public class NetSpeedService extends Service {
    private Timer timer;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        if (timer != null) {
            timer.cancel();
        } else {
            timer = new Timer();
            timer.schedule(task, 1000, 1 * 1000);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 1s后启动任务，每2s执行一次
        return super.onStartCommand(intent, flags, startId);
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            int netType = getNetworkType(getApplicationContext());
            MessageEvent event = new MessageEvent();
            event.setAction(Constants.speedAction);
            event.setObj(netType);
            RxBus.getDefault().post(event);
        }
    };

    private void showNetSpeed() {
        Message msg = mHandler.obtainMessage();
        msg.what = 100;
        mHandler.sendMessage(msg);// 更新界面
    }


    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            showNetSpeed();
        }
    };

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        timer.cancel();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 判断当前网络类型-1为未知网络0为没有网络连接1网络断开或关闭2为以太网3为WiFi4为2G5为3G6为4G
     */

    public static int getNetworkType(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            /** 没有任何网络 */
            return 0;
        }
        if (!networkInfo.isConnected()) {
            /** 网络断开或关闭 */
            return 1;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
            /** 以太网网络 */
            return 2;
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            /** wifi网络，当激活时，默认情况下，所有的数据流量将使用此连接 */
            return 3;
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            /** 移动数据连接,不能与连接共存,如果wifi打开，则自动关闭 */
            switch (networkInfo.getSubtype()) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    /** 2G网络 */
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    /** 3G网络 */
                case TelephonyManager.NETWORK_TYPE_LTE:
                    /** 4G网络 */
                    return 4;

            }
        }
        /** 未知网络 */
        return -1;
    }

}
