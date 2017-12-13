package com.haoyu.app.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.lego.student.R;

import java.math.BigDecimal;

/**
 * 创建日期：2017/8/15 on 10:47
 * 描述:教研活动
 * 作者:马飞奔 Administrator
 */
public class CmtsMovMainFragment extends BaseFragment {
    private RadioGroup radioGroup;
    private RadioButton rb_all;
    private RadioButton rb_my;
    private String text_all, text_my;
    private CmtsMovChildFragment f1, f2;
    private FragmentManager fragmentManager;
    private int checkIndex = 1;

    @Override
    public int createView() {
        return R.layout.fragment_cmtsmain;
    }

    @Override
    public void initView(View view) {
        radioGroup = view.findViewById(R.id.radioGroup);
        rb_all = view.findViewById(R.id.rb_all);
        rb_my = view.findViewById(R.id.rb_my);
        text_all = getResources().getString(R.string.teach_active_all);
        text_my = getResources().getString(R.string.teach_active_my);
    }

    @Override
    public void initData() {
        rb_all.setText(text_all);
        rb_my.setText(text_my);
        fragmentManager = getChildFragmentManager();
        setCheckIndex(checkIndex);
    }

    public void setCheckIndex(int checkIndex) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        switch (checkIndex) {
            case 1:
                if (f1 == null) {
                    f1 = new CmtsMovChildFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", 1);
                    f1.setArguments(bundle);
                    f1.setOnResponseListener(new CmtsMovChildFragment.OnResponseListener() {
                        @Override
                        public void getTotalCount(int totalCount) {
                            rb_all.setText(text_all + "（" + getCount(totalCount) + "）");
                        }
                    });
                    transaction.add(R.id.content, f1);
                } else {
                    transaction.show(f1);
                }
                break;
            case 2:
                if (f2 == null) {
                    f2 = new CmtsMovChildFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", 2);
                    f2.setArguments(bundle);
                    f2.setOnResponseListener(new CmtsMovChildFragment.OnResponseListener() {
                        @Override
                        public void getTotalCount(int totalCount) {
                            rb_my.setText(text_my + "（" + getCount(totalCount) + "）");
                        }
                    });
                    transaction.add(R.id.content, f2);
                } else {
                    transaction.show(f2);
                }
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (f1 != null)
            transaction.hide(f1);
        if (f2 != null)
            transaction.hide(f2);
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

    @Override
    public void setListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.rb_all:
                        checkIndex = 1;
                        break;
                    case R.id.rb_my:
                        checkIndex = 2;
                        break;
                }
                setCheckIndex(checkIndex);
            }
        });
    }
}
