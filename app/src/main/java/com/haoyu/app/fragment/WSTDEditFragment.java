package com.haoyu.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.MWorkshopActivity;
import com.haoyu.app.entity.WorkshopActivityResult;
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
 * 描述:工作坊添加教学研讨
 * 作者:xiaoma
 */

public class WSTDEditFragment extends BaseFragment {
    private Activity context;
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.et_content)
    EditText et_content;
    @BindView(R.id.bt_next)
    Button bt_next;
    private String workshopId, workSectionId;
    private OnNextListener onNextListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (Activity) context;
        Bundle bundle = getArguments();
        workshopId = bundle.getString("workshopId");
        workSectionId = bundle.getString("workSectionId");
    }

    @Override
    public int createView() {
        return R.layout.fragment_wstdedit;
    }

    @Override
    public void setListener() {
        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.hideSoftInput(context);
                if (checkOut()) {
                    submitDisc();
                }
            }
        });
    }

    private boolean checkOut() {
        String title = et_title.getText().toString().trim();
        String content = et_content.getText().toString().trim();
        if (title.length() == 0) {
            showMaterialDialog("请输入研讨主题");
            return false;
        } else if (content.length() == 0) {
            showMaterialDialog("请输入研讨内容");
            return false;
        }
        return true;
    }

    private void showMaterialDialog(String message) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage(message);
        dialog.setPositiveButton("确定", null);
        dialog.show();
    }

    private void submitDisc() {
        String title = et_title.getText().toString();
        String content = et_content.getText().toString();
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId() + "/m/activity/wsts";
        final Map<String, String> map = new HashMap<>();
        map.put("activity.relation.id", workSectionId);
        map.put("activity.type", "discussion");
        map.put("discussion.discussionRelations[0].relation.id", workshopId);
        map.put("discussion.title", title);
        map.put("discussion.content", content);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<WorkshopActivityResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError();
            }

            @Override
            public void onResponse(WorkshopActivityResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    if (onNextListener != null) {
                        onNextListener.onNext(response.getResponseData());
                    }
                }
            }
        }, map));
    }

    public interface OnNextListener {
        void onNext(MWorkshopActivity activity);
    }

    public void setOnNextListener(OnNextListener onNextListener) {
        this.onNextListener = onNextListener;
    }

}
