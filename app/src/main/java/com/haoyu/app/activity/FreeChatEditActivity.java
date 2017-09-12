package com.haoyu.app.activity;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.CommentEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

public class FreeChatEditActivity extends BaseActivity {
    private Activity mContext = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.tv_submit)
    TextView tv_submit;
    private String relationId;
    @BindView(R.id.et_content)
    EditText mContent;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_freechat_edit;
    }

    @Override
    public void initView() {
        relationId = getIntent().getStringExtra("relationId");
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCommunication(mContent.getText().toString().trim());
            }
        });
    }

    private void createCommunication(String message) {
        if (message.trim().length() > 0) {
            String url = Constants.OUTRT_NET + "/m/comment";
            Map<String, String> map = new HashMap<>();
            map.put("content", message);
            map.put("relation.id", relationId);
            map.put("relation.type", "workshop_comment");
            addSubscription(OkHttpClientManager.postAsyn(mContext, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<CommentEntity>>() {
                @Override
                public void onBefore(Request request) {
                    super.onBefore(request);
                    showTipDialog();
                }

                @Override
                public void onError(Request request, Exception e) {
                    hideTipDialog();
                    onNetWorkError(mContext);
                }

                @Override
                public void onResponse(BaseResponseResult<CommentEntity> response) {
                    hideTipDialog();
                    if (response != null && response.getResponseData() != null) {
                        MessageEvent event = new MessageEvent();
                        event.action = Action.CREATE_COMMENT;
                        event.obj = response.getResponseData();
                        RxBus.getDefault().post(event);
                        toastFullScreen("发表成功", true);
                        finish();
                    } else {
                        toastFullScreen("发表失败", false);
                    }
                }
            }, map));
        } else {
            toast(mContext, "请输入发表的内容");
        }

    }
}
