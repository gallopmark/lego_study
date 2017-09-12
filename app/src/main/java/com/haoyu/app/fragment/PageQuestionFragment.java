package com.haoyu.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.haoyu.app.activity.AppQuestionEditActivity;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.lego.student.R;

import java.math.BigDecimal;

import butterknife.BindView;

/**
 * 创建日期：2017/8/16 on 11:17
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class PageQuestionFragment extends BaseFragment {
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.rb_allQuestion)
    RadioButton rb_allQuestion;
    @BindView(R.id.bt_createQA)
    Button bt_createQA;
    private PageAllQuestionFragment allQuestionFragment;
    private PageNoticeQuestionFragment noticeQuestionFragment;
    private PageMyQuestionFragment myQuestionFragment;
    private FragmentManager fragmentManager;
    private String relationId;

    @Override
    public int createView() {
        return R.layout.fragment_page_question;
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            relationId = bundle.getString("entityId");
        }
    }

    @Override
    public void initData() {
        fragmentManager = getChildFragmentManager();
        setSelected(1);
    }

    public void setSelected(int selected) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        switch (selected) {
            case 1:
                if (allQuestionFragment == null) {
                    allQuestionFragment = new PageAllQuestionFragment();
                    allQuestionFragment.setOnResponseListener(new PageAllQuestionFragment.OnResponseListener() {
                        @Override
                        public void getTotalCount(int totalCount) {
                            rb_allQuestion.setText("全部" + " (" + getCount(totalCount) + ")");
                        }
                    });
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "course");
                    bundle.putString("relationId", relationId);
                    bundle.putString("relationType", "course_study");
                    allQuestionFragment.setArguments(bundle);
                    transaction.add(R.id.content, allQuestionFragment);
                } else
                    transaction.show(allQuestionFragment);
                break;
            case 2:
                if (noticeQuestionFragment == null) {
                    noticeQuestionFragment = new PageNoticeQuestionFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("relationId", relationId);
                    bundle.putString("relationType", "course_study");
                    noticeQuestionFragment.setArguments(bundle);
                    transaction.add(R.id.content, noticeQuestionFragment);
                } else
                    transaction.show(noticeQuestionFragment);
                break;
            case 3:
                if (myQuestionFragment == null) {
                    myQuestionFragment = new PageMyQuestionFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "course");
                    bundle.putString("relationId", relationId);
                    bundle.putString("relationType", "course_study");
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
        if (noticeQuestionFragment != null)
            transaction.hide(noticeQuestionFragment);
        if (myQuestionFragment != null)
            transaction.hide(myQuestionFragment);
    }

    @Override
    public void setListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.rb_allQuestion:
                        setSelected(1);
                        break;
                    case R.id.rb_myNotice:
                        setSelected(2);
                        break;
                    case R.id.rb_myQuestion:
                        setSelected(3);
                        break;
                }
            }
        });
        bt_createQA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), AppQuestionEditActivity.class);
                intent.putExtra("relationId", relationId);
                intent.putExtra("relationType", "course_study");
                getActivity().startActivity(intent);
            }
        });
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
