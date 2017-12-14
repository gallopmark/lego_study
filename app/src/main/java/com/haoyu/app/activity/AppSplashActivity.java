package com.haoyu.app.activity;

import android.content.Intent;
import android.os.Handler;
import android.view.KeyEvent;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;

/**
 * 创建日期：2017/6/23 on 15:30
 * 描述:启动页
 * 作者:马飞奔 Administrator
 */
public class AppSplashActivity extends BaseActivity {

    private Handler handler;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_app_splash;
    }

    @Override
    public void initData() {
        handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enter();
            }
        }, 2500);
    }

    private void enter() {
        if (firstLogin()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
