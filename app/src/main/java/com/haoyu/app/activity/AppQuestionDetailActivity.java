package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.haoyu.app.adapter.AppAnswerListAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.FAQsAnswerEntity;
import com.haoyu.app.entity.FAQsAnswerListResult;
import com.haoyu.app.entity.FAQsEntity;
import com.haoyu.app.entity.FollowMobileEntity;
import com.haoyu.app.entity.FollowMobileResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.OkHttpClientManager.ResultCallback;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.GoodView;
import com.haoyu.app.view.RippleView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 问答详情页（包括课程学习的问答和工作坊的问答）
 */
public class AppQuestionDetailActivity extends BaseActivity implements
        OnClickListener, XRecyclerView.LoadingListener {
    private AppAnswerListAdapter adapter; // 答案列表适配器
    private ImageView answer_userIco; // 最佳答案用户头像
    private AppQuestionDetailActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_answer)
    TextView tv_answer;
    private FAQsEntity faQsEntity; // 问答类对象
    private View headerView; // 头部布局
    private ImageView ic_question; // 问题左边图标
    private boolean isLoadMore = false;
    private boolean isRefresh = false;
    private List<FAQsAnswerEntity> answerList = new ArrayList<>(); // 答案集合
    private TextView emptyAnswer;
    private String orders = "CREATE_TIME.ASC"; // 按创建时间，创建最早时间排前
    private int page = 1;
    private String questionId, type;
    private TextView tv_answer_date;
    private TextView tv_answer_userName;
    private TextView tv_bestAnswer_content;
    private TextView tv_isCollected;
    private ImageView iv_isCollected;
    private TextView tv_question_content;
    private TextView tv_userName;
    private ImageView userIco;
    private final int answerCode = 10, alterCode = 11;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_answer_details;
    }

    @Override
    public void initView() {
        type = getIntent().getStringExtra("type");
        faQsEntity = ((FAQsEntity) getIntent().getSerializableExtra("entity"));
        questionId = faQsEntity.getId();
        headerView = getLayoutInflater().inflate(
                R.layout.wenda_listview_head_view, null, false);
        userIco = getView(headerView, R.id.userIco);
        tv_userName = getView(headerView, R.id.tv_userName);
        tv_isCollected = getView(headerView, R.id.tv_guanzhu);
        iv_isCollected = getView(headerView, R.id.iv_guanzhu);
        ic_question = getView(headerView, R.id.ic_question);
        tv_question_content = getView(headerView, R.id.tv_question);
        answer_userIco = getView(headerView, R.id.answer_userIco);
        tv_answer_userName = getView(headerView, R.id.tv_answer_userName);
        tv_bestAnswer_content = getView(headerView, R.id.tv_bestAnswer);
        tv_answer_date = getView(headerView, R.id.answer_date);
        emptyAnswer = getView(headerView, R.id.emptyAnswer);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,
                (int) (0.22D * tv_question_content.getLineHeight()), 0, 0);
        ic_question.setLayoutParams(params);
        adapter = new AppAnswerListAdapter(context, answerList, getUserId());
        xRecyclerView.addHeaderView(headerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(this);
        showData();
    }

    /**
     * 显示从列表中传递过来的数据
     */
    public void showData() {
        MobileUser mobileUser = faQsEntity.getCreator();
        if (mobileUser != null && mobileUser.getId() != null && mobileUser.getId().equals(getUserId())) {
            toolBar.setShow_right_button(true);
        }
        if (mobileUser != null && mobileUser.getAvatar() != null) {
            GlideImgManager.loadCircleImage(context, mobileUser.getAvatar()
                    , R.drawable.user_default, R.drawable.user_default, userIco);
        } else {
            userIco.setImageResource(R.drawable.user_default);
        }
        if (mobileUser != null && mobileUser.getRealName() != null) {
            tv_userName.setText(mobileUser.getRealName());
        } else {
            tv_userName.setText("匿名用户");
        }
        tv_question_content.setText(faQsEntity.getContent());
        if (type.equals("course")) {
            tv_isCollected.setVisibility(View.VISIBLE);
        } else {
            iv_isCollected.setVisibility(View.VISIBLE);
        }
        if (faQsEntity.getFollow() != null) {
            tv_isCollected.setText("取消收藏");
            iv_isCollected.setImageResource(R.drawable.workshop_collect_press);
        } else {
            tv_isCollected.setText("收藏");
            iv_isCollected.setImageResource(R.drawable.workshop_collect_default);
        }
    }

    /**
     * 获取答案列表
     */
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/faq_answer" + "?questionId="
                + questionId + "&page=" + page + "&orders=" + orders;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new ResultCallback<FAQsAnswerListResult>() {
            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
                if (!isRefresh && !isLoadMore) {
                    showTipDialog();
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    page -= 1;
                    xRecyclerView.loadMoreComplete(false);
                }
            }

            @Override
            public void onResponse(FAQsAnswerListResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null && response.getResponseData().getAnswers() != null && response.getResponseData().getAnswers().size() > 0) {
                    showData(response.getResponseData().getAnswers(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    } else {
                        xRecyclerView.setLoadingMoreEnabled(false);
                        emptyAnswer.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    /**
     * 显示答案列表
     */
    private void showData(List<FAQsAnswerEntity> answers, Paginator paginator) {
        if (isRefresh) {
            xRecyclerView.refreshComplete(true);
            answerList.clear();
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        answerList.addAll(answers);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
        FAQsAnswerEntity bestAnswer = answerList.get(0);
        MobileUser mobileUser = bestAnswer.getCreator();
        if (mobileUser.getAvatar() != null) {
            GlideImgManager.loadCircleImage(context, mobileUser.getAvatar(),
                    R.drawable.user_default, R.drawable.user_default, answer_userIco);
        } else {
            answer_userIco.setImageResource(R.drawable.user_default);
        }
        if (mobileUser.getRealName() != null) {
            tv_answer_userName.setText(mobileUser.getRealName());
        } else {
            tv_answer_userName.setText("匿名用户");
        }
        tv_bestAnswer_content.setText(Html
                .fromHtml("<font color='#01AE37'>[已采纳的答案] </font>"
                        + bestAnswer.getContent()));
        if (bestAnswer.getCreateTime() != null) {
            tv_answer_date.setText(TimeUtil.getSlashDate(bestAnswer
                    .getCreateTime()));
        }
    }

    private int alterPosition;

    @Override
    public void setListener() {
       toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
           @Override
           public void onLeftClick(View view) {
               finish();
           }

           @Override
           public void onRightClick(View view) {
               bottomDialog();
           }
       });
        tv_answer.setOnClickListener(context);
        tv_isCollected.setOnClickListener(context);
        iv_isCollected.setOnClickListener(context);
        adapter.setDisposeCallBack(new AppAnswerListAdapter.ItemDisposeCallBack() {
            @Override
            public void onAlter(FAQsAnswerEntity entity, int position) {   //修改回答
                alterPosition = position;
                Intent intent = new Intent();
                intent.setClass(context, AppQuestionEditActivity.class);
                intent.putExtra("isAlterAnswer", true);
                intent.putExtra("answerId", entity.getId());
                intent.putExtra("content", entity.getContent());
                startActivityForResult(intent, alterCode);
            }

            @Override
            public void onDelete(FAQsAnswerEntity entity, int position) {  //删除回答
                deleteAnswer(position);
            }
        });
    }


    /**
     * 删除回答
     *
     * @param position
     */
    private void deleteAnswer(final int position) {
        String url = Constants.OUTRT_NET + "/m/faq_answer/" + answerList.get(position).getId();
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            public void onError(Request request, Exception exception) {
                hideTipDialog();
                onNetWorkError(context);
            }

            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    answerList.remove(position);
                    adapter.notifyDataSetChanged();
                    if (answerList.size() <= 0) {
                        emptyAnswer.setVisibility(View.VISIBLE);
                    }
                }
            }
        }, map));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_guanzhu:
                if (faQsEntity.getFollow() != null) {
                    cancelCollection();
                } else {
                    collection();
                }
                break;
            case R.id.iv_guanzhu:
                if (faQsEntity.getFollow() != null) {
                    cancelCollection();
                } else {
                    collection();
                }
                break;
            case R.id.tv_answer:
                Intent intent = new Intent();
                intent.setClass(context, AppQuestionEditActivity.class);
                intent.putExtra("isAnswer", true);
                intent.putExtra("questionId", questionId);
                startActivityForResult(intent, answerCode);
                break;
        }

    }

    private void bottomDialog() {
        View view = getLayoutInflater().inflate(
                R.layout.dialog_discussion_delete, null);
        final AlertDialog bottomDialog = new AlertDialog.Builder(context).create();
        RippleView rv_delete = view.findViewById(R.id.rv_delete);
        RippleView rv_cancel = view.findViewById(R.id.rv_cancel);
        rv_delete.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            public void onComplete(RippleView rippleView) {
                bottomDialog.dismiss();
                deleteQuestion();
            }
        });
        rv_cancel.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            public void onComplete(RippleView rippleView) {
                bottomDialog.dismiss();
            }
        });
        bottomDialog.show();
        bottomDialog.getWindow().setLayout(
                ScreenUtils.getScreenWidth(context),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomDialog.getWindow().setWindowAnimations(R.style.dialog_anim);
        bottomDialog.getWindow().setContentView(view);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void deleteQuestion() {
        String url = Constants.OUTRT_NET + "/m/faq_question/" + faQsEntity.getId();
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
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
                if (response != null && response.getResponseCode() != null
                        && response.getResponseCode().equals("00")) {
                    MessageEvent event = new MessageEvent();
                    event.action = Action.DELETE_COURSE_QUESTION;
                    event.obj = faQsEntity;
                    RxBus.getDefault().post(event);
                    toastFullScreen("成功删除，返回上一级", true);
                    finish();
                }
            }
        }, map));
    }

    /**
     * 取消收藏
     */
    private void cancelCollection() {
        FollowMobileEntity entity = faQsEntity.getFollow();
        if (entity != null) {
            String url = Constants.OUTRT_NET + "/m/follow/" + entity.getId();
            Map<String, String> map = new HashMap<>();
            map.put("_method", "delete");
            addSubscription(OkHttpClientManager.postAsyn(context, url, new ResultCallback<BaseResponseResult>() {
                public void onError(Request request, Exception exception) {
                    onNetWorkError(context);
                }

                public void onResponse(BaseResponseResult response) {
                    if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                        if (iv_isCollected.getVisibility() == View.VISIBLE) {
                            GoodView goodView = new GoodView(context);
                            goodView.setImage(R.drawable.workshop_collect_default);
                            goodView.show(iv_isCollected);
                            goodView.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                @Override
                                public void onDismiss() {
                                    iv_isCollected.setImageResource(R.drawable.workshop_collect_default);
                                }
                            });
                        } else {
                            tv_isCollected.setText("收藏");
                        }
                        faQsEntity.setFollow(null);
                        MessageEvent event = new MessageEvent();
                        event.action = Action.COLLECTION;
                        event.obj = faQsEntity;
                        RxBus.getDefault().post(event);
                    }
                }
            }, map));
        }
    }

    /**
     * 创建收藏
     */
    private void collection() {
        String url = Constants.OUTRT_NET + "/m/follow";
        Map<String, String> map = new HashMap<>();
        map.put("followEntity.id", faQsEntity.getId());
        map.put("followEntity.type", "course_study_question");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new ResultCallback<FollowMobileResult>() {
            public void onError(Request request, Exception exception) {
            }

            public void onResponse(FollowMobileResult response) {
                if (response.getResponseCode().equals("00")) {
                    if (iv_isCollected.getVisibility() == View.VISIBLE) {
                        GoodView goodView = new GoodView(context);
                        goodView.setImage(R.drawable.workshop_collect_press);
                        goodView.show(iv_isCollected);
                        goodView.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                iv_isCollected.setImageResource(R.drawable.workshop_collect_press);
                            }
                        });
                    } else {
                        tv_isCollected.setText("取消收藏");
                    }
                    faQsEntity.setFollow(response.getResponseData());
                    MessageEvent event = new MessageEvent();
                    event.action = Action.COLLECTION;
                    event.obj = faQsEntity;
                    RxBus.getDefault().post(event);
                }
            }
        }, map));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case answerCode:
                if (resultCode == RESULT_OK && data != null) {
                    FAQsAnswerEntity entity = (FAQsAnswerEntity) data.getSerializableExtra("entity");
                    if (emptyAnswer.getVisibility() == View.VISIBLE) {
                        emptyAnswer.setVisibility(View.GONE);
                    }
                    if (!xRecyclerView.isLoadingMoreEnabled()) {
                        answerList.add(entity);
                        adapter.notifyDataSetChanged();
                    } else {
                        toastFullScreen("发表回答成功", true);
                    }
                }
                break;
            case alterCode:
                if (resultCode == RESULT_OK && data != null) {
                    String content = data.getStringExtra("content");
                    answerList.get(alterPosition).setContent(content);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onRefresh() {
        isRefresh = true;
        isLoadMore = false;
        page = 1;
        initData();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        page += 1;
        initData();
    }
}
