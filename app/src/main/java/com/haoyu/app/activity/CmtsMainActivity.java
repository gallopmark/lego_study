package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioGroup;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.fragment.CmtsLsonMainFragment;
import com.haoyu.app.fragment.CmtsMovMainFragment;
import com.haoyu.app.fragment.CmtsSaysMainFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.AppToolBar;

import butterknife.BindView;


/**
 * 创建日期：2017/8/15 on 11:03
 * 描述:教研
 * 作者:马飞奔 Administrator
 */
public class CmtsMainActivity extends BaseActivity {
    private CmtsMainActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.line_says)
    View line_says;
    @BindView(R.id.line_class)
    View line_class;
    @BindView(R.id.line_activity)
    View line_activity;
    private CmtsSaysMainFragment ssFragment;
    private CmtsLsonMainFragment ccFragment;
    private CmtsMovMainFragment atFragment;
    private FragmentManager fragmentManager;
    private int selected = 1;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_cmts_main;
    }

    @Override
    public void initView() {
        setSupportToolbar();
        fragmentManager = getSupportFragmentManager();
        setSelected(selected);
    }

    private void setSupportToolbar() {
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                if (selected == 1)
                    startActivity(new Intent(context, CmtsSaysEditActivity.class));
                else if (selected == 2)
                    startActivity(new Intent(context, CmtsLessonCreateActivity.class));
            }
        });
    }

    private void setSelected(int selected) {
        line_says.setVisibility(View.INVISIBLE);
        line_class.setVisibility(View.INVISIBLE);
        line_activity.setVisibility(View.INVISIBLE);
        switch (selected) {
            case 1:
                toolBar.setShow_right_button(true);
                line_says.setVisibility(View.VISIBLE);
                break;
            case 2:
                toolBar.setShow_right_button(false);
                line_class.setVisibility(View.VISIBLE);
                break;
            case 3:
                toolBar.setShow_right_button(true);
                line_activity.setVisibility(View.VISIBLE);
                break;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        switch (selected) {
            case 1:
                if (ssFragment == null) {
                    ssFragment = new CmtsSaysMainFragment();
                    transaction.add(R.id.content, ssFragment);
                } else
                    transaction.show(ssFragment);
                break;
            case 2:
                if (ccFragment == null) {
                    ccFragment = new CmtsLsonMainFragment();
                    transaction.add(R.id.content, ccFragment);
                } else
                    transaction.show(ccFragment);
                break;
            case 3:
                if (atFragment == null) {
                    atFragment = new CmtsMovMainFragment();
                    transaction.add(R.id.content, atFragment);
                } else
                    transaction.show(atFragment);
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (ssFragment != null)
            transaction.hide(ssFragment);
        if (ccFragment != null)
            transaction.hide(ccFragment);
        if (atFragment != null)
            transaction.hide(atFragment);
    }

    @Override
    public void setListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.rb_says:
                        selected = 1;
                        break;
                    case R.id.rb_class:
                        selected = 2;
                        break;
                    case R.id.rb_activity:
                        selected = 3;
                        break;
                }
                setSelected(selected);
            }
        });
    }

}
