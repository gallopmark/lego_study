package com.haoyu.app.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.AppCommentAdapter;
import com.haoyu.app.adapter.MFileInfoAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.AppActivityViewEntity;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.CommentEntity;
import com.haoyu.app.entity.CommentListResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.TimePeriod;
import com.haoyu.app.fragment.VideoPlayerFragment;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.HtmlTagHandler;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.GoodView;
import com.haoyu.app.view.LoadFailView;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;

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
 * 创建日期：2017/11/2.
 * 描述:评课议课
 * 作者:Administrator
 */
public class WSCDInfoActivity extends BaseActivity implements View.OnClickListener {
    private WSCDInfoActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.ll_outSideTop)
    LinearLayout ll_outSideTop;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_stage)
    TextView tv_stage;
    @BindView(R.id.tv_textBook)
    TextView tv_textBook;

    /*videoplayer layout*/
    @BindView(R.id.fl_video)
    FrameLayout fl_video;

    @BindView(R.id.ll_videoOutSide)
    LinearLayout ll_videoOutSide;
    @BindView(R.id.tv_content)
    TextView tv_content;
    @BindView(R.id.rv_file)
    RecyclerView rv_file;
    @BindView(R.id.ll_discussion)
    LinearLayout ll_discussion;
    @BindView(R.id.tv_discussCount)
    TextView tv_discussCount;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.tv_emptyComment)
    TextView tv_emptyComment;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.tv_more_reply)
    TextView tv_more_reply;
    @BindView(R.id.tv_bottomView)
    TextView tv_bottomView;
    private boolean running;
    private String activityId, activityTitle;
    private TimePeriod timePeriod;
    private AppActivityViewEntity.ClassDiscussEntity mVideoDC;
    private int smallHeight;
    private VideoPlayerFragment videoFragment;
    private int discussNum;//总回复数
    private AppCommentAdapter adapter;
    private List<CommentEntity> mComments = new ArrayList<>();
    private int replyPosition, childPosition;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_wstscdinfo;
    }

    @Override
    public void initView() {
        running = getIntent().getBooleanExtra("running", false);
        timePeriod = (TimePeriod) getIntent().getSerializableExtra("timePeriod");
        activityId = getIntent().getStringExtra("activityId");
        activityTitle = getIntent().getStringExtra("activityTitle");
        mVideoDC = (AppActivityViewEntity.ClassDiscussEntity) getIntent().getSerializableExtra("discussClass");
        setToolBar();
        showData();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AppCommentAdapter(context, mComments, getUserId());
        recyclerView.setAdapter(adapter);
        registRxBus();
    }

    private void setToolBar() {
        toolBar.setTitle_text("教学观摩");
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    private void showData() {
        setOutSideTop();
        setVideo_info(mVideoDC.getVideoFiles());
        setContext_text(mVideoDC.getSummary());
        setFile_info(mVideoDC.getAttchFiles());
    }

    private void setTime_text() {
        if (running) {
            if (timePeriod != null && timePeriod.getMinutes() > 0) {
                String time = "离活动结束还剩：" + TimeUtil.dateDiff(timePeriod.getMinutes());
                tv_time.setText(Html.fromHtml(time));
            } else {
                if (timePeriod != null && timePeriod.getState() != null) {
                    tv_time.setText("活动" + timePeriod.getState());
                } else {
                    tv_time.setText("活动进行中");
                }
            }
        } else {
            tv_time.setText("活动已结束");
        }
    }

    private void setOutSideTop() {
        setTime_text();
        tv_title.setText(activityTitle);
        String stage_text = "学段/学科：";
        if (mVideoDC.getStage() != null) {
            stage_text += mVideoDC.getStage();
        }
        if (mVideoDC.getSubject() != null) {
            stage_text += ("/" + mVideoDC.getSubject());
        }
        tv_stage.setText(stage_text);
        String textBook = "选用教材：";
        if (mVideoDC.getTextbook() != null) {
            textBook += mVideoDC.getTextbook();
        }
        tv_textBook.setText(textBook);
    }

    private void setVideo_info(List<MFileInfo> videoFiles) {
        if (videoFiles.size() > 0) {
            MFileInfo fileInfo = videoFiles.get(0);
            smallHeight = ScreenUtils.getScreenHeight(context) / 5 * 2;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, smallHeight);
            fl_video.setLayoutParams(params);
            videoFragment = new VideoPlayerFragment();
            Bundle bundle = new Bundle();
            bundle.putString("videoUrl", fileInfo.getUrl());
            bundle.putString("videoTitle", activityTitle);
            videoFragment.setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fl_video, videoFragment).commit();
            videoFragment.setOnRequestedOrientation(new VideoPlayerFragment.OnRequestedOrientation() {
                @Override
                public void onRequested(int orientation) {
                    setRequestedOrientation(orientation);
                }
            });
        }
    }

    private void setContext_text(String content) {
        if (content != null && content.trim().length() > 0) {
            Html.ImageGetter imageGetter = new HtmlHttpImageGetter(tv_content, Constants.REFERER, true);
            HtmlTagHandler tagHandler = new HtmlTagHandler(new HtmlTagHandler.OnImageClickListener() {
                @Override
                public void onImageClick(View view, String url) {
                    ArrayList<String> imgList = new ArrayList<>();
                    imgList.add(Constants.REFERER + url);
                    Intent intent = new Intent(context, AppMultiImageShowActivity.class);
                    intent.putStringArrayListExtra("photos", imgList);
                    startActivity(intent);
                }
            });
            Spanned spanned = Html.fromHtml(content, imageGetter, tagHandler);
            tv_content.setMovementMethod(LinkMovementMethod.getInstance());
            tv_content.setText(spanned);
            tv_content.setVisibility(View.VISIBLE);
        } else {
            tv_content.setVisibility(View.GONE);
        }
    }

    private void setFile_info(final List<MFileInfo> attchFiles) {
        if (attchFiles.size() > 0) {
            rv_file.setVisibility(View.VISIBLE);
            FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
            layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
            rv_file.setLayoutManager(layoutManager);
            MFileInfoAdapter adapter = new MFileInfoAdapter(attchFiles);
            rv_file.setAdapter(adapter);
            adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                    MFileInfo fileInfo = attchFiles.get(position);
                    Intent intent = new Intent(context, MFileInfoActivity.class);
                    intent.putExtra("fileInfo", fileInfo);
                    startActivity(intent);
                }
            });
        } else {
            rv_file.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 判断Android当前的屏幕是横屏还是竖屏。横竖屏判断
        setOrientattion(newConfig.orientation);
    }

    private void setOrientattion(int orientation) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {   //竖屏
            if (videoFragment != null) {
                videoFragment.setFullScreen(false);
            }
            showOutSize();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, smallHeight);
            fl_video.setLayoutParams(params);
        } else { //横屏
            if (videoFragment != null) {
                videoFragment.setFullScreen(true);
            }
            hideOutSize();
            int screeWidth = ScreenUtils.getScreenWidth(context);
            int screenHeight = ScreenUtils.getScreenHeight(context);
            int statusHeight = ScreenUtils.getStatusHeight(context);
            int realHeight = screenHeight - statusHeight;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screeWidth, realHeight);
            fl_video.setLayoutParams(params);
        }
    }

    private void showOutSize() {
        toolBar.setVisibility(View.VISIBLE);
        ll_outSideTop.setVisibility(View.VISIBLE);
        ll_videoOutSide.setVisibility(View.VISIBLE);
        tv_bottomView.setVisibility(View.VISIBLE);
    }

    private void hideOutSize() {
        toolBar.setVisibility(View.GONE);
        ll_outSideTop.setVisibility(View.GONE);
        ll_videoOutSide.setVisibility(View.GONE);
        tv_bottomView.setVisibility(View.GONE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void initData() {
        tv_more_reply.setVisibility(View.GONE);
        showTipDialog();
        String url = Constants.OUTRT_NET + "/m/comment?relation.id=" + activityId + "&relation.type=discuss_class&orders=CREATE_TIME.ASC&limit=5";
        addSubscription(Flowable.just(url).map(new Function<String, CommentListResult>() {
            @Override
            public CommentListResult apply(String url) throws Exception {
                return get(url);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<CommentListResult>() {
                    @Override
                    public void accept(CommentListResult response) throws Exception {
                        hideTipDialog();
                        updateUI(response);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        hideTipDialog();
                        loadFailView.setVisibility(View.VISIBLE);
                    }
                }));
    }

    private CommentListResult get(String url) throws Exception {
        Gson gson = new GsonBuilder().create();
        String listStr = OkHttpClientManager.getAsString(context, url);
        CommentListResult commentListResult = gson.fromJson(listStr, CommentListResult.class);
        if (commentListResult != null && commentListResult.getResponseData() != null &&
                commentListResult.getResponseData().getmComments() != null
                && commentListResult.getResponseData().getmComments().size() > 0) {
            for (int i = 0; i < commentListResult.getResponseData().getmComments().size(); i++) {
                String mainPostId = commentListResult.getResponseData().getmComments().get(i).getId();
                url = Constants.OUTRT_NET + "/m/comment?relation.id=" + activityId + "&relation.type=discuss_class"
                        + "&mainId=" + mainPostId + "&orders=CREATE_TIME.ASC";
                try {
                    String Jsonarr = OkHttpClientManager.getAsString(context, url);
                    CommentListResult childReplyListResult = gson.fromJson(Jsonarr, CommentListResult.class);
                    if (childReplyListResult.getResponseData() != null) {
                        commentListResult.getResponseData().getmComments().get(i).
                                setChildList(childReplyListResult.getResponseData().getmComments());
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return commentListResult;
    }

    private void updateUI(CommentListResult response) {
        mComments.clear();
        ll_discussion.setVisibility(View.VISIBLE);
        if (response != null && response.getResponseData() != null && response.getResponseData().getmComments() != null
                && response.getResponseData().getmComments().size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            mComments.addAll(response.getResponseData().getmComments());
            adapter.notifyDataSetChanged();
            if (response.getResponseData().getPaginator() != null) {
                if (response.getResponseData().getPaginator().getHasNextPage()) {
                    tv_more_reply.setText("查看所有评论");
                    tv_more_reply.setVisibility(View.VISIBLE);
                }
                discussNum = response.getResponseData().getPaginator().getTotalCount();
            }
        } else {
            setEmpty_text();
        }
        setDiscussCount(discussNum);
    }

    private void setEmpty_text() {
        tv_emptyComment.setVisibility(View.VISIBLE);
        String text = "目前还没人参与评论，\n赶紧去发表您的评论吧！";
        SpannableString ssb = new SpannableString(text);
        int start = text.indexOf("去") + 1;
        int end = text.indexOf("吧");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (running) {
                    showCommentDialog(false);
                } else {
                    showMaterialDialog("提示", "活动已结束,无法参与听课评课");
                }
            }
        };
        ssb.setSpan(clickableSpan, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.defaultColor)),
                start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_emptyComment.setMovementMethod(LinkMovementMethod.getInstance());
        tv_emptyComment.setText(ssb);
    }

    private void setDiscussCount(int discussNum) {
        String text = "已有 " + discussNum + " 回复";
        tv_discussCount.setText(text);
    }

    @Override
    public void setListener() {
        tv_more_reply.setOnClickListener(context);
        tv_bottomView.setOnClickListener(context);
        adapter.setCommentCallBack(new AppCommentAdapter.CommentCallBack() {
            @Override
            public void comment(int position, CommentEntity entity) {
                if (running) {
                    childPosition = position;
                    showCommentDialog(true);
                } else {
                    showMaterialDialog("提示", "活动已结束,无法参与听课评课");
                }
            }
        });

        adapter.setSupportCallBack(new AppCommentAdapter.SupportCallBack() {
            @Override
            public void support(int position, TextView tv_like) {
                createLike(position, tv_like);
            }
        });
        adapter.setMoreReplyCallBack(new AppCommentAdapter.MoreReplyCallBack() {
            @Override
            public void moreReply(int position, CommentEntity entity) {
                replyPosition = position;
                Intent intent = new Intent(context, AppMoreReplyActivity.class);
                intent.putExtra("entity", entity);
                intent.putExtra("relationType", "discuss_class");
                intent.putExtra("relationId", activityId);
                startActivity(intent);
            }
        });
        adapter.setDeleteMainComment(new AppCommentAdapter.DeleteMainComment() {
            @Override
            public void deleteMainComment(String id, int position) {
                if (running) {
                    deleteComment(id, position);
                } else {
                    showMaterialDialog("提示", "活动已结束,无法参与听课评课");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_more_reply:
                Intent intent = new Intent(context, AppMoreCommentActivity.class);
                intent.putExtra("relationId", activityId);
                intent.putExtra("relationType", "discuss_class");
                startActivity(intent);
                return;
            case R.id.tv_bottomView:
                if (running) {
                    showCommentDialog(false);
                } else {
                    showMaterialDialog("提示", "活动已结束,无法参与听课评课");
                }
                return;
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
        map.put("relation.id", activityId);
        map.put("relation.type", "discuss_class");
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
                    recyclerView.setVisibility(View.VISIBLE);
                    tv_emptyComment.setVisibility(View.GONE);
                    if (mComments.size() < 5) {
                        CommentEntity entity = response.getResponseData();
                        entity.setCreator(getCreator(entity.getCreator()));
                        mComments.add(entity);
                        adapter.notifyDataSetChanged();
                    } else {
                        tv_more_reply.setVisibility(View.VISIBLE);
                        toastFullScreen("评论成功", true);
                    }
                    discussNum++;
                    setDiscussCount(discussNum);
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
        map.put("relation.id", activityId);
        map.put("relation.type", "discuss_class");
        map.put("content", content);
        map.put("mainId", mComments.get(childPosition).getId());
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
                    int childNum = mComments.get(childPosition).getChildNum() + 1;
                    mComments.get(childPosition).setChildNum(childNum);
                    adapter.notifyDataSetChanged();
                    if (mComments.get(childPosition).getChildList().size() >= 10) {
                        toastFullScreen("评论成功", true);
                    } else {
                        CommentEntity entity = response.getResponseData();
                        entity.setCreator(getCreator(entity.getCreator()));
                        mComments.get(childPosition).getChildList().add(entity);
                        adapter.notifyDataSetChanged();
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
        String relationId = mComments.get(position).getId();
        HashMap<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", relationId);
        map.put("relation.type", "discussion_post");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            public void onError(Request request, Exception exception) {
                toast(context, "点赞失败");
            }

            public void onResponse(AttitudeMobileResult response) {
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    GoodView goodView = new GoodView(context);
                    int defaultColor = ContextCompat.getColor(context, R.color.defaultColor);
                    goodView.setTextInfo("+1", defaultColor, 15);
                    goodView.show(tvLike);
                    int count = mComments.get(position).getSupportNum() + 1;
                    mComments.get(position).setSupportNum(count);
                    adapter.notifyDataSetChanged();
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
                    mComments.remove(position);
                    adapter.notifyDataSetChanged();
                    if (mComments.size() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        tv_emptyComment.setVisibility(View.VISIBLE);
                    }
                    initData();
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
                int position = mComments.indexOf(mainComment);
                if (position != -1 && event.getBundle().getSerializable("childComment") != null
                        && event.getBundle().getSerializable("childComment") instanceof CommentEntity) {
                    int childPostNum = mComments.get(position).getChildNum();
                    mComments.get(position).setChildNum(childPostNum + 1);
                    CommentEntity childComment = (CommentEntity) event.getBundle().getSerializable("childComment");
                    if (mComments.get(position).getChildList() != null && mComments.get(position).getChildList().size() < 10) {
                        mComments.get(position).getChildList().add(childComment);
                    }
                    adapter.notifyDataSetChanged();
                }
            } else {
                int childPostNum = mComments.get(replyPosition).getChildNum();
                mComments.get(replyPosition).setChildNum(childPostNum + 1);
                adapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.CREATE_LIKE)) {
            if (event.obj != null && event.obj instanceof CommentEntity) {
                CommentEntity entity = (CommentEntity) event.obj;
                if (mComments.indexOf(entity) != -1) {
                    mComments.set(mComments.indexOf(entity), entity);
                    adapter.notifyDataSetChanged();
                }
            } else {
                int supportNum = mComments.get(replyPosition).getSupportNum();
                mComments.get(replyPosition).setSupportNum(supportNum + 1);
                adapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.DELETE_MAIN_COMMENT) && event.obj != null && event.obj instanceof CommentEntity) {       //在更多评论列表删除评论
            CommentEntity entity = (CommentEntity) event.getObj();
            if (mComments.contains(entity)) {
                mComments.remove(entity);
                adapter.notifyDataSetChanged();
                initData();
            }
        }
    }

}
