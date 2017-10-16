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
import com.haoyu.app.adapter.MFileInfoAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.dialog.FileUploadDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.FileUploadDataResult;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MFileInfoData;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.ReplyEntity;
import com.haoyu.app.entity.ReplyListResult;
import com.haoyu.app.entity.ReplyResult;
import com.haoyu.app.entity.TeachingLessonEntity;
import com.haoyu.app.entity.TeachingLessonSingleResult;
import com.haoyu.app.filePicker.LFilePicker;
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
import com.haoyu.app.view.RoundRectProgressBar;
import com.haoyu.app.view.StickyScrollView;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

/**
 * 创建日期：2017/1/11 on 13:29
 * 描述: 社区创课详情
 * 作者:马飞奔 Administrator
 */
public class TeachingResearchCCActivity extends BaseActivity implements View.OnClickListener {
    private TeachingResearchCCActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.empty_detail)
    TextView empty_detail;
    @BindView(R.id.detailLayout)
    LinearLayout detailLayout;
    @BindView(R.id.contentView)
    StickyScrollView contentView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.loadFailView1)
    LoadFailView loadFailView1;
    @BindView(R.id.mRrogressBar)
    RoundRectProgressBar roundBar;
    @BindView(R.id.tv_day)
    TextView tv_day;
    @BindView(R.id.tv_ccTitle)
    TextView tv_ccTitle;
    @BindView(R.id.iv_userIco)
    ImageView iv_userIco;
    @BindView(R.id.tv_userName)
    TextView tv_userName;
    @BindView(R.id.tv_createTime)
    TextView tv_createTime;
    @BindView(R.id.bt_heatNum)
    Button bt_heatNum;
    @BindView(R.id.bt_supportNum)
    Button bt_supportNum;
    @BindView(R.id.bt_adviseNum)
    Button bt_adviseNum;
    private int supportNum, adviseNum;
    @BindView(R.id.ll_ccContent)
    LinearLayout ll_ccContent;
    @BindView(R.id.ll_sticky)
    LinearLayout ll_sticky;
    @BindView(R.id.iv_expand)
    ImageView iv_expand;
    @BindView(R.id.tv_ccContent)
    HtmlTextView tv_ccContent;
    @BindView(R.id.tv_checkAll)
    TextView tv_checkAll;
    @BindView(R.id.tv_error)
    TextView tv_error;
    @BindView(R.id.loadingFile)
    LoadingView loadingFile;
    @BindView(R.id.rv_file)
    RecyclerView rv_file;
    @BindView(R.id.empty_resources)
    View empty_resources;
    @BindView(R.id.ll_advise)
    View ll_advise;
    @BindView(R.id.tv_advise)
    TextView tv_advise;
    private List<ReplyEntity> adviseList = new ArrayList<>();
    private AppDiscussionAdapter adviseAdapter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.tv_more_reply)
    View tv_more_reply;
    @BindView(R.id.empty_advise)
    LinearLayout empty_advise;
    @BindView(R.id.tv_giveAdvise)
    TextView tv_giveAdvise;
    @BindView(R.id.bottomView)
    View bottomView;
    private String id, relationId;
    private File uploadFile;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teaching_research_cc;
    }

    @Override
    public void initView() {
        id = getIntent().getStringExtra("id");
        int remainDay = getIntent().getIntExtra("remainDay", 0);
        relationId = getIntent().getStringExtra("relationId");
        FullyLinearLayoutManager manager = new FullyLinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        adviseAdapter = new AppDiscussionAdapter(context, adviseList, getUserId());
        recyclerView.setAdapter(adviseAdapter);
        roundBar.setMax(60);
        roundBar.setProgress(60 - remainDay);
        tv_day.setText("还剩" + remainDay + "天");
        registRxBus();
    }

    public void initData() {
        final String url = Constants.OUTRT_NET + "/m/lesson/cmts/view/" + id;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<TeachingLessonSingleResult>() {
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
            public void onResponse(TeachingLessonSingleResult singleResult) {
                loadingView.setVisibility(View.VISIBLE);
                if (singleResult != null && singleResult.getResponseData() != null && singleResult.getResponseData().getmLesson() != null) {
                    contentView.setVisibility(View.VISIBLE);
                    updateUI(singleResult.getResponseData().getmLesson());
                    getFiles();
                    getAdvise();
                } else {
                    empty_detail.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateUI(TeachingLessonEntity entity) {
        tv_ccTitle.setText(entity.getTitle());
        if (entity.getCreator() != null && entity.getCreator().getAvatar() != null) {
            GlideImgManager.loadCircleImage(context, entity.getCreator().getAvatar(),
                    R.drawable.user_default, R.drawable.user_default, iv_userIco);
        } else {
            iv_userIco.setImageResource(R.drawable.user_default);
        }
        if (entity.getCreator() != null && entity.getCreator().getRealName() != null) {
            tv_userName.setText(entity.getCreator().getRealName());
        } else {
            tv_userName.setText("匿名用户");
        }
        if (entity.getCreator() != null && entity.getCreator().getId() != null
                && entity.getCreator().getId().equals(getUserId())) {
            toolBar.setShow_right_button(true);
        }
        tv_createTime.setText("发布于" + TimeUtil.getSlashDate(entity.getCreateTime()));
        if (entity.getmDiscussionRelations() != null && entity.getmDiscussionRelations().size() > 0) {
            supportNum = entity.getmDiscussionRelations().get(0).getSupportNum();
            adviseNum = entity.getmDiscussionRelations().get(0).getReplyNum();
            bt_heatNum.setText("热度（" + entity.getmDiscussionRelations().get(0).getBrowseNum() + "）");
            bt_supportNum.setText("赞（" + entity.getmDiscussionRelations().get(0).getSupportNum() + "）");
            bt_adviseNum.setText("提建议（" + adviseNum + "）");
            tv_advise.setText("收到" + adviseNum + "条建议");
        } else {
            bt_heatNum.setText("热度（" + 0 + "）");
            bt_supportNum.setText("赞（" + 0 + "）");
            bt_adviseNum.setText("提建议（" + 0 + "）");
            tv_advise.setText("收到" + 0 + "条建议");
        }
        if (entity.getContent() != null && entity.getContent().trim().length() > 0) {
            ll_ccContent.setVisibility(View.VISIBLE);
            tv_ccContent.setHtml(entity.getContent(), new HtmlHttpImageGetter(tv_ccContent, Constants.REFERER));
            ll_sticky.setOnClickListener(new View.OnClickListener() {
                private boolean isExpand = true;

                @Override
                public void onClick(View view) {
                    if (isExpand) {
                        tv_ccContent.setVisibility(View.VISIBLE);
                        iv_expand.setImageResource(R.drawable.course_dictionary_shouqi);
                        isExpand = false;
                    } else {
                        contentView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                contentView.smoothScrollTo(0, ll_sticky.getTop());
                            }
                        }, 10);
                        tv_ccContent.setVisibility(View.GONE);
                        iv_expand.setImageResource(R.drawable.course_dictionary_xiala);
                        isExpand = true;
                    }
                }
            });
        } else {
            ll_ccContent.setVisibility(View.GONE);
        }
        detailLayout.setVisibility(View.VISIBLE);
    }

    private void getFiles() {
        String url = Constants.OUTRT_NET + "/m/file?fileRelations[0].relation.id=" + relationId + "&fileRelations[0].relation.type=discussion&limit=2&orders=CREATE_TIME.DESC";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MFileInfoData>>() {
            @Override
            public void onBefore(Request request) {
                loadingFile.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingFile.setVisibility(View.GONE);
                tv_error.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(BaseResponseResult<MFileInfoData> response) {
                loadingFile.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    updateFiles(response.getResponseData());
                }
            }
        }));
    }

    private void updateFiles(MFileInfoData responseData) {
        if (responseData.getmFileInfos().size() > 0) {
            final List<MFileInfo> mDatas = responseData.getmFileInfos();
            rv_file.setVisibility(View.VISIBLE);
            FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
            layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
            rv_file.setLayoutManager(layoutManager);
            MFileInfoAdapter adapter = new MFileInfoAdapter(mDatas);
            rv_file.setAdapter(adapter);
            adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                    MFileInfo fileInfo = mDatas.get(position);
                    Intent intent = new Intent(context, MFileInfoActivity.class);
                    intent.putExtra("fileInfo", fileInfo);
                    startActivity(intent);
                }
            });
            if (responseData.getPaginator() != null && responseData.getPaginator().getHasNextPage())
                tv_checkAll.setVisibility(View.VISIBLE);
            tv_checkAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MFileInfosActivity.class);
                    intent.putExtra("title", "课程资源");
                    intent.putExtra("relationId", relationId);
                    intent.putExtra("relationType", "discussion");
                    startActivity(intent);
                }
            });
        } else {
            empty_resources.setVisibility(View.VISIBLE);
        }
    }

    private void getAdvise() {
        String url = Constants.OUTRT_NET + "/m/discussion/post?discussionUser.discussionRelation.id=" + relationId
                + "&orders=CREATE_TIME.ASC" + "&limit=5";
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
                        if (response != null && response.getResponseData() != null &&
                                response.getResponseData().getmDiscussionPosts() != null) {
                            updateAdvise(response.getResponseData().getmDiscussionPosts(),
                                    response.getResponseData().getPaginator());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
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
                String url = Constants.OUTRT_NET + "/m/discussion/post?discussionUser.discussionRelation.id=" + relationId + "&mainPostId=" + mainPostId
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

    private void updateAdvise(List<ReplyEntity> mDatas, Paginator paginator) {
        adviseList.clear();
        if (mDatas.size() > 0) {
            adviseList.addAll(mDatas);
            adviseAdapter.notifyDataSetChanged();
            if (paginator != null && paginator.getHasNextPage()) {
                tv_more_reply.setVisibility(View.VISIBLE);
            }
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            empty_advise.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        bottomView.setVisibility(View.VISIBLE);
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
        bt_supportNum.setOnClickListener(context);
        bt_adviseNum.setOnClickListener(context);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        loadFailView1.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getAdvise();
            }
        });
        tv_error.setOnClickListener(context);
        tv_more_reply.setOnClickListener(context);
        tv_giveAdvise.setOnClickListener(context);
        bottomView.setOnClickListener(context);
        adviseAdapter.setOnPostClickListener(new AppDiscussionAdapter.OnPostClickListener() {
            @Override
            public void onTargetClick(View view, int position, ReplyEntity entity) {

            }

            @Override
            public void onChildClick(View view, int position) {
                childPosition = position;
                showCommentDialog(true);
            }
        });

        adviseAdapter.setSupportCallBack(new AppDiscussionAdapter.SupportCallBack() {
            @Override
            public void support(int position, TextView tv_like) {
                createLike(position, tv_like);
            }
        });
        adviseAdapter.setMoreReplyCallBack(new AppDiscussionAdapter.MoreReplyCallBack() {
            @Override
            public void callBack(ReplyEntity entity, int position) {
                replyPosition = position;
                Intent intent = new Intent(context, AppMoreChildReplyActivity.class);
                intent.putExtra("entity", entity);
                intent.putExtra("relationId", relationId);
                startActivity(intent);
            }
        });
        adviseAdapter.setDeleteMainReply(new AppDiscussionAdapter.DeleteMainReply() {
            @Override
            public void deleteMainReply(String id, int position) {
                deleteReply(id, position);
            }
        });
    }

    /**
     * 创建观点(点赞)
     *
     * @param position
     */
    private void createLike(final int position, final TextView tvLike) {
        String url = Constants.OUTRT_NET + "/m/attitude";
        String relationId = adviseList.get(position).getId();
        HashMap<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", relationId);
        map.put("relation.type", "discussion_post");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            public void onError(Request request, Exception exception) {
                onNetWorkError(context);
            }

            public void onResponse(AttitudeMobileResult response) {
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    GoodView goodView = new GoodView(context);
                    int defaultColor = ContextCompat.getColor(context, R.color.defaultColor);
                    goodView.setTextInfo("+1", defaultColor, 15);
                    goodView.show(tvLike);
                    int count = adviseList.get(position).getSupportNum() + 1;
                    adviseList.get(position).setSupportNum(count);
                    adviseAdapter.notifyDataSetChanged();
                } else if (response != null && response.getResponseMsg() != null) {
                    toast(context, "您已点赞过");
                } else {
                    toast(context, "点赞失败");
                }
            }
        }, map));
    }

    private void sendChildReply(final int position, final String content) {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("mainPostId", adviseList.get(position).getId());
        map.put("discussionUser.discussionRelation.id", relationId);
        String url = Constants.OUTRT_NET + "/m/discussion/post";
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<ReplyResult>() {
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
                    int childPostCount = adviseList.get(position).getChildPostCount() + 1;
                    adviseList.get(position).setChildPostCount(childPostCount);
                    if (adviseList.get(position).getChildReplyEntityList() != null && adviseList.get(position).getChildReplyEntityList().size() < 10) {
                        adviseList.get(position).getChildReplyEntityList().add(entity);
                    } else {
                        toastFullScreen("回复成功", true);
                    }
                    adviseAdapter.notifyDataSetChanged();
                }
            }
        }, map));
    }

    private void deleteReply(String id, final int position) {
        String url = Constants.OUTRT_NET + "/m/discussion/post/" + id;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        map.put("discussionUser.discussionRelation.id", relationId);
        map.put("mainPostId", id);
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
                if (response != null && response.getResponseCode() != null
                        && response.getResponseCode().equals("00")) {
                    adviseList.remove(position);
                    adviseAdapter.notifyDataSetChanged();
                    if (adviseList.size() == 0) {
                        empty_advise.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    adviseNum--;
                    tv_advise.setText("收到" + adviseNum + "条建议");
                    getAdvise();
                }
            }
        }, map));
    }

    private int childPosition, replyPosition;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_error:
                getFiles();
                break;
            case R.id.bt_supportNum:
                createLike();
                break;
            case R.id.bt_adviseNum:
                contentView.smoothScrollTo(0, (int) ll_advise.getY());
                break;
            case R.id.tv_giveAdvise:
                showCommentDialog(false);
                break;
            case R.id.bottomView:
                showCommentDialog(false);
                break;
            case R.id.tv_more_reply:
                Intent intent = new Intent(context, AppMoreMainReplyActivity.class);
                intent.putExtra("type", "advise");
                intent.putExtra("relationId", relationId);
                startActivity(intent);
                break;
        }
    }

    private void createLike() {
        String url = Constants.OUTRT_NET + "/m/attitude";
        Map<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", id);
        map.put("relation.type", "discussion");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            @Override
            public void onError(Request request, Exception exception) {
                onNetWorkError(context);
            }

            @Override
            public void onResponse(AttitudeMobileResult response) {
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    GoodView goodView = new GoodView(context);
                    int defaultColor = ContextCompat.getColor(context, R.color.defaultColor);
                    goodView.setTextInfo("+1", defaultColor, 16);
                    goodView.show(bt_supportNum);
                    supportNum++;
                    bt_supportNum.setText("赞（" + supportNum + "）");
                    MessageEvent event = new MessageEvent();
                    event.action = Action.SUPPORT_STUDY_CLASS;
                    RxBus.getDefault().post(event);
                } else if (response != null && response.getResponseMsg() != null) {
                    toast(context, "您已点赞过");
                } else {
                    toast(context, "点赞失败");
                }
            }
        }, map));
    }

    private void showCommentDialog(final boolean sendChild) {
        CommentDialog dialog = new CommentDialog(context, "请输入您的建议");
        dialog.show();
        dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
            @Override
            public void sendComment(String content) {
                if (sendChild) {
                    sendChildReply(childPosition, content);
                } else {
                    giveAdvice(content);
                }
            }
        });
    }

    private void showBottomDialog() {
        View view = getLayoutInflater().inflate(
                R.layout.dialog_teaching_cc, null);
        final AlertDialog bottomDialog = new AlertDialog.Builder(context).create();
        View tv_upload = view.findViewById(R.id.tv_upload);
        View tv_share = view.findViewById(R.id.tv_share);
        View tv_edit = view.findViewById(R.id.tv_edit);
        View tv_delete = view.findViewById(R.id.tv_delete);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomDialog != null) {
                    bottomDialog.dismiss();
                }
                switch (view.getId()) {
                    case R.id.tv_upload:
                        openFilePicker();
                        break;
                    case R.id.tv_share:
                        toast(context, "暂不支持");
                        break;
                    case R.id.tv_edit:
                        Intent intent = new Intent(context, TeachingResearchCreateCCActivity.class);
                        intent.putExtra("title", tv_ccTitle.getText().toString());
                        intent.putExtra("content", tv_ccContent.getText().toString());
                        intent.putExtra("id", id);
                        intent.putExtra("alter", true);
                        startActivity(intent);
                        break;
                    case R.id.tv_delete:
                        showTipsDialog();
                        break;
                }
            }
        };
        tv_share.setOnClickListener(listener);
        tv_delete.setOnClickListener(listener);
        tv_upload.setOnClickListener(listener);
        tv_edit.setOnClickListener(listener);
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

    private void openFilePicker() {
        new LFilePicker()
                .withActivity(context)
                .withRequestCode(1)
                .withMutilyMode(false)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            List<String> list = data.getStringArrayListExtra(RESULT_INFO);
            if (list != null && list.size() > 0) {
                String filePath = list.get(0);
                uploadFile = new File(filePath);
                uploadFile();
            }
        }
    }

    private void uploadFile() {
        if (uploadFile != null && uploadFile.exists()) {
            String url = Constants.OUTRT_NET + "/m/file/uploadTemp";
            final FileUploadDialog uploadDialog = new FileUploadDialog(context, uploadFile.getName(), "正在上传");
            uploadDialog.setCancelable(false);
            uploadDialog.setCanceledOnTouchOutside(false);
            uploadDialog.show();
            final Disposable mSubscription = Flowable.just(url).map(new Function<String, FileUploadResult>() {
                @Override
                public FileUploadResult apply(String url) throws Exception {
                    return commitFile(url, uploadDialog);
                }
            }).map(new Function<FileUploadResult, FileUploadDataResult>() {
                @Override
                public FileUploadDataResult apply(FileUploadResult mResult) throws Exception {
                    return commitContent(mResult);
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<FileUploadDataResult>() {
                        @Override
                        public void accept(FileUploadDataResult response) throws Exception {
                            uploadDialog.dismiss();
                            if (response != null && response.getResponseCode() != null &&
                                    response.getResponseCode().equals("00")) {
                                toastFullScreen("上传成功", true);
                                initData();
                            } else {
                                showErrorDialog();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            uploadDialog.dismiss();
                        }
                    });
            uploadDialog.setCancelListener(new FileUploadDialog.CancelListener() {
                @Override
                public void cancel() {
                    showCancelDialog(mSubscription, uploadDialog);
                }
            });
        } else {
            showMaterialDialog("提示", "上传的文件不存在，请重新选择文件");
        }
    }

    /*上传资源到临时文件*/
    private FileUploadResult commitFile(String url, final FileUploadDialog dialog) throws Exception {
        Gson gson = new GsonBuilder().create();
        String resultStr = OkHttpClientManager.post(context, url, uploadFile, uploadFile.getName(), new OkHttpClientManager.ProgressListener() {
            @Override
            public void onProgress(long totalBytes, long remainingBytes, boolean done, File file) {
                Flowable.just(new long[]{totalBytes, remainingBytes}).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<long[]>() {
                            @Override
                            public void accept(long[] params) throws Exception {
                                dialog.setUploadProgressBar(params[0], params[1]);
                                dialog.setUploadText(params[0], params[1]);
                            }
                        });
            }
        });
        FileUploadResult mResult = gson.fromJson(resultStr, FileUploadResult.class);
        return mResult;
    }

    /*拿到上传临时文件返回的结果再次提交到创课表*/
    private FileUploadDataResult commitContent(FileUploadResult mResult) throws Exception {
        if (mResult != null && mResult.getResponseData() != null) {
            String url = Constants.OUTRT_NET + "/m/lesson/cmts/" + id + "/upload";
            Gson gson = new GsonBuilder().create();
            Map<String, String> map = new HashMap<>();
            map.put("fileInfos[0].id", mResult.getResponseData().getId());
            map.put("fileInfos[0].url", mResult.getResponseData().getUrl());
            map.put("fileInfos[0].fileName", mResult.getResponseData().getFileName());
            String responseStr = OkHttpClientManager.postAsString(context, url, map);
            FileUploadDataResult uploadResult = gson.fromJson(responseStr, FileUploadDataResult.class);
            return uploadResult;
        }
        return null;
    }

    /*上传失败显示dialog*/
    private void showErrorDialog() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("上传结果");
        dialog.setMessage("由于网络问题上传资源失败，您可以点击重新上传再次上传");
        dialog.setNegativeTextColor(ContextCompat.getColor(context, R.color.gray));
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        dialog.setNegativeButton("取消", null);
        dialog.setPositiveButton("重新上传", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                uploadFile();
            }
        });
        dialog.show();
    }

    /*取消上传显示dialog*/
    private void showCancelDialog(final Disposable mSubscription, final FileUploadDialog uploadDialog) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("你确定取消本次上传吗？");
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        dialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                dialog.dismiss();
                mSubscription.dispose();
            }
        });
        dialog.setNegativeButton("关闭", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                dialog.dismiss();
                if (uploadDialog != null && !uploadDialog.isShowing()) {
                    uploadDialog.show();
                }
            }
        });
        dialog.show();
    }

    private void showTipsDialog() {
        MaterialDialog materialDialog = new MaterialDialog(context);
        materialDialog.setTitle("提示");
        materialDialog.setMessage("你确定删除吗？");
        materialDialog.setNegativeButton("确定", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                deleteCc();
            }
        });
        materialDialog.setPositiveButton("取消", null);
        materialDialog.show();
    }

    /*删除创课*/
    private void deleteCc() {
        String url = Constants.OUTRT_NET + "/m/lesson/cmts/" + id;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                onNetWorkError(context);
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    MessageEvent event = new MessageEvent();
                    event.action = Action.DELETE_GEN_CLASS;
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

    private void giveAdvice(final String content) {
        String url = Constants.OUTRT_NET + "/m/discussion/post";
        Map<String, String> map = new HashMap<>();
        map.put("discussionUser.discussionRelation.id", relationId);
        map.put("content", content);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<ReplyResult>() {
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
            public void onResponse(ReplyResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                    empty_advise.setVisibility(View.GONE);
                    if (adviseList.size() < 5) {
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
                        adviseList.add(entity);
                        adviseAdapter.notifyDataSetChanged();
                        if (recyclerView.getVisibility() != View.VISIBLE) {
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        tv_more_reply.setVisibility(View.VISIBLE);
                        toastFullScreen("发送成功", true);
                    }
                    adviseNum++;
                    tv_advise.setText("收到" + adviseNum + "条建议");
                    MessageEvent event = new MessageEvent();
                    event.action = Action.GIVE_STUDY_ADVICE;
                    RxBus.getDefault().post(event);
                } else {
                    toastFullScreen("发送失败", true);
                }
            }
        }, map));
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.CREATE_MAIN_REPLY) && event.obj != null && event.obj instanceof ReplyEntity) {
            adviseNum++;
            tv_advise.setText("收到" + adviseNum + "条建议");
            ReplyEntity entity = (ReplyEntity) event.obj;
            if (adviseList.size() < 5) {
                adviseList.add(entity);
                adviseAdapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.CREATE_CHILD_REPLY)) {
            /*来自更多评论列表界面创建子回复*/
            if (event.getBundle() != null && event.getBundle().getSerializable("mainReply") != null
                    && event.getBundle().getSerializable("mainReply") instanceof ReplyEntity) {
                ReplyEntity mainReply = (ReplyEntity) event.getBundle().getSerializable("mainReply");
                int position = adviseList.indexOf(mainReply);
                if (position != -1 && event.getBundle().getSerializable("childReply") != null
                        && event.getBundle().getSerializable("childReply") instanceof ReplyEntity) {
                    ReplyEntity childReply = (ReplyEntity) event.getBundle().getSerializable("childReply");
                    int childPostNum = adviseList.get(position).getChildPostCount() + 1;
                    adviseList.get(position).setChildPostCount(childPostNum);
                    if (adviseList.get(position).getChildReplyEntityList() != null && adviseList.get(position).getChildReplyEntityList().size() < 10) {
                        adviseList.get(position).getChildReplyEntityList().add(childReply);
                    }
                    adviseAdapter.notifyDataSetChanged();
                }
            } else {   //来自更多回复列表创建回复
                int childPostNum = adviseList.get(replyPosition).getChildPostCount() + 1;
                adviseList.get(replyPosition).setChildPostCount(childPostNum);
                adviseAdapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.CREATE_LIKE)) {
            if (event.obj != null && event.obj instanceof ReplyEntity) {
                ReplyEntity entity = (ReplyEntity) event.obj;
                if (adviseList.indexOf(entity) != -1) {
                    adviseList.set(adviseList.indexOf(entity), entity);
                    adviseAdapter.notifyDataSetChanged();
                }
            } else {
                int supportNum = adviseList.get(replyPosition).getSupportNum();
                adviseList.get(replyPosition).setSupportNum(supportNum + 1);
                adviseAdapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.DELETE_MAIN_REPLY) && event.obj != null
                && event.obj instanceof ReplyEntity) {
            ReplyEntity entity = (ReplyEntity) event.obj;
            if (adviseList.contains(entity)) {
                adviseList.remove(entity);
                adviseAdapter.notifyDataSetChanged();
                getAdvise();
            }
            adviseNum--;
            tv_advise.setText("收到" + adviseNum + "条建议");
        }
    }
}
