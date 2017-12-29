package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.AppCommentAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.dialog.FileUploadDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.CommentEntity;
import com.haoyu.app.entity.CommentListResult;
import com.haoyu.app.entity.FileUploadDataResult;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.TeachingMovementEntity;
import com.haoyu.app.entity.TeachingRegistAtResult;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.HtmlTagHandler;
import com.haoyu.app.utils.MediaFile;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.GoodView;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.StickyScrollView;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;

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
 * 创建日期：2017/1/12 on 11:44
 * 描述: 社区活动详情
 * 作者:马飞奔 Administrator
 */
public class CmtsMovInfoActivity extends BaseActivity implements View.OnClickListener {
    private CmtsMovInfoActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    @BindView(R.id.ssv_content)
    StickyScrollView ssv_content;
    @BindView(R.id.at_img)
    ImageView at_img;  //活动封面图
    @BindView(R.id.at_title)
    TextView at_title;  //活动标题
    @BindView(R.id.tv_time)
    TextView tv_time; //活动时间
    @BindView(R.id.tv_address)
    TextView tv_address; //活动地点
    @BindView(R.id.tv_host)
    TextView tv_host;  //活动主办
    @BindView(R.id.tv_atType)
    TextView tv_atType; //活动类型
    @BindView(R.id.tv_apply)
    TextView tv_apply;  //活动报名（截止时间）
    @BindView(R.id.tv_participation)
    TextView tv_participation;  // 报名信息
    @BindView(R.id.tv_limit)
    TextView tv_limit;  //活动人数名额
    @BindView(R.id.ll_content)
    LinearLayout ll_content;
    @BindView(R.id.iv_expand)
    ImageView iv_expand;
    @BindView(R.id.at_content)
    TextView at_content;  //活动内容

    @BindView(R.id.videoRV)
    RecyclerView videoRV;  //活动视频列表
    @BindView(R.id.empty_resources)
    View empty_resources;  //空活动花絮
    private List<MFileInfo> fileInfos = new ArrayList<>();
    FileAdapter fileAdapter;

    @BindView(R.id.tv_comment)
    TextView tv_comment;  //活动点评
    @BindView(R.id.lfv)
    LoadFailView lfv;
    @BindView(R.id.commentRV)
    RecyclerView commentRV;  //活动评论列表
    @BindView(R.id.tv_more_reply)
    TextView tv_more_reply;  //查看更多评论
    @BindView(R.id.tv_emptyComment)
    TextView tv_emptyComment;   //空评论列表
    private List<CommentEntity> commentList = new ArrayList<>();
    private AppCommentAdapter commentAdapter;

