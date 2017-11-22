package com.haoyu.app.activity;

import android.content.Intent;
import android.view.KeyEvent;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

/**
 * 创建日期：2017/6/23 on 15:30
 * 描述:启动页
 * 作者:马飞奔 Administrator
 */
public class AppSplashActivity extends BaseActivity {

    @Override
    public int setLayoutResID() {
        return R.layout.activity_app_splash;
    }

    @Override
    public void initView() {
        addSubscription(Flowable.timer(2000, TimeUnit.MILLISECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                enter();
            }
        }));
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

}
