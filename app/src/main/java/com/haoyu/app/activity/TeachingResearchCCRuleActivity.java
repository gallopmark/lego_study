package com.haoyu.app.activity;

import android.view.View;
import android.widget.RadioButton;
import android.widget.ScrollView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.AppToolBar;

import butterknife.BindView;

/**
 * 创建日期：2017/2/16 on 15:36
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class TeachingResearchCCRuleActivity extends BaseActivity implements View.OnClickListener {
    private TeachingResearchCCRuleActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;  //返回
    @BindView(R.id.scrollview)
    ScrollView scrollview;
    @BindView(R.id.rb_idea)
    RadioButton rb_idea;
    @BindView(R.id.rb_support)
    RadioButton rb_support;
    @BindView(R.id.rb_advise)
    RadioButton rb_advise;
    @BindView(R.id.rb_build)
    RadioButton rb_build;
    @BindView(R.id.rb_viewing)
    RadioButton rb_viewing;
    @BindView(R.id.ll_idea)
    View ll_idea;
    @BindView(R.id.ll_support)
    View ll_support;
    @BindView(R.id.ll_advise)
    View ll_advise;
    @BindView(R.id.ll_build)
    View ll_build;
    @BindView(R.id.ll_viewing)
    View ll_viewing;
    private int selectIndex = 1;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teaching_research_cc_rule;
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        rb_idea.setOnClickListener(context);
        rb_support.setOnClickListener(context);
        rb_advise.setOnClickListener(context);
        rb_build.setOnClickListener(context);
        rb_viewing.setOnClickListener(context);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rb_idea:
                selectIndex = 1;
                changeStatu(selectIndex);
                break;
            case R.id.rb_support:
                selectIndex = 2;
                changeStatu(selectIndex);
                break;
            case R.id.rb_advise:
                selectIndex = 3;
                changeStatu(selectIndex);
                break;
            case R.id.rb_build:
                selectIndex = 4;
                changeStatu(selectIndex);
                break;
            case R.id.rb_viewing:
                selectIndex = 5;
                changeStatu(selectIndex);
                break;
        }
    }

    private void changeStatu(int selectIndex) {
        rb_idea.setChecked(false);
        rb_support.setChecked(false);
        rb_advise.setChecked(false);
        rb_build.setChecked(false);
        rb_viewing.setChecked(false);
        switch (selectIndex) {
            case 1:
                rb_idea.setChecked(true);
                scrollToPosition(ll_idea);
                break;
            case 2:
                rb_support.setChecked(true);
                scrollToPosition(ll_support);
                break;
            case 3:
                rb_advise.setChecked(true);
                scrollToPosition(ll_advise);
                break;
            case 4:
                rb_build.setChecked(true);
                scrollToPosition(ll_build);
                break;
            case 5:
                rb_viewing.setChecked(true);
                scrollToPosition(ll_viewing);
                break;
        }
    }

    /**
     * 滑动到指定位置
     */
    private void scrollToPosition(final View view) {
        scrollview.smoothScrollTo(0, (int) view.getY());
    }
}
