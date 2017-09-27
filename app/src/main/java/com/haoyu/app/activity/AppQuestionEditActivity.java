package com.haoyu.app.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.FAQsAnswerEntity;
import com.haoyu.app.entity.FAQsAnswerResult;
import com.haoyu.app.entity.FAQsEntity;
import com.haoyu.app.entity.FAQsResult;
import com.haoyu.app.entity.MobileUser;
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
 * 提问问题，修改问题，回答问题界面
 */
public class AppQuestionEditActivity extends BaseActivity implements OnClickListener {
    private AppQuestionEditActivity context = this;
    private FAQsEntity faQsEntity;
    private boolean isAlter = false;    //修改问题
    private boolean isAlterAnswer = false;   //修改回答
    private boolean isAnswer = false;   //创建回答
    private String questionId, answerId;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.tv_submit)
    TextView tv_submit;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_questionType)
    TextView tv_questionType;
    @BindView(R.id.et_content)
    EditText et_content;
    @BindView(R.id.ll_tips1)
    LinearLayout ll_tips1;
    @BindView(R.id.ll_tips2)
    LinearLayout ll_tips2;
    @BindView(R.id.tv_konw)
    TextView tv_konw;
    private String relationId, relationType;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_app_question_edit;
    }

    @Override
    public void initView() {
        overridePendingTransition(R.anim.fade_in, 0);
        relationId = getIntent().getStringExtra("relationId");
        relationType = getIntent().getStringExtra("relationType");
        isAnswer = getIntent().getBooleanExtra("isAnswer", false);
        isAlter = getIntent().getBooleanExtra("isAlter", false);
        isAlterAnswer = getIntent().getBooleanExtra("isAlterAnswer", false);
        if (isAnswer) {
            tv_title.setText("回答");
            tv_questionType.setText("我来回答");
            faQsEntity = (FAQsEntity) getIntent().getSerializableExtra("entity");
            questionId = faQsEntity.getId();
            et_content.setHint("回答内容");
        } else if (isAlter) {
            tv_title.setText("修改问题");
            faQsEntity = (FAQsEntity) getIntent().getSerializableExtra("entity");
            questionId = faQsEntity.getId();
            et_content.setHint("修改内容");
            et_content.setText(faQsEntity.getContent());
            Editable editable = et_content.getText();
            Selection.setSelection(editable, editable.length());
        } else if (isAlterAnswer) {
            tv_title.setText("修改回答");
            answerId = getIntent().getStringExtra("answerId");
            String content = getIntent().getStringExtra("content");
            et_content.setHint("回答内容");
            et_content.setText(content);
            Editable editable = et_content.getText();
            Selection.setSelection(editable, editable.length());
        } else {
            tv_title.setText("提问");
            if (relationType != null && relationType.equals("workshop_question")) {
                ll_tips1.setVisibility(View.VISIBLE);
                ll_tips2.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        tv_submit.setOnClickListener(context);
        ll_tips1.setOnClickListener(context);
        tv_konw.setOnClickListener(context);
        et_content.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.toString().trim().length() > 0) {
                    tv_submit.setEnabled(true);
                } else {
                    tv_submit.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_tips1:
                ll_tips2.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_konw:
                ll_tips2.setVisibility(View.GONE);
                break;
            case R.id.tv_submit:
                Common.hideSoftInput(context);
                String content = et_content.getText().toString().trim();
                if (isAnswer) {
                    if (content.length() == 0)
                        showMaterialDialog("提示", "请输入您的答案！");
                    else
                        answer();
                } else if (isAlter) {
                    if (content.length() == 0)
                        showMaterialDialog("提示", "请输入问题内容！");
                    else
                        alter();
                } else if (isAlterAnswer) {
                    if (content.length() == 0)
                        showMaterialDialog("提示", "请输入您的答案！");
                    else
                        alterAnswer();
                } else {
                    if (content.length() == 0)
                        showMaterialDialog("提示", "请输入问题内容！");
                    else
                        question();
                }
                break;
        }
    }

    /**
     * 修改问题
     */
    private void alter() {
        String url = Constants.OUTRT_NET + "/m/faq_question/" + questionId;
        Map<String, String> map = new HashMap<>();
        map.put("content", et_content.getText().toString());
        map.put("_method", "put");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new ResultCallback<FAQsResult>() {
            public void onError(Request request, Exception e) {
                onNetWorkError(context);
            }

            public void onResponse(FAQsResult response) {
                if (response != null && response.getResponseData() != null) {
                    MessageEvent event = new MessageEvent();
                    event.action = Action.ALTER_FAQ_QUESTION;
                    event.obj = response.getResponseData();
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

    /**
     * 创建回答
     */
    private void answer() {
        Map<String, String> map = new HashMap<>();
        map.put("questionId", questionId);
        map.put("content", et_content.getText().toString());
        String url = Constants.OUTRT_NET + "/m/faq_answer";
        addSubscription(OkHttpClientManager.postAsyn(context, url, new ResultCallback<FAQsAnswerResult>() {
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
            public void onResponse(FAQsAnswerResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    FAQsAnswerEntity entity = response.getResponseData();
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
                    event.action = Action.CREATE_FAQ_ANSWER;
                    event.obj = faQsEntity;
                    RxBus.getDefault().post(event);
                    Intent intent = new Intent();
                    intent.putExtra("entity", entity);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    if (response != null && response.getResponseMsg() != null) {
                        toast(context, response.getResponseMsg());
                    }
                }
            }
        }, map));
    }

    /**
     * 修改回答
     */
    private void alterAnswer() {
        String url = Constants.OUTRT_NET + "/m/faq_answer/" + answerId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("content", et_content.getText().toString());
        addSubscription(OkHttpClientManager.postAsyn(context, url, new ResultCallback<BaseResponseResult>() {
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
                    Intent intent = new Intent();
                    intent.putExtra("content", et_content.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    if (response != null && response.getResponseMsg() != null) {
                        toast(context, response.getResponseMsg());
                    }
                }
            }
        }, map));
    }

    /**
     * 创建问题
     */
    private void question() {
        Map<String, String> map = new HashMap<>();
        map.put("relation.id", relationId);
        map.put("relation.type", relationType);
        map.put("content", et_content.getText().toString());
        addSubscription(OkHttpClientManager.postAsyn(context, Constants.OUTRT_NET + "/m/faq_question", new ResultCallback<FAQsResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                toastFullScreen("问题发布失败！请检查网络。", false);
                finish();
            }

            @Override
            public void onResponse(FAQsResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    FAQsEntity entity = response.getResponseData();
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
                    event.action = Action.CREATE_FAQ_QUESTION;
                    event.obj = entity;
                    RxBus.getDefault().post(event);
                    toastFullScreen("问题发布成功！", true);
                    finish();
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
