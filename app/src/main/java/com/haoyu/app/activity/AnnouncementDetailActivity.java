package com.haoyu.app.activity;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.AnnouncementEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/1/5 on 17:16
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AnnouncementDetailActivity extends BaseActivity {
    private AnnouncementDetailActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_createDate)
    TextView tv_createDate;
    @BindDrawable(R.drawable.announcements_createdate)
    Drawable createTime;
    @BindDimen(R.dimen.margin_size_4)
    int paddingSize;
    @BindView(R.id.tv_content)
    HtmlTextView tv_content;
    private String noticeId;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_announcement_detail;
    }

    @Override
    public void initView() {
        noticeId = getIntent().getStringExtra("relationId");
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

    /*获取通知公告详细*/
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/announcement/view/" + noticeId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<AnnouncementEntity>>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult<AnnouncementEntity> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    setResult(RESULT_OK);
                    updateUI(response.getResponseData());
                }
            }
        }));
    }

    private void updateUI(AnnouncementEntity entity) {
        tv_title.setText(entity.getTitle());
/// 这一步必须要做,否则不会显示.
        createTime.setBounds(0, 0, createTime.getMinimumWidth(), createTime.getMinimumHeight());
        tv_createDate.setCompoundDrawables(createTime, null, null, null);
        tv_createDate.setCompoundDrawablePadding(paddingSize);
        tv_createDate.setText(TimeUtil.getDateHR(entity.getCreateTime()));
        tv_content.setHtml(entity.getContent(), new HtmlHttpImageGetter(tv_content, Constants.REFERER));
    }
}
