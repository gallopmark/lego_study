package com.haoyu.app.activity;

import android.view.View;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.BriefingEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 研修简报详情
 */
public class BriefingDetailActivity extends BaseActivity {
    private BriefingDetailActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_createDate)
    TextView tv_createDate;
    @BindView(R.id.tv_content)
    HtmlTextView tv_content;
    private String relationId;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_briefing_detail;
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
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/briefing/view/" + relationId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<BriefingEntity>>() {
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
            public void onResponse(BaseResponseResult<BriefingEntity> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    initContent(response.getResponseData());
                }
            }
        }));
    }

    private void initContent(BriefingEntity entity) {
        if (entity.getTitle() != null) {
            tv_title.setText(entity.getTitle());
        } else {
            tv_title.setText("无标题");
        }
        tv_content.setHtml(entity.getContent(), new HtmlHttpImageGetter(tv_content));
        tv_createDate.setText("发布时间：" + TimeUtil.getSlashDate(entity.getCreateTime()));
    }
}
