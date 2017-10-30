package com.haoyu.app.activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.haoyu.app.adapter.DictEntryAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.DictEntryMobileEntity;
import com.haoyu.app.entity.DictEntryResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.TeachingLessonData;
import com.haoyu.app.entity.TeachingLessonEntity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/2/16 on 14:08
 * 描述: 发起创课项目
 * 作者:马飞奔 Administrator
 */
public class TeachingResearchCreateCCActivity extends BaseActivity implements View.OnClickListener {
    private TeachingResearchCreateCCActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.bt_enter)
    Button bt_enter;  //进入创课规则
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.et_content)
    EditText et_content;
    @BindView(R.id.tv_stage)
    TextView tv_stage;
    @BindView(R.id.tv_subject)
    TextView tv_subject;
    private List<DictEntryMobileEntity> subjectList = new ArrayList<>(); // 学科集合
    private List<DictEntryMobileEntity> stageList = new ArrayList<>(); // 学段集合
    private boolean isInitStage, isInitSubject;
    private String id;
    private boolean alter;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teaching_research_create_cc;
    }

    @Override
    public void initView() {
        id = getIntent().getStringExtra("id");
        alter = getIntent().getBooleanExtra("alter", false);
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        et_title.setText(title);
        et_content.setText(content);
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
                if (title.length() == 0) {
                    showMaterialDialog("提示", "请输入标题");
                } else if (content.length() == 0) {
                    showMaterialDialog("提示", "请输入内容描述");
                } else {
                    if (alter)
                        alterCC(title, content);
                    else
                        createCC(title, content);
                }
            }
        });
        bt_enter.setOnClickListener(context);
        tv_stage.setOnClickListener(context);
        tv_subject.setOnClickListener(context);
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

    @Override
    public void onClick(View v) {
        Common.hideSoftInput(context);
        switch (v.getId()) {
            case R.id.bt_enter:
                startActivity(new Intent(context, TeachingResearchCCGuideActivity.class));
                break;
            case R.id.tv_stage:
                if (!isInitStage) {
                    initStage();
                } else {
                    showStageWindow();
                }
                break;
            case R.id.tv_subject:
                if (!isInitSubject) {
                    initSubject();
                } else {
                    showSubjectWindow();
                }
                break;
        }
    }

    private void initSubject() {
        String url = Constants.OUTRT_NET + "/m/textBook?textBookTypeCode=SUBJECT";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<DictEntryResult>() {

            @Override
            public void onError(Request request, Exception e) {
                isInitSubject = false;
            }

            @Override
            public void onResponse(DictEntryResult response) {
                isInitSubject = true;
                if (response != null && response.getResponseData() != null) {
                    subjectList.addAll(response.getResponseData());
                    showSubjectWindow();
                }
            }
        }));
    }

    /**
     * 访问学段条目
     */
    private void initStage() {
        String url = Constants.OUTRT_NET + "/m/textBook?textBookTypeCode=STAGE";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<DictEntryResult>() {

            @Override
            public void onError(Request request, Exception e) {
                isInitStage = false;
            }

            @Override
            public void onResponse(DictEntryResult response) {
                isInitStage = true;
                if (response != null && response.getResponseData() != null) {
                    stageList.addAll(response.getResponseData());
                    showStageWindow();
                }
            }
        }));
    }

    private String stageId, subjectId;
    private int selectStage = -1, selectSubject = -1;
    private PopupWindow stageWindwow, subjectWindow;

    private void showStageWindow() {
        if (stageWindwow != null) {
            stageWindwow.dismiss();
            stageWindwow = null;
        }
        Drawable shouqi = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(),
                shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_xiala);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(),
                zhankai.getMinimumHeight());
        tv_stage.setCompoundDrawables(null, null, shouqi, null);
        View view = View.inflate(context, R.layout.popupwindow_listview,
                null);
        ListView lv = view.findViewById(R.id.listView);
        final DictEntryAdapter adapter = new DictEntryAdapter(context, stageList, selectStage);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (stageWindwow != null) {
                    stageWindwow.dismiss();
                }
                selectStage = position;
                stageId = stageList.get(position).getTextBookValue();
                tv_stage.setText(stageList.get(position).getTextBookName());
            }
        });
        stageWindwow = new PopupWindow(view, tv_stage.getWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        stageWindwow.setFocusable(true);
        stageWindwow.setBackgroundDrawable(new BitmapDrawable());
        stageWindwow.setOutsideTouchable(true);
        stageWindwow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv_stage.setCompoundDrawables(null, null, zhankai, null);
            }
        });
        stageWindwow.showAsDropDown(tv_stage);
    }

    private void showSubjectWindow() {
        if (subjectWindow != null) {
            subjectWindow.dismiss();
            subjectWindow = null;
        }
        Drawable shouqi = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(),
                shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_xiala);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(),
                zhankai.getMinimumHeight());
        tv_subject.setCompoundDrawables(null, null, shouqi, null);
        View view = View.inflate(context, R.layout.popupwindow_listview,
                null);
        ListView lv = view.findViewById(R.id.listView);
        final DictEntryAdapter adapter = new DictEntryAdapter(context, subjectList, selectSubject);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectSubject = position;
                adapter.setSelectItem(selectSubject);
                subjectId = subjectList.get(position).getTextBookValue();
                tv_subject.setText(subjectList.get(position).getTextBookName());
                if (subjectWindow != null) {
                    subjectWindow.dismiss();
                }
            }
        });
        subjectWindow = new PopupWindow(view, tv_subject.getWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        subjectWindow.setFocusable(true);
        subjectWindow.setBackgroundDrawable(new BitmapDrawable());
        subjectWindow.setOutsideTouchable(true);
        subjectWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv_subject.setCompoundDrawables(null, null, zhankai, null);
            }
        });
        subjectWindow.showAsDropDown(tv_subject);
    }

    private void alterCC(String title, final String content) {
        String url = Constants.OUTRT_NET + "/m/lesson/cmts/" + id;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("title", title);
        map.put("content", content);
        map.put("stage", stageId);
        map.put("subject", subjectId);
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
                    event.action = Action.ALTER_GEN_CLASS;
                    Bundle bundle = new Bundle();
                    bundle.putString("title", et_title.getText().toString());
                    bundle.putString("content", et_content.getText().toString());
                    event.setBundle(bundle);
                    RxBus.getDefault().post(event);
                }
                finish();
            }
        }, map));
    }

    /*发起创课
    * title	标题	String	Y	最长128
content	内容	String	Y	最长1000
discussionRelations[0].relation.id	关联Id	String	Y	例如：
社区创课：“cmts”
discussionRelations[0].relation.type	关联类型	String	Y	例如：
社区创课：“lesson”

stage	学段	String	Y
subject	学科	String	Y

    * */
    private void createCC(String title, String content) {
        String url = Constants.OUTRT_NET + "/m/lesson/cmts";
        final Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("content", content);
        map.put("discussionRelations[0].relation.id", "cmts");
        map.put("discussionRelations[0].relation.type", "lesson");
        map.put("stage", stageId);
        map.put("subject", subjectId);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<TeachingLessonData>>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                toast(context, "与服务器连接失败");
            }

            @Override
            public void onResponse(BaseResponseResult<TeachingLessonData> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null
                        && response.getResponseData().getmLesson() != null) {
                    TeachingLessonEntity entity = response.getResponseData().getmLesson();
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
                    event.action = Action.CREATE_GEN_CLASS;
                    event.obj = entity;
                    RxBus.getDefault().post(event);
                    toastFullScreen("发表成功", true);
                    finish();
                } else
                    toastFullScreen("发表失败", false);
            }
        }, map));
    }
}
