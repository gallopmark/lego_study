package com.haoyu.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.MWorkshopActivity;
import com.haoyu.app.fragment.WSTSEditFragment;
import com.haoyu.app.fragment.WSTSSubmitFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/12/1.
 * 描述:工作坊添加听课评课
 * 作者:xiaoma
 */

public class WSTSEditActivity extends BaseActivity {
    private WSTSEditActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    private String workshopId, workSectionId;
    private FragmentManager fragmentManager;
    private MWorkshopActivity activity;
    private String startTime, endTime;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_currency_layout;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        workSectionId = getIntent().getStringExtra("workSectionId");
        toolBar.setTitle_text("添加听课评课");
        toolBar.setRight_button_text("提交");
        fragmentManager = getSupportFragmentManager();
        showF1();
    }

    private void showF1() {
        toolBar.setShow_right_button(false);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        WSTSEditFragment f1 = new WSTSEditFragment();
        Bundle bundle = new Bundle();
        bundle.putString("workshopId", workshopId);
        bundle.putString("workSectionId", workSectionId);
        f1.setArguments(bundle);
        transaction.replace(R.id.container, f1);
        f1.setOnNextListener(new WSTSEditFragment.OnNextListener() {
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
        toolBar.setShow_right_button(true);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        final WSTSSubmitFragment f2 = new WSTSSubmitFragment();
        Bundle bundle = new Bundle();
        bundle.putString("workshopId", workshopId);
        bundle.putString("activityId", activity.getId());
        f2.setArguments(bundle);
        transaction.add(R.id.container, f2);
        transaction.commitAllowingStateLoss();
        toolBar.setOnRightClickListener(new AppToolBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                String startTime = f2.getStartTime();
                String endTime = f2.getEndTime();
                if (!f2.isAddItem()) {
                    showMaterialDialog("提示", "至少填写一项评分项");
                } else if (startTime.length() == 0) {
                    showMaterialDialog("提示", "请选择活动开始时间");
                } else if (endTime.length() == 0) {
                    showMaterialDialog("提示", "请选择活动结束时间");
                } else {
                    context.startTime = startTime;
                    context.endTime = endTime;
                    commit();
                }
            }
        });
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

    private void commit() {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId()
                + "/m/activity/wsts/" + activity.getId();
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("activity.startTime", startTime);
        map.put("activity.endTime", endTime);
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
}
