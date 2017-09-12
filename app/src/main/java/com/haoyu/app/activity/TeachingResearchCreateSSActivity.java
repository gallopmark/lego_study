package com.haoyu.app.activity;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.DiscussResult;
import com.haoyu.app.entity.MobileUser;
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

/**
 * 创建日期：2017/1/10 on 13:45
 * 描述: 创建研说
 * 作者:马飞奔 Administrator
 */
public class TeachingResearchCreateSSActivity extends BaseActivity {
    private TeachingResearchCreateSSActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.et_content)
    EditText et_content;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teaching_research_create_ss;
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
                String title = et_title.getText().toString().trim();
                String content = et_content.getText().toString().trim();
                if (title.length() == 0)
                    showMaterialDialog("提示", "请输入标题");
                else if (content.length() == 0)
                    showMaterialDialog("提示", "请输入描述内容");
                else
                    commit(title, content);
            }
        });
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
    }

    /*创建研说*/
    private void commit(String title, final String content) {
        showTipDialog();
        String url = Constants.OUTRT_NET + "/m/discussion/cmts";
        Map<String, String> map = new HashMap<>();
        map.put("discussionRelations[0].relation.id", "cmts");
        map.put("discussionRelations[0].relation.type", "discussion");
        map.put("title", title);
        map.put("content", content);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<DiscussResult>() {
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
                    if (entity.getCreator() == null) {
                        MobileUser creator = new MobileUser();
                        creator.setId(getUserId());
                        creator.setAvatar(getAvatar());
                        creator.setRealName(getRealName());
                        entity.setCreator(creator);
                    } else {
                        if (entity.getCreator().getId() == null || (entity.getCreator().getId() != null && entity.getCreator().getId().toLowerCase().equals("null")))
                            entity.getCreator().setId(getUserId());
                        if (entity.getCreator().getAvatar() == null || (entity.getCreator().getAvatar() != null && entity.getCreator().getAvatar().toLowerCase().equals("null")))
                            entity.getCreator().setAvatar(getAvatar());
                        if (entity.getCreator().getRealName() == null || (entity.getCreator().getRealName() != null && entity.getCreator().getRealName().toLowerCase().equals("null")))
                            entity.getCreator().setRealName(getRealName());
                    }
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_STUDY_SAYS;
                    event.obj = entity;
                    RxBus.getDefault().post(event);
                    toastFullScreen("发表成功", true);
                    finish();
                } else {
                    onNetWorkError(context);
                }
            }
        }, map));
    }
}
