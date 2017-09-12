package com.haoyu.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.fragment.PageAllQuestionFragment;
import com.haoyu.app.fragment.PageMyQuestionFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.AppToolBar;

import java.math.BigDecimal;

import butterknife.BindView;

/**
 * 创建日期：2016/12/27 on 9:58
 * 描述: 工作坊互助问答页面
 * 作者:马飞奔 Administrator
 */
public class WorkshopQuestionActivity extends BaseActivity {
    private WorkshopQuestionActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.rb_allQuestion)
    RadioButton rb_all;
    @BindView(R.id.bottomView)
    TextView bottomView;
    private String relationId, relationType = "workshop_question";
    private PageAllQuestionFragment allQuestionFragment;
    private PageMyQuestionFragment myQuestionFragment;
    private FragmentManager fragmentManager;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_workshop_question;
    }

    @Override
    public void initView() {
        relationId = getIntent().getStringExtra("relationId");  //得到工作坊Id
        fragmentManager = getSupportFragmentManager();
        setSelected(1);
    }


    @Override
    public void setListener() {
        if (allQuestionFragment != null)
            allQuestionFragment.setOnResponseListener(new PageAllQuestionFragment.OnResponseListener() {
                @Override
                public void getTotalCount(int totalCount) {
                    rb_all.setText("全部" + " (" + getCount(totalCount) + ")");
                }
            });
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        bottomView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context, AppQuestionEditActivity.class);
                intent.putExtra("relationId", relationId);
                intent.putExtra("relationType", relationType);
                startActivity(intent);
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.rb_allQuestion:
                        setSelected(1);
                        break;
                    case R.id.rb_myQuestion:
                        setSelected(2);
                        break;
                }
            }
        });
    }

    public void setSelected(int selected) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        switch (selected) {
            case 1:
                if (allQuestionFragment == null) {
                    allQuestionFragment = new PageAllQuestionFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "workshop");
                    bundle.putString("relationId", relationId);
                    bundle.putString("relationType", relationType);
                    allQuestionFragment.setArguments(bundle);
                    transaction.add(R.id.content, allQuestionFragment);
                } else
                    transaction.show(allQuestionFragment);
                break;
            case 2:
                if (myQuestionFragment == null) {
                    myQuestionFragment = new PageMyQuestionFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "workshop");
                    bundle.putString("relationId", relationId);
                    bundle.putString("relationType", relationType);
                    myQuestionFragment.setArguments(bundle);
                    transaction.add(R.id.content, myQuestionFragment);
                } else
                    transaction.show(myQuestionFragment);
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (allQuestionFragment != null)
            transaction.hide(allQuestionFragment);
        if (myQuestionFragment != null)
            transaction.hide(myQuestionFragment);
    }

    private String getCount(int count) {
        if (count < 10000) {
            return String.valueOf(count);
        }
        double num = (double) count / 10000;
        BigDecimal bd = new BigDecimal(num);
        num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (num < 10000) {
            if (num / 1000 > 1) {
                bd = new BigDecimal(num / 1000);
                num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                return num + "千万";
            } else if (num / 100 > 1) {
                bd = new BigDecimal(num / 100);
                num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                return num + "百万";
            } else if (num / 10 > 1) {
                bd = new BigDecimal(num / 10);
                num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                return num + "十万";
            }
            return num + "万";
        }
        return "大于1亿";
    }
}
