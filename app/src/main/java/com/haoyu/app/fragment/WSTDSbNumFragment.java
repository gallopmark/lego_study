package com.haoyu.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.gdei.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/12/5.
 * 描述:工作坊设置教学研讨活动完成指标（回复数，子回复数）
 * 作者:xiaoma
 */

public class WSTDSbNumFragment extends BaseFragment {
    private Activity context;
    @BindView(R.id.et_mainCount)
    EditText et_mainCount;
    @BindView(R.id.et_childCount)
    EditText et_childCount;
    @BindView(R.id.bt_next)
    Button bt_next;
    private String workshopId, activityId;
    private OnNextListener onNextListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (Activity) context;
        Bundle bundle = getArguments();
        workshopId = bundle.getString("workshopId");
        activityId = bundle.getString("activityId");
    }

    @Override
    public int createView() {
        return R.layout.fragment_wstdsbnum;
    }

    @Override
    public void setListener() {
        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.hideSoftInput(context);
                String mainCount = et_mainCount.getText().toString().trim();
                String childCount = et_childCount.getText().toString().trim();
                if (!TextUtils.isEmpty(mainCount) || !TextUtils.isEmpty(childCount)) {
                    submitNum(mainCount, childCount);
                } else {
                    if (onNextListener != null) {
                        onNextListener.onNext();
                    }
                }
            }
        });
    }

    private void submitNum(String mainCount, String childCount) {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId() + "/m/activity/wsts/" + activityId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("activity.attributeMap[main_post_num].attrValue", mainCount);
        map.put("activity.attributeMap[sub_post_num].attrValue", childCount);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {

            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    if (onNextListener != null) {
                        onNextListener.onNext();
                    }
                }
            }
        }, map));
    }

    public interface OnNextListener {
        void onNext();
    }

    public void setOnNextListener(OnNextListener onNextListener) {
        this.onNextListener = onNextListener;
    }

}
