package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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
public class TeachingResearchActivity extends BaseActivity implements View.OnClickListener {
    private TeachingResearchActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.bt_ss)
    Button bt_ss;
    @BindView(R.id.bt_cc)
    Button bt_cc;
    @BindView(R.id.bt_at)
    Button bt_at;
    @BindView(R.id.iv_tabss)
    ImageView iv_tabss;
    @BindView(R.id.iv_tabcc)
    ImageView iv_tabcc;
    @BindView(R.id.iv_tabat)
    ImageView iv_tabat;
    @BindView(R.id.tab_line)
    View tab_line;
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
        iv_tabss.setVisibility(View.INVISIBLE);
        iv_tabcc.setVisibility(View.INVISIBLE);
        iv_tabat.setVisibility(View.INVISIBLE);
        switch (selected) {
            case 1:
                tab_line.setBackgroundColor(ContextCompat.getColor(context, R.color.tab_teachss));
                toolBar.setShow_right_button(true);
                iv_tabss.setVisibility(View.VISIBLE);
                break;
            case 2:
                tab_line.setBackgroundColor(ContextCompat.getColor(context, R.color.tab_teachcc));
                toolBar.setShow_right_button(false);
                iv_tabcc.setVisibility(View.VISIBLE);
                break;
            case 3:
                tab_line.setBackgroundColor(ContextCompat.getColor(context, R.color.tab_teachat));
                toolBar.setShow_right_button(false);
                iv_tabat.setVisibility(View.VISIBLE);
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
        bt_ss.setOnClickListener(context);
        bt_cc.setOnClickListener(context);
        bt_at.setOnClickListener(context);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_ss:
                selected = 1;
                setSelected(selected);
                return;
            case R.id.bt_cc:
                selected = 2;
                setSelected(selected);
                return;
            case R.id.bt_at:
                selected = 3;
                setSelected(selected);
                return;
        }
    }
}
