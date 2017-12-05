package com.haoyu.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.MWorkshopActivity;
import com.haoyu.app.fragment.WSTDEditFragment;
import com.haoyu.app.fragment.WSTDSbNumFragment;
import com.haoyu.app.fragment.WSTDSbTimeFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/5/27 on 15:30
 * 描述：工作坊添加教学研讨活动
 * 作者:马飞奔 Administrator
 */
public class WSTDEditActivity extends BaseActivity {
    private WSTDEditActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    private String workshopId, workSectionId;
    private FragmentManager fragmentManager;
    private MWorkshopActivity activity;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_currency_layout;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        workSectionId = getIntent().getStringExtra("workSectionId");
        toolBar.setTitle_text("添加主题研讨");
        toolBar.setRight_button_text("提交");
        fragmentManager = getSupportFragmentManager();
        showF1();
    }

    private void showF1() {
        toolBar.setShow_right_button(false);
        WSTDEditFragment f1 = new WSTDEditFragment();
        Bundle bundle = new Bundle();
        bundle.putString("workshopId", workshopId);
        bundle.putString("workSectionId", workSectionId);
        f1.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, f1);
        f1.setOnNextListener(new WSTDEditFragment.OnNextListener() {
            @Override
            public void onNext(MWorkshopActivity activity) {
                context.activity = activity;
                Intent intent = new Intent();
                intent.putExtra("activity", activity);
                setResult(RESULT_OK, intent);
                showF2();
            }
        });
        transaction.commitAllowingStateLoss();
    }

    private void showF2() {
        toolBar.setShow_right_button(false);
        WSTDSbNumFragment f2 = new WSTDSbNumFragment();
        Bundle bundle = new Bundle();
        bundle.putString("workshopId", workshopId);
        bundle.putString("activityId", activity.getId());
        f2.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, f2);
        f2.setOnNextListener(new WSTDSbNumFragment.OnNextListener() {
            @Override
            public void onNext() {
                showF3();
            }
        });
        transaction.commitAllowingStateLoss();
    }

    private void showF3() {
        toolBar.setShow_right_button(true);
        final WSTDSbTimeFragment f3 = new WSTDSbTimeFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, f3);
        transaction.commitAllowingStateLoss();
        toolBar.setOnRightClickListener(new AppToolBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                String startTime = f3.getStartTime();
                String endTime = f3.getEndTime();
                if (!TextUtils.isEmpty(startTime) || !TextUtils.isEmpty(endTime)) {
                    commit(startTime, endTime);
                } else {
                    finish();
                }
            }
        });
    }

    private void commit(String startTime, String endTime) {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId()
                + "/m/activity/wsts/" + activity.getId();
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        if (!TextUtils.isEmpty(startTime)) {
            map.put("activity.startTime", startTime);
        }
        if (!TextUtils.isEmpty(endTime)) {
            map.put("activity.endTime", endTime);
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    finish();
                }
            }
        }, map));
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