    @BindView(R.id.ll_bottom)
    LinearLayout ll_bottom;
    @BindView(R.id.tv_bottomText)
    TextView tv_bottomText;
    @BindView(R.id.bt_type)
    Button bt_type;
    private TeachingMovementEntity movEntity;
    private String movementId;
    private int viewNum, participateNum, limit;  //活动参与数,限额数
    private int replyPosition, childPosition;
    private boolean register;
    private String registerId;
    private String mainUrl;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_cmtsmovinfo;
    }

    @Override
    public void initView() {
        String empty_text = getResources().getString(R.string.teach_active_emptylist);
        tv_empty.setText(empty_text);
        movementId = getIntent().getStringExtra("movementId");
        /*设置活动封面占屏幕高度的1/4*/
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = ScreenUtils.getScreenHeight(context) / 7 * 2;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        at_img.setLayoutParams(params);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        videoRV.setLayoutManager(layoutManager);
        fileAdapter = new FileAdapter(fileInfos);
        videoRV.setAdapter(fileAdapter);
        commentAdapter = new AppCommentAdapter(context, commentList, getUserId());
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        commentRV.setNestedScrollingEnabled(false);
        commentRV.setLayoutManager(manager);
        commentRV.setAdapter(commentAdapter);
        registRxBus();
        mainUrl = Constants.OUTRT_NET + "/m/comment?relation.id=" + movementId + "&relation.type=movement&orders=CREATE_TIME.DESC";
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/movement/view/" + movementId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<TeachingMovementEntity>>() {
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
            public void onResponse(BaseResponseResult<TeachingMovementEntity> result) {
                loadingView.setVisibility(View.GONE);
                if (result != null && result.getResponseData() != null) {
                    updateUI(result.getResponseData());
                    getComment();
                } else {
                    tv_empty.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateUI(TeachingMovementEntity entity) {
        movEntity = entity;
        ssv_content.setVisibility(View.VISIBLE);
        if (movEntity.getCreator() != null && movEntity.getCreator().getId() != null && movEntity.getCreator().getId().equals(getUserId())) {
            toolBar.setShow_right_button(true);
        }
        GlideImgManager.loadImage(context, entity.getImage(), R.drawable.app_default, R.drawable.app_default, at_img);
        setTopText(entity);
        setContent_text(entity.getContent());
        setFile_infos(entity.getmFileInfos());
        setBottomText(entity);
    }

    private void setTopText(TeachingMovementEntity entity) {
        at_title.setText(entity.getTitle());
        if (entity.getmMovementRelations() != null && entity.getmMovementRelations().size() > 0) {
            TeachingMovementEntity.MovementRelation relation = entity.getmMovementRelations().get(0);
            if (relation.getTimePeriod() != null) {
                tv_time.setText("时间：" + TimeUtil.convertDayOfMinute(relation.getTimePeriod().getStartTime(),
                        relation.getTimePeriod().getEndTime()));
            }
            if (relation.getTimePeriod() != null) {
                long endTime = relation.getTimePeriod().getEndTime();
                tv_apply.setText("报名：截止" + TimeUtil.convertDayOfMinute(endTime));
            }
            viewNum = relation.getBrowseNum();
            participateNum = relation.getParticipateNum();
            limit = relation.getTicketNum();
        } else {
            tv_time.setVisibility(View.GONE);
            tv_apply.setVisibility(View.GONE);
        }
        tv_address.setText("地点：" + entity.getLocation());
        tv_host.setText("主办：" + entity.getSponsor());
        if (entity.getType() != null && entity.getType().equals("communicationMeeting")) {
            tv_atType.setText("类型：跨校交流会");
        } else if (entity.getType() != null && entity.getType().equals("expertInteraction")) {
            tv_atType.setText("类型：专家互动");
        } else if (entity.getType() != null && entity.getType().equals("lessonViewing")) {
            tv_atType.setText("类型：创课观摩");
        } else {
            tv_atType.setVisibility(View.GONE);
        }
        if (entity.getParticipationType() != null && entity.getParticipationType().equals("ticket")) {
            tv_participation.setText("参与：须报名预约，凭电子票入场");
            tv_limit.setVisibility(View.VISIBLE);
            tv_limit.setText("限额：" + participateNum + "/" + limit + "人");
        } else if (entity.getParticipationType() != null && entity.getParticipationType().equals("free")) {
            tv_participation.setText("参与：在线报名，免费入场");
        } else if (entity.getParticipationType() != null && entity.getParticipationType().equals("chair")) {
            tv_participation.setText("参与：讲座视频录像+在线问答交流");
        } else {
            tv_limit.setVisibility(View.GONE);
            tv_participation.setVisibility(View.GONE);
        }
    }

    private void setContent_text(String content) {
        if (content != null && content.length() > 0) {
            ll_content.setVisibility(View.VISIBLE);
            HtmlTagHandler tagHandler = new HtmlTagHandler(new HtmlTagHandler.OnImageClickListener() {
                @Override
                public void onImageClick(View view, String url) {
                    ArrayList<String> imgList = new ArrayList<>();
                    imgList.add(Constants.REFERER + url);
                    Intent intent = new Intent(context, AppMultiImageShowActivity.class);
                    intent.putStringArrayListExtra("photos", imgList);
                    startActivity(intent);
                    overridePendingTransition(R.anim.zoom_in, 0);
                }
            });
            Html.ImageGetter imageGetter = new HtmlHttpImageGetter(at_content, Constants.REFERER, true);
            Spanned spanned = Html.fromHtml(content, imageGetter, tagHandler);
            at_content.setMovementMethod(LinkMovementMethod.getInstance());
            at_content.setText(spanned);
            at_content.setVisibility(View.VISIBLE);
            iv_expand.setImageResource(R.drawable.course_dictionary_shouqi);
            ll_content.setOnClickListener(new View.OnClickListener() {
                private boolean isExpand = false;

                @Override
                public void onClick(View view) {
                    if (isExpand) {
                        at_content.setVisibility(View.VISIBLE);
                        iv_expand.setImageResource(R.drawable.course_dictionary_shouqi);
                        isExpand = false;
                    } else {
                        ssv_content.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ssv_content.smoothScrollTo(0, ll_content.getTop());
                            }
                        }, 10);
                        at_content.setVisibility(View.GONE);
                        iv_expand.setImageResource(R.drawable.course_dictionary_xiala);
                        isExpand = true;
                    }
                }
            });
        } else {
            ll_content.setVisibility(View.GONE);
        }
    }

    public void setFile_infos(List<MFileInfo> mDatas) {
        if (mDatas != null && mDatas.size() > 0) {
            fileAdapter.addAll(mDatas);
            videoRV.setVisibility(View.VISIBLE);
            empty_resources.setVisibility(View.GONE);
        } else {
            videoRV.setVisibility(View.GONE);
            empty_resources.setVisibility(View.VISIBLE);
        }
    }

    private void setBottomText(TeachingMovementEntity entity) {
        ll_bottom.setVisibility(View.VISIBLE);
        setNum_text();
        if (entity.getState() != null && entity.getState().equals("register")) {
            register = true;
            if (entity.getmMovementRegisters() != null && entity.getmMovementRegisters().size() > 0) {
                if (entity.getmMovementRegisters().get(0).getId() != null) {
                    registerId = entity.getmMovementRegisters().get(0).getId();
                    register = false;
                    bt_type.setText("取消报名");
                    bt_type.setEnabled(true);
                }
            } else {
                bt_type.setEnabled(true);
                bt_type.setText("报名参与");
            }
        } else {
            bt_type.setText("报名参与");
            bt_type.setEnabled(false);
        }
    }

    private void setNum_text() {
        String text = viewNum + " 次浏览，" + participateNum + " 人参加";
        SpannableString ssb = new SpannableString(text);
        ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)),
                0, text.indexOf("次"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        int start = text.indexOf("，") + 1;
        int end = text.indexOf("人");
        ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)),
                start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_bottomText.setText(ssb);
    }

    private class FileAdapter extends BaseArrayRecyclerAdapter<MFileInfo> {
        private List<String> imgList = new ArrayList<>();

        public FileAdapter(List<MFileInfo> mDatas) {
            super(mDatas);
        }

        private void addAll(List<MFileInfo> fileInfos) {
            mDatas.addAll(fileInfos);
            for (MFileInfo mFileInfo : mDatas) {
                imgList.add(mFileInfo.getUrl());
            }
            notifyDataSetChanged();
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, final MFileInfo mFileInfo, final int position) {
            View contentView = holder.obtainView(R.id.contentView);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ScreenUtils.getScreenWidth(context) / 3, ScreenUtils.getScreenWidth(context) / 4);
            contentView.setLayoutParams(params);
            ImageView iv_img = holder.obtainView(R.id.iv_img);
            ImageView iv_video = holder.obtainView(R.id.iv_video);
            if (mFileInfo.getUrl() != null && MediaFile.isVideoFileType(mFileInfo.getUrl())) {
                iv_video.setVisibility(View.VISIBLE);
            } else {
                iv_video.setVisibility(View.GONE);
            }
            if (mFileInfo.getUrl() != null) {
                GlideImgManager.loadImage(context, mFileInfo.getUrl(), R.drawable.app_default, R.drawable.app_default, iv_img);
            } else {
                iv_img.setImageResource(R.drawable.app_default);
            }
            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, AppMultiImageShowActivity.class);
                    intent.putStringArrayListExtra("photos", (ArrayList<String>) imgList);
                    intent.putExtra("position", position);
                    startActivity(intent);
                    overridePendingTransition(R.anim.zoom_in, 0);
                }
            });
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.teaching_research_at_file_item;
        }
    }

    private void getComment() {
        tv_more_reply.setVisibility(View.GONE);
        String url = mainUrl + "&limit=5";
        addSubscription(Flowable.just(url).map(new Function<String, CommentListResult>() {
            @Override
            public CommentListResult apply(String url) throws Exception {
                return getComment(url);
            }
        }).map(new Function<CommentListResult, CommentListResult>() {
            @Override
            public CommentListResult apply(CommentListResult result) throws Exception {
                if (result != null && result.getResponseData() != null && result.getResponseData().getmComments().size() > 0) {
                    return getChildComment(result, result.getResponseData().getmComments());
                }
                return result;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<CommentListResult>() {
            @Override
            public void accept(CommentListResult response) throws Exception {
                if (response != null && response.getResponseData() != null) {
                    updateListUI(response.getResponseData().getmComments(), response.getResponseData().getPaginator());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                loadFailView.setVisibility(View.VISIBLE);
            }
        }));
    }

    /*获取主评论列表*/
    private CommentListResult getComment(String url) throws Exception {
        String json = OkHttpClientManager.getAsString(context, url);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, CommentListResult.class);
    }

    /*通过主评论id获取子评论*/
    private CommentListResult getChildComment(CommentListResult result, List<CommentEntity> list) {
        for (int i = 0; i < list.size(); i++) {
            String mainPostId = list.get(i).getId();
            String url = mainUrl + "&mainPostId=" + mainPostId;
            try {
                CommentListResult mResult = getComment(url);
                if (mResult != null && mResult.getResponseData() != null) {
                    List<CommentEntity> childList = mResult.getResponseData().getmComments();
                    result.getResponseData().getmComments().get(i).setChildList(childList);
                }
            } catch (Exception e) {
                continue;
            }
        }
        return result;
    }

    /*更新评论列表*/
    private void updateListUI(List<CommentEntity> mDatas, Paginator paginator) {
        commentList.clear();
        if (mDatas.size() > 0) {
            commentList.addAll(mDatas);
            commentAdapter.notifyDataSetChanged();
            if (paginator != null && paginator.getHasNextPage()) {
                tv_more_reply.setVisibility(View.VISIBLE);
            }
            commentRV.setVisibility(View.VISIBLE);
        } else {
            commentRV.setVisibility(View.GONE);
            setEmpty_text();
        }
    }

    private void setEmpty_text() {
        tv_emptyComment.setVisibility(View.VISIBLE);
        String text = "目前还没人发起评论，\n赶紧去发表您的评论吧！";
        SpannableString ssb = new SpannableString(text);
        int start = text.indexOf("去") + 1;
        int end = text.indexOf("吧");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                showCommentDialog(false);
            }
        };
        ssb.setSpan(clickableSpan, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.defaultColor)),
                start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_emptyComment.setMovementMethod(LinkMovementMethod.getInstance());
        tv_emptyComment.setText(ssb);
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
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        lfv.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getComment();
            }
        });
        tv_comment.setOnClickListener(context);
        tv_more_reply.setOnClickListener(context);
        bt_type.setOnClickListener(context);
        commentAdapter.setCommentCallBack(new AppCommentAdapter.CommentCallBack() {
            @Override
            public void comment(int position, CommentEntity entity) {
                childPosition = position;
                showCommentDialog(true);
            }
        });

        commentAdapter.setSupportCallBack(new AppCommentAdapter.SupportCallBack() {
            @Override
            public void support(int position, TextView tv_like) {
                createLike(position, tv_like);
            }
        });
        commentAdapter.setMoreReplyCallBack(new AppCommentAdapter.MoreReplyCallBack() {
            @Override
            public void moreReply(int position, CommentEntity entity) {
                replyPosition = position;
                Intent intent = new Intent(context, AppMoreReplyActivity.class);
                intent.putExtra("entity", entity);
                intent.putExtra("relationType", "movement");
                intent.putExtra("relationId", movementId);
                startActivity(intent);
            }
        });
        commentAdapter.setDeleteMainComment(new AppCommentAdapter.DeleteMainComment() {
            @Override
            public void deleteMainComment(String id, int position) {
                deleteComment(id, position);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_comment:
                showCommentDialog(false);
                break;
            case R.id.tv_more_reply:
                Intent intent = new Intent(context, AppMoreCommentActivity.class);
                intent.putExtra("relationId", movementId);
                intent.putExtra("relationType", "movement");
                startActivity(intent);
                break;
            case R.id.bt_type:
                if (register) {   //报名参与
                    registerActivity();
                } else {           //取消报名
                    unRegisterActivity();
                }
                break;
        }
    }

    private void showCommentDialog(final boolean sendChild) {
        CommentDialog dialog = new CommentDialog(context, "请输入评论内容");
        dialog.show();
        dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
            @Override
            public void sendComment(String content) {
                if (sendChild) {
                    sendChildComment(content);
                } else {
                    sendMainComment(content);
                }
            }
        });
    }

    /*发送主评论*/
    private void sendMainComment(String content) {
        String url = Constants.OUTRT_NET + "/m/comment";
        Map<String, String> map = new HashMap<>();
        map.put("relation.id", movementId);
        map.put("relation.type", "movement");
        map.put("content", content);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<CommentEntity>>() {
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
            public void onResponse(BaseResponseResult<CommentEntity> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    commentRV.setVisibility(View.VISIBLE);
                    tv_emptyComment.setVisibility(View.GONE);
                    CommentEntity entity = response.getResponseData();
                    entity.setCreator(getCreator(entity.getCreator()));
                    if (commentList.size() < 5) {
                        commentList.add(entity);
                        commentAdapter.notifyDataSetChanged();
                    } else {
                        tv_more_reply.setVisibility(View.VISIBLE);
                        toastFullScreen("评论成功", true);
                    }
                } else {
                    toastFullScreen("评论失败", false);
                }
            }
        }, map));
    }

    private MobileUser getCreator(MobileUser creaotr) {
        if (creaotr == null) {
            creaotr = new MobileUser();
            creaotr.setId(getUserId());
            creaotr.setAvatar(getAvatar());
            creaotr.setRealName(getRealName());
        } else {
            creaotr.setId(getUserId());
            creaotr.setAvatar(getAvatar());
            creaotr.setRealName(getRealName());
        }
        return creaotr;
    }

    /*发送子评论*/
    private void sendChildComment(String content) {
        String url = Constants.OUTRT_NET + "/m/comment";
        Map<String, String> map = new HashMap<>();
        map.put("relation.id", movementId);
        map.put("relation.type", "movement");
        map.put("content", content);
        map.put("mainId", commentList.get(childPosition).getId());
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<CommentEntity>>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult<CommentEntity> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    int childNum = commentList.get(childPosition).getChildNum() + 1;
                    commentList.get(childPosition).setChildNum(childNum);
                    commentAdapter.notifyDataSetChanged();
                    if (commentList.get(childPosition).getChildList().size() >= 10) {
                        toastFullScreen("评论成功", true);
                    } else {
                        CommentEntity entity = response.getResponseData();
                        entity.setCreator(getCreator(entity.getCreator()));
                        commentList.get(childPosition).getChildList().add(entity);
                        commentAdapter.notifyDataSetChanged();
                    }
                } else {
                    toastFullScreen("评论失败", false);
                }
            }
        }, map));
    }

    /**
     * 创建观点(点赞)
     */
    private void createLike(final int position, final TextView tvLike) {
        String url = Constants.OUTRT_NET + "/m/attitude";
        String relationId = commentList.get(position).getId();
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
                    int count = commentList.get(position).getSupportNum() + 1;
                    commentList.get(position).setSupportNum(count);
                    commentAdapter.notifyDataSetChanged();
                } else if (response != null && response.getResponseMsg() != null) {
                    toast(context, "您已点赞过");
                } else {
                    toast(context, "点赞失败");
                }
            }
        }, map));
    }

    /**
     * 删除评论
     *
     * @param id
     * @param position
     */
    private void deleteComment(String id, final int position) {
        String url = Constants.OUTRT_NET + "/m/comment/" + id;
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
                    commentList.remove(position);
                    commentAdapter.notifyDataSetChanged();
                    if (commentList.size() == 0) {
                        commentRV.setVisibility(View.GONE);
                        tv_emptyComment.setVisibility(View.VISIBLE);
                    }
                    getComment();
                }
            }
        }, map));
    }

    private void registerActivity() {
        String url = Constants.OUTRT_NET + "/m/movement/register";
        Map<String, String> map = new HashMap<>();
        map.put("movement.id", movementId);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<TeachingRegistAtResult>() {
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
            public void onResponse(TeachingRegistAtResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    registerId = response.getResponseData().getId();
                    register = false;
                    bt_type.setText("取消报名");
                    participateNum += 1;
                    setNum_text();
                    MessageEvent event = new MessageEvent();
                    event.action = Action.REGIST_MOVEMENT;
                    movEntity.getmMovementRegisters().add(response.getResponseData());
                    if (movEntity.getmMovementRelations().size() > 0) {
                        movEntity.getmMovementRelations().get(0).setParticipateNum(participateNum);
                    }
                    event.obj = movEntity;
                    RxBus.getDefault().post(event);
                } else {
                    toast(context, "报名失败");
                }
            }
        }, map));
    }

    private void unRegisterActivity() {
        String url = Constants.OUTRT_NET + "/m/movement/register/" + registerId;
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
                    register = true;
                    bt_type.setText("报名参与");
                    participateNum -= 1;
                    if (participateNum <= 0) {
                        participateNum = 0;
                    }
                    setNum_text();
                    MessageEvent event = new MessageEvent();
                    event.action = Action.UNREGIST_MOVEMENT;
                    movEntity.getmMovementRegisters().clear();
                    if (movEntity.getmMovementRelations().size() > 0) {
                        movEntity.getmMovementRelations().get(0).setParticipateNum(participateNum);
                    }
                    event.obj = movEntity;
                    RxBus.getDefault().post(event);
                } else {
                    toast(context, "取消报名失败");
                }
            }
        }, map));
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.CREATE_CHILD_COMMENT)) {
            if (event.getBundle() != null && event.getBundle().getSerializable("mainComment") != null
                    && event.getBundle().getSerializable("mainComment") instanceof CommentEntity) {
                CommentEntity mainComment = (CommentEntity) event.getBundle().getSerializable("mainComment");
                int position = commentList.indexOf(mainComment);
                if (position != -1 && event.getBundle().getSerializable("childComment") != null
                        && event.getBundle().getSerializable("childComment") instanceof CommentEntity) {
                    int childPostNum = commentList.get(position).getChildNum();
                    commentList.get(position).setChildNum(childPostNum + 1);
                    CommentEntity childComment = (CommentEntity) event.getBundle().getSerializable("childComment");
                    if (commentList.get(position).getChildList() != null && commentList.get(position).getChildList().size() < 10) {
                        commentList.get(position).getChildList().add(childComment);
                    }
                    commentAdapter.notifyDataSetChanged();
                }
            } else {
                int childPostNum = commentList.get(replyPosition).getChildNum();
                commentList.get(replyPosition).setChildNum(childPostNum + 1);
                commentAdapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.CREATE_LIKE)) {
            if (event.obj != null && event.obj instanceof CommentEntity) {
                CommentEntity entity = (CommentEntity) event.obj;
                if (commentList.indexOf(entity) != -1) {
                    commentList.set(commentList.indexOf(entity), entity);
                    commentAdapter.notifyDataSetChanged();
                }
            } else {
                int supportNum = commentList.get(replyPosition).getSupportNum();
                commentList.get(replyPosition).setSupportNum(supportNum + 1);
                commentAdapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.DELETE_MAIN_COMMENT) && event.obj != null && event.obj instanceof CommentEntity) {       //在更多评论列表删除评论
            CommentEntity entity = (CommentEntity) event.getObj();
            if (commentList.contains(entity)) {
                commentList.remove(entity);
                commentAdapter.notifyDataSetChanged();
                getComment();
            }
        }
    }

    private void showBottomDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_teaching_at, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        TextView tv_video = view.findViewById(R.id.tv_video);
        TextView tv_photo = view.findViewById(R.id.tv_photo);
        TextView tv_delete = view.findViewById(R.id.tv_delete);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_video:
                        pickerMedia(2);
                        break;
                    case R.id.tv_photo:
                        pickerMedia(1);
                        break;
                    case R.id.tv_delete:
                        showTipsDialog();
                        break;
                }
                dialog.dismiss();
            }
        };
        tv_video.setOnClickListener(listener);
        tv_photo.setOnClickListener(listener);
        tv_delete.setOnClickListener(listener);
        tv_cancel.setOnClickListener(listener);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(ScreenUtils.getScreenWidth(context), LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setWindowAnimations(R.style.dialog_anim);
        window.setContentView(view);
        window.setGravity(Gravity.BOTTOM);
    }

    private void pickerMedia(final int mediaType) {
        MediaOption.Builder builder = new MediaOption.Builder();
        if (mediaType == 1) {
            builder.setSelectType(MediaOption.TYPE_IMAGE);
        } else {
            builder.setSelectType(MediaOption.TYPE_VIDEO);
        }
        builder.setShowCamera(true);
        MediaOption option = builder.build();
        MediaPicker.getInstance().init(option).selectMedia(context, new MediaPicker.onSelectMediaCallBack() {
            @Override
            public void onSelected(final String path) {
                if (new File(path).exists()) {
                    if (mediaType == 2 && NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context)) {
                        MaterialDialog dialog = new MaterialDialog(context);
                        dialog.setTitle("网络提醒");
                        dialog.setMessage("使用2G/3G/4G网络上传视频会消耗较多流量。确定要开启吗？");
                        dialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
                            @Override
                            public void onClick(View v, AlertDialog dialog) {
                                upload(new File(path));
                            }
                        });
                        dialog.setNegativeButton("取消", null);
                        dialog.show();
                    } else {
                        upload(new File(path));
                    }
                } else {
                    toast(context, "文件不存在");
                }
            }
        });
    }

    private void upload(final File file) {
        String url = Constants.OUTRT_NET + "/m/file/uploadTemp";
        final FileUploadDialog uploadDialog = new FileUploadDialog(context, file.getName(), "正在上传");
        uploadDialog.setCancelable(false);
        uploadDialog.setCanceledOnTouchOutside(false);
        uploadDialog.show();
        final Disposable mSubscription = Flowable.just(url).map(new Function<String, FileUploadResult>() {
            @Override
            public FileUploadResult apply(String url) throws Exception {
                return commitFile(url, file, uploadDialog);
            }
        }).map(new Function<FileUploadResult, FileUploadDataResult>() {
            @Override
            public FileUploadDataResult apply(FileUploadResult mResult) throws Exception {
                return commitContent(mResult);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<FileUploadDataResult>() {
                    @Override
                    public void accept(FileUploadDataResult uploadResult) throws Exception {
                        uploadDialog.dismiss();
                        if (uploadResult != null && uploadResult.getResponseData() != null
                                && uploadResult.getResponseData().getmFileInfos() != null) {
                            List<MFileInfo> fileInfos = uploadResult.getResponseData().getmFileInfos();
                            setFile_infos(fileInfos);
                        } else {
                            showErrorDialog(file);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        toastFullScreen("上传失败", false);
                        uploadDialog.dismiss();
                    }
                });
        uploadDialog.setCancelListener(new FileUploadDialog.CancelListener() {
            @Override
            public void cancel() {
                showCancelDialog(mSubscription, uploadDialog);
            }
        });
    }

    /*上传资源到临时文件*/
    private FileUploadResult commitFile(String url, File file, final FileUploadDialog dialog) throws Exception {
        Gson gson = new GsonBuilder().create();
        String resultStr = OkHttpClientManager.post(context, url, file, file.getName(), new OkHttpClientManager.ProgressListener() {
            @Override
            public void onProgress(long totalBytes, long remainingBytes, boolean done, File file) {
                Flowable.just(new long[]{totalBytes, remainingBytes}).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<long[]>() {
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
            String url = Constants.OUTRT_NET + "/m/movement/" + movementId + "/upload";
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
    private void showErrorDialog(final File file) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("上传结果");
        dialog.setMessage("由于网络问题上传资源失败，您可以点击重新上传再次上传");
        dialog.setNegativeTextColor(ContextCompat.getColor(context, R.color.gray));
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        dialog.setNegativeButton("取消", null);
        dialog.setPositiveButton("重新上传", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                upload(file);
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
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("你确定删除吗？");
        dialog.setNegativeButton("确定", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                delete();
            }
        });
        dialog.setPositiveButton("取消", null);
        dialog.show();
    }

    /*删除活动*/
    private void delete() {
        String url = Constants.OUTRT_NET + "/m/movement/" + movementId;
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
                    toastFullScreen("已成功删除，返回首页", true);
                    MessageEvent event = new MessageEvent();
                    event.action = Action.DELETE_MOVEMENT;
                    event.obj = movEntity;
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    toastFullScreen("删除失败", false);
                }
            }
        }, map));
    }

}
