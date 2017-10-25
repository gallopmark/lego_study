package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioGroup;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.fragment.TeachResearchATFragment;
import com.haoyu.app.fragment.TeachResearchCCFragment;
import com.haoyu.app.fragment.TeachResearchSSFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.AppToolBar;

import butterknife.BindView;


/**
 * 创建日期：2017/8/15 on 11:03
 * 描述:教研
 * 作者:马飞奔 Administrator
 */
public class TeachingResearchActivity extends BaseActivity {
    private TeachingResearchActivity context = this;
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
    private TeachResearchSSFragment ssFragment;
    private TeachResearchCCFragment ccFragment;
    private TeachResearchATFragment atFragment;
    private FragmentManager fragmentManager;
    private int selected = 1;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teaching_research;
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
                    startActivity(new Intent(context, TeachingResearchCreateSSActivity.class));
                else if (selected == 2)
                    startActivity(new Intent(context, TeachingResearchCreateCCActivity.class));
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
                toolBar.setShow_right_button(false);
                line_activity.setVisibility(View.VISIBLE);
                break;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        switch (selected) {
            case 1:
                if (ssFragment == null) {
                    ssFragment = new TeachResearchSSFragment();
                    transaction.add(R.id.content, ssFragment);
                } else
                    transaction.show(ssFragment);
                break;
            case 2:
                if (ccFragment == null) {
                    ccFragment = new TeachResearchCCFragment();
                    transaction.add(R.id.content, ccFragment);
                } else
                    transaction.show(ccFragment);
                break;
            case 3:
                if (atFragment == null) {
                    atFragment = new TeachResearchATFragment();
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
