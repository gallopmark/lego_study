package com.haoyu.app.activity;

import android.view.View;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.AppToolBar;

import butterknife.BindView;

/**
 * 关于我们
 *
 * @author xiaoma
 */
public class AboutUsActivity extends BaseActivity {

    @BindView(R.id.toolBar)
    AppToolBar toolBar;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_about_us;
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }
}
