package com.haoyu.app.activity;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.DiscussResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.OkHttpClientManager.ResultCallback;
import com.haoyu.app.view.AppToolBar;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建讨论，修改讨论，创建评论界面
 */
public class CourseDiscussEditActivity extends BaseActivity {
    private CourseDiscussEditActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.et_content)
    EditText et_content;
    private String relationId;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_create_discuss;
    }

    @Override
    public void initView() {
        overridePendingTransition(R.anim.fade_in, 0);
        relationId = getIntent().getStringExtra("courseId");
    }

    @Override
    public void setListener() {
        et_title.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // et.getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
                Drawable drawable = et_title.getCompoundDrawables()[2];
                //如果右边没有图片，不再处理
                if (drawable == null)
                    return false;
                //如果不是按下事件，不再处理
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (event.getX() > et_title.getWidth()
                        - et_title.getPaddingRight()
                        - drawable.getIntrinsicWidth()) {
                    et_title.setSelection(et_title.getText().length());//将光标移至文字末尾
                }
                return false;
            }
        });
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                Common.hideSoftInput(context);
                String title = et_title.getText().toString().trim();
                String content = et_content.getText().toString().trim();
                if (title.length() == 0) {
                    toast(context, "请输入讨论标题");
                } else if (content.length() == 0) {
                    toast(context, "请输入讨论内容");
                } else {
                    createDiscuss();
                }
            }
        });
    }

    /**
     * 创建讨论
     */
    private void createDiscuss() {
        Map<String, String> map = new HashMap<>();
        map.put("title", et_title.getText().toString());
        map.put("content", et_content.getText().toString());
        map.put("discussionRelations[0].relation.id", relationId);
        map.put("discussionRelations[0].relation.type", "courseStudy");
        String url = Constants.OUTRT_NET + "/m/discussion";
        addSubscription(OkHttpClientManager.postAsyn(context, url, new ResultCallback<DiscussResult>() {
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
            public void onResponse(DiscussResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    DiscussEntity entity = response.getResponseData();
                    if (entity.getCreator() != null && entity.getCreator().getAvatar() == null) {
                        entity.getCreator().setAvatar(getAvatar());
                    }
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_COURSE_DISCUSSION;
                    event.obj = entity;
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    if (response != null && response.getResponseMsg() != null) {
                        toast(context, response.getResponseMsg());
                    }
                }
            }
        }, map));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out);
    }
}
