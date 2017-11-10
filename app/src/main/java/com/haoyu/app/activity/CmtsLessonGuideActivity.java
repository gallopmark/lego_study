package com.haoyu.app.activity;

import android.view.View;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.AppToolBar;

import butterknife.BindView;

/**
 * 创建日期：2017/9/22 on 14:31
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CmtsLessonGuideActivity extends BaseActivity {
    @BindView(R.id.toolBar)
    AppToolBar toolBar;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_cmts_lessonguide;
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
