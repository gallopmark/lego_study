package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.DiscussResult;
import com.haoyu.app.fragment.CmtsStatementFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.RippleView;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/1/10 on 14:25
 * 描述: 教研话题详情
 * 作者:马飞奔 Administrator
 */
public class CmtsStatementActivity extends BaseActivity {
    private CmtsStatementActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    private DiscussEntity discussEntity;
    private String relationId;  //研说id,研说关系Id

    @Override
    public int setLayoutResID() {
        return R.layout.activity_currency;
    }

    @Override
    public void initView() {
        String title = getResources().getString(R.string.study_says_detail);
        String empty_text = getResources().getString(R.string.study_says_emptylist);
        toolBar.setTitle_text(title);
        toolBar.getIv_rightImage().setImageResource(R.drawable.teaching_research_dot);
        tv_empty.setText(empty_text);
        discussEntity = (DiscussEntity) getIntent().getSerializableExtra("entity");
        relationId = discussEntity.getId();
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/discussion/cmts/view/" + relationId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<DiscussResult>() {
            @Override
            public void onBefore(Request request) {
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(DiscussResult discussResult) {
                loadingView.setVisibility(View.GONE);
                if (discussResult != null && discussResult.getResponseData() != null) {
                    updateUI(discussResult.getResponseData());
                } else {
                    tv_empty.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    /*显示详情*/
    private void updateUI(DiscussEntity entity) {
        if (entity.getCreator() != null && entity.getCreator().getId() != null && entity.getCreator().getId().equals(getUserId())) {
            toolBar.setShow_right_button(true);
        }
        CmtsStatementFragment fragment = new CmtsStatementFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("entity", entity);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    @Override
    public void setListener() {
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                showBottomDialog();
            }
        });
    }

    private void showBottomDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_delete, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        RippleView rv_delete = view.findViewById(R.id.rv_delete);
        RippleView rv_cancel = view.findViewById(R.id.rv_cancel);
        RippleView.OnRippleCompleteListener listener = new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView view) {
                switch (view.getId()) {
                    case R.id.rv_delete:
                        showTipsDialog();
                        break;
                    case R.id.rv_cancel:
                        break;
                }
                dialog.dismiss();
            }
        };
        rv_delete.setOnRippleCompleteListener(listener);
        rv_cancel.setOnRippleCompleteListener(listener);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(ScreenUtils.getScreenWidth(context), LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setWindowAnimations(R.style.dialog_anim);
        window.setContentView(view);
        window.setGravity(Gravity.BOTTOM);
    }

    private void showTipsDialog() {
        MaterialDialog materialDialog = new MaterialDialog(context);
        materialDialog.setTitle("提示");
        materialDialog.setMessage("你确定删除吗？");
        materialDialog.setNegativeButton("确定", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                deleteSay();
            }
        });
        materialDialog.setPositiveButton("取消", null);
        materialDialog.show();
    }

    /**
     * 删除研说
     */
    private void deleteSay() {
        String url = Constants.OUTRT_NET + "/m/discussion/cmts/" + relationId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
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
                    MessageEvent event = new MessageEvent();
                    event.action = Action.DELETE_STUDY_SAYS;
                    event.obj = discussEntity;
                    RxBus.getDefault().post(event);
                    toastFullScreen("已成功删除，返回首页", true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 3000);
                } else {
                    toast(context, "删除失败");
                }
            }
        }, map));

    }

}
