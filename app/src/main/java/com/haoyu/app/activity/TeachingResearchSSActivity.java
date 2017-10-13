package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.AppDiscussionAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.DiscussResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.ReplyEntity;
import com.haoyu.app.entity.ReplyListResult;
import com.haoyu.app.entity.ReplyResult;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.GoodView;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

/**
 * 创建日期：2017/1/10 on 14:25
 * 描述: 教研话题详情
 * 作者:马飞奔 Administrator
 */
public class TeachingResearchSSActivity extends BaseActivity implements View.OnClickListener {
    private TeachingResearchSSActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.empty_detail)
    TextView empty_detail;
    @BindView(R.id.detailLayout)
    LinearLayout detailLayout;   //研说详情布局
    @BindView(R.id.loadFailView1)
    LoadFailView loadFailView1;  //评论区加载失败布局
    @BindView(R.id.tv_trTitle)
    TextView tv_trTitle;  //研说标题
    @BindView(R.id.iv_userIco)
    ImageView iv_userIco;  //研说创建人头像
    @BindView(R.id.tv_userName)
    TextView tv_userName; //研说创建人名字
    @BindView(R.id.tv_createTime)
    TextView tv_createTime;  //研说创建时间
    @BindView(R.id.tv_viewNum)
    TextView tv_viewNum; //浏览人数
    @BindView(R.id.tv_content)
    HtmlTextView tv_content;  //研说内容
    @BindView(R.id.mFileImg)
    ImageView mFileImg;
    @BindView(R.id.bt_support)
    Button bt_support;  //点赞按钮
    @BindView(R.id.tv_commentNum)
    TextView tv_commentNum;  //评论条数
    @BindView(R.id.replyRV)
    RecyclerView replyRV;  //评论列表
    @BindView(R.id.tv_more_reply)
    View tv_more_reply;
    @BindView(R.id.empty_comment)
    LinearLayout empty_comment;
    @BindView(R.id.tv_comment)
    TextView tv_comment;
    private List<ReplyEntity> replyList = new ArrayList<>();
    private AppDiscussionAdapter replyAdapter;
    @BindView(R.id.bottomView)
    View bottomView;  //底部评论布局
    private String relationId, uuid;  //研说id,研说关系Id
    private int supportNum, replyNum;  //点赞数,评论数
    private int page = 1;
    private String creatorId;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teaching_research_ss;
    }

    @Override
    public void initView() {
        relationId = getIntent().getStringExtra("id");
        uuid = getIntent().getStringExtra("uuid");
        replyAdapter = new AppDiscussionAdapter(context, replyList, getUserId());
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        replyRV.setNestedScrollingEnabled(false);
        replyRV.setLayoutManager(layoutManager);
        replyRV.setAdapter(replyAdapter);
        registRxBus();
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
                    showData(discussResult.getResponseData());
                    getReply();
                } else {
                    empty_detail.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    /*显示详情*/
    private void showData(DiscussEntity entity) {
        if (entity.getCreator() != null) {
            creatorId = entity.getCreator().getId();
            GlideImgManager.loadCircleImage(context, entity.getCreator().getAvatar(),
                    R.drawable.user_default, R.drawable.user_default, iv_userIco);
            tv_userName.setText(entity.getCreator().getRealName());
        }
        tv_createTime.setText("发布于" + TimeUtil.getSlashDate(entity.getCreateTime()));
        if (entity.getmDiscussionRelations() != null && entity.getmDiscussionRelations().size() > 0) {
            supportNum = entity.getmDiscussionRelations().get(0).getSupportNum();
            replyNum = entity.getmDiscussionRelations().get(0).getReplyNum();
            tv_viewNum.setText(String.valueOf(entity.getmDiscussionRelations().get(0).getBrowseNum()));
            bt_support.setText("赞(" + supportNum + ")");
            tv_commentNum.setText("共有" + replyNum + "条评论");
        }
        tv_trTitle.setText(entity.getTitle());
        tv_content.setHtml(entity.getContent(), new HtmlHttpImageGetter(tv_content, Constants.REFERER));
        if (entity.getmFileInfos() != null && entity.getmFileInfos().size() > 0) {
            GlideImgManager.loadImage(context, entity.getmFileInfos().get(0).getUrl(),
                    R.drawable.app_default, R.drawable.app_default, mFileImg);
        }
        detailLayout.setVisibility(View.VISIBLE);
    }

    private void getReply() {
        tv_more_reply.setVisibility(View.GONE);
        String url = Constants.OUTRT_NET + "/m/discussion/post?discussionUser.discussionRelation.id=" + uuid
                + "&page=" + page + "&orders=CREATE_TIME.ASC" + "&limit=5";
        addSubscription(Flowable.just(url).map(new Function<String, ReplyListResult>() {
            @Override
            public ReplyListResult apply(String url) throws Exception {
                return getMainReply(url);
            }
        }).map(new Function<ReplyListResult, ReplyListResult>() {
            @Override
            public ReplyListResult apply(ReplyListResult result) throws Exception {
                return getChildReply(result);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ReplyListResult>() {
                    @Override
                    public void accept(ReplyListResult response) throws Exception {
                        if (response != null && response.getResponseData() != null
                                && response.getResponseData().getmDiscussionPosts() != null) {
                            updateUI(response.getResponseData().getmDiscussionPosts(), response.getResponseData().getPaginator());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        loadFailView1.setErrorMsg("加载评论失败，请点击重试！");
                        loadFailView1.setVisibility(View.VISIBLE);
                    }
                }));
    }

    /*获取主回复*/
    private ReplyListResult getMainReply(String url) throws Exception {
        String listStr = OkHttpClientManager.getAsString(context, url);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(listStr, ReplyListResult.class);
    }

    /*通过主回复id获取子回复*/
    private ReplyListResult getChildReply(ReplyListResult result) {
        if (result != null && result.getResponseData() !=
                null && result.getResponseData().getmDiscussionPosts() != null) {
            for (int i = 0; i < result.getResponseData().getmDiscussionPosts().size(); i++) {
                String mainPostId = result.getResponseData().getmDiscussionPosts().get(i).getId();
                String url = Constants.OUTRT_NET + "/m/discussion/post?discussionUser.discussionRelation.id=" + uuid + "&mainPostId=" + mainPostId
                        + "&orders=CREATE_TIME.ASC";
                try {
                    ReplyListResult mResult = getMainReply(url);
                    if (mResult.getResponseData() != null) {
                        result.getResponseData().getmDiscussionPosts().get(i).setChildReplyEntityList(mResult.getResponseData().getmDiscussionPosts());
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return result;
    }

    /*显示评论列表*/
    private void updateUI(List<ReplyEntity> mainList, Paginator paginator) {
        replyList.clear();
        if (mainList.size() > 0) {
            replyList.addAll(mainList);
            replyAdapter.notifyDataSetChanged();
            if (paginator != null && paginator.getHasNextPage()) {
                tv_more_reply.setVisibility(View.VISIBLE);
            }
            replyRV.setVisibility(View.VISIBLE);
        } else {
            replyRV.setVisibility(View.GONE);
            empty_comment.setVisibility(View.VISIBLE);
        }
        bottomView.setVisibility(View.VISIBLE);
    }

    private int childPosition, replyPosition;

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
        bt_support.setOnClickListener(context);
        bottomView.setOnClickListener(context);
        tv_comment.setOnClickListener(context);
        tv_more_reply.setOnClickListener(context);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        loadFailView1.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getReply();
            }
        });
        replyAdapter.setOnPostClickListener(new AppDiscussionAdapter.OnPostClickListener() {
            @Override
            public void onTargetClick(View view, final int position, final ReplyEntity entity) {

            }

            @Override
            public void onChildClick(View view, final int position) {
                childPosition = position;
                showCommentDialog(true);
            }
        });

        replyAdapter.setMoreReplyCallBack(new AppDiscussionAdapter.MoreReplyCallBack() {
            @Override
            public void callBack(ReplyEntity entity, int position) {
                replyPosition = position;
                Intent intent = new Intent(context, AppMoreChildReplyActivity.class);
                intent.putExtra("entity", entity);
                intent.putExtra("relationId", uuid);
                startActivity(intent);
            }
        });

        replyAdapter.setSupportCallBack(new AppDiscussionAdapter.SupportCallBack() {
            @Override
            public void support(int position, TextView tv_like) {
                createLike(position);
            }
        });

        replyAdapter.setDeleteMainReply(new AppDiscussionAdapter.DeleteMainReply() {
            @Override
            public void deleteMainReply(String id, int position) {
                deleteReply(id, position);
            }
        });
    }

    private void showCommentDialog(final boolean sendChild) {
        CommentDialog dialog = new CommentDialog(context, "请输入评论内容");
        dialog.show();
        dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
            @Override
            public void sendComment(String content) {
                if (sendChild) {
                    sendChildReply(childPosition, content);
                } else {
                    sendMainReply(content);
                }
            }
        });
    }

    /*创建子回复*/
    private void sendChildReply(final int position, final String content) {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("mainPostId", replyList.get(position).getId());
        map.put("discussionUser.discussionRelation.id", uuid);
        String url = Constants.OUTRT_NET + "/m/discussion/post";
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<ReplyResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            public void onError(Request request, Exception exception) {
                hideTipDialog();
                onNetWorkError(context);
            }

            public void onResponse(ReplyResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    ReplyEntity entity = response.getResponseData();
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
                    int childPostCount = replyList.get(position).getChildPostCount() + 1;
                    replyList.get(position).setChildPostCount(childPostCount);
                    if (replyList.get(position).getChildReplyEntityList() != null && replyList.get(position).getChildReplyEntityList().size() < 10) {
                        replyList.get(position).getChildReplyEntityList().add(entity);
                    } else {
                        toastFullScreen("评论成功", true);
                    }
                    replyAdapter.notifyDataSetChanged();
                } else {
                    toastFullScreen("评论失败", false);
                }
            }
        }, map);
    }

    /*创建主回复*/
    private void sendMainReply(String content) {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("discussionUser.discussionRelation.id", uuid);
        String url = Constants.OUTRT_NET + "/m/discussion/post";
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<ReplyResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(ReplyResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    replyRV.setVisibility(View.VISIBLE);
                    empty_comment.setVisibility(View.GONE);
                    if (replyList.size() < 5) {
                        ReplyEntity entity = response.getResponseData();
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
                        replyList.add(entity);
                        replyAdapter.notifyDataSetChanged();
                    } else {
                        tv_more_reply.setVisibility(View.VISIBLE);
                        toastFullScreen("评论成功", true);
                    }
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_MAIN_REPLY;
                    RxBus.getDefault().post(event);
                    replyNum++;
                    tv_commentNum.setText("共有" + replyNum + "条评论");
                } else {
                    toastFullScreen("评论失败", false);
                }
            }
        }, map));
    }

    /**
     * 创建评论列表点赞观点(点赞)
     *
     * @param position
     */
    private void createLike(final int position) {
        String url = Constants.OUTRT_NET + "/m/attitude";
        String relationId = replyList.get(position).getId();
        HashMap<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", relationId);
        map.put("relation.type", "discussion_post");
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            public void onError(Request request, Exception exception) {
                onNetWorkError(context);
            }

            public void onResponse(AttitudeMobileResult response) {
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    int count = replyList.get(position).getSupportNum() + 1;
                    replyList.get(position).setSupportNum(count);
                    replyAdapter.notifyDataSetChanged();
                } else if (response != null && response.getResponseMsg() != null) {
                    toast(context, "您已点赞过");
                } else {
                    toast(context, "点赞失败");
                }
            }
        }, map);
    }

    private void deleteReply(String id, final int position) {
        String url = Constants.OUTRT_NET + "/m/discussion/post/" + id;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        map.put("discussionUser.discussionRelation.id", relationId);
        map.put("mainPostId", id);
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
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
                    replyList.remove(position);
                    replyAdapter.notifyDataSetChanged();
                    if (replyList.size() == 0) {
                        replyRV.setVisibility(View.GONE);
                        empty_comment.setVisibility(View.VISIBLE);
                    }
                    replyNum--;
                    tv_commentNum.setText("共有" + replyNum + "条评论");
                    getReply();
                }
            }
        }, map);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_support:
                createLike();
                break;
            case R.id.tv_comment:
                showCommentDialog(false);
                break;
            case R.id.bottomView:
                showCommentDialog(false);
                break;
            case R.id.tv_more_reply:
                Intent intent = new Intent(context, AppMoreMainReplyActivity.class);
                intent.putExtra("type", "comment");
                intent.putExtra("relationId", uuid);
                startActivity(intent);
                break;
        }
    }

    private void showBottomDialog() {
        View view = getLayoutInflater().inflate(
                R.layout.dialog_teaching_say, null);
        final AlertDialog bottomDialog = new AlertDialog.Builder(context).create();
        Button bt_share = view.findViewById(R.id.bt_share);
        Button bt_delete = view.findViewById(R.id.bt_delete);
        if (creatorId != null && creatorId.equals(getUserId())) {
            bt_delete.setVisibility(View.VISIBLE);
        } else {
            bt_delete.setVisibility(View.GONE);
        }
        bt_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomDialog != null) {
                    bottomDialog.dismiss();
                }
                toast(context, "暂不支持");
            }
        });
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomDialog != null) {
                    bottomDialog.dismiss();
                }
                showTipsDialog();
            }
        });
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.setCancelable(true);
        bottomDialog.show();
        Window window = bottomDialog.getWindow();
        window.setLayout(ScreenUtils.getScreenWidth(context),
                LinearLayout.LayoutParams.WRAP_CONTENT);
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

    /* 创建观点（点赞） */
    private void createLike() {
        String url = Constants.OUTRT_NET + "/m/attitude";
        Map<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", relationId);
        map.put("relation.type", "discussion");
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
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
            public void onResponse(AttitudeMobileResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode().equals("00")) {
                    int supportNum = context.supportNum + 1;
                    bt_support.setText("赞(" + supportNum + ")");
                    GoodView goodView = new GoodView(context);
                    int defaultColor = ContextCompat.getColor(context, R.color.defaultColor);
                    goodView.setTextInfo("+1", defaultColor, 16);
                    goodView.show(bt_support);
                    MessageEvent event = new MessageEvent();
                    event.action = Action.SUPPORT_STUDY_SAYS;
                    event.arg1 = supportNum;
                    RxBus.getDefault().post(event);
                } else {
                    if (response != null && response.getResponseMsg() != null) {
                        toast(context, "您已点赞过");
                    }
                }
            }
        }, map);
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.CREATE_MAIN_REPLY) && event.obj != null && event.obj instanceof ReplyEntity) {
            replyNum++;
            tv_commentNum.setText("共有" + replyNum + "条评论");
            ReplyEntity entity = (ReplyEntity) event.obj;
            if (replyList.size() < 5) {
                replyList.add(entity);
                replyAdapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.CREATE_CHILD_REPLY)) {
            /*来自更多评论列表界面创建子回复*/
            if (event.getBundle() != null && event.getBundle().getSerializable("mainReply") != null
                    && event.getBundle().getSerializable("mainReply") instanceof ReplyEntity) {
                ReplyEntity mainReply = (ReplyEntity) event.getBundle().getSerializable("mainReply");
                int position = replyList.indexOf(mainReply);
                if (position != -1 && event.getBundle().getSerializable("childReply") != null
                        && event.getBundle().getSerializable("childReply") instanceof ReplyEntity) {
                    ReplyEntity childReply = (ReplyEntity) event.getBundle().getSerializable("childReply");
                    int childPostNum = replyList.get(position).getChildPostCount() + 1;
                    replyList.get(position).setChildPostCount(childPostNum);
                    if (replyList.get(position).getChildReplyEntityList() != null && replyList.get(position).getChildReplyEntityList().size() < 10) {
                        replyList.get(position).getChildReplyEntityList().add(childReply);
                    }
                    replyAdapter.notifyDataSetChanged();
                }
            } else {   //来自更多回复列表创建回复
                int childPostNum = replyList.get(replyPosition).getChildPostCount() + 1;
                replyList.get(replyPosition).setChildPostCount(childPostNum);
                replyAdapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.CREATE_LIKE)) {
            if (event.obj != null && event.obj instanceof ReplyEntity) {
                ReplyEntity entity = (ReplyEntity) event.obj;
                if (replyList.indexOf(entity) != -1) {
                    replyList.set(replyList.indexOf(entity), entity);
                    replyAdapter.notifyDataSetChanged();
                }
            } else {
                int supportNum = replyList.get(replyPosition).getSupportNum();
                replyList.get(replyPosition).setSupportNum(supportNum + 1);
                replyAdapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.DELETE_MAIN_REPLY) && event.obj != null
                && event.obj instanceof ReplyEntity) {
            replyNum--;
            tv_commentNum.setText("共有" + replyNum + "条评论");
            ReplyEntity entity = (ReplyEntity) event.obj;
            if (replyList.contains(entity)) {
                replyList.remove(entity);
                replyAdapter.notifyDataSetChanged();
                getReply();
            }
        }
    }
}
