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
import android.widget.RelativeLayout;
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
import com.haoyu.app.entity.TeachingMovementSingleResult;
import com.haoyu.app.entity.TeachingRegistAtResult;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.MediaFile;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.ExpandableTextView;
import com.haoyu.app.view.GoodView;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.StickyScrollView;

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
public class TeachingResearchATActivity extends BaseActivity implements View.OnClickListener {
    private TeachingResearchATActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.empty_detail)
    TextView empty_detail;
    @BindView(R.id.scrollView)
    StickyScrollView scrollView;
    @BindView(R.id.detailLayout)
    LinearLayout detailLayout;
    @BindView(R.id.loadFailView1)
    LoadFailView loadFailView1;
    @BindView(R.id.layoutBottom)
    RelativeLayout layoutBottom;
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
    @BindView(R.id.at_content)
    ExpandableTextView at_content;  //活动内容
    @BindView(R.id.empty_content)
    View empty_content;
    @BindView(R.id.videoRV)
    RecyclerView videoRV;  //活动视频列表
    private List<MFileInfo> mFileInfos = new ArrayList<>();
    private FileAdapter fileAdapter;
    @BindView(R.id.empty_resources)
    View empty_resources;  //空活动花絮
    @BindView(R.id.at_comment)
    TextView at_comment;  //活动点评
    @BindView(R.id.commentRV)
    RecyclerView commentRV;  //活动评论列表
    @BindView(R.id.tv_more_reply)
    TextView tv_more_reply;  //查看更多评论
    @BindView(R.id.empty_comment)
    LinearLayout empty_comment;   //空评论列表
    @BindView(R.id.tv_comment)
    TextView tv_comment;
    private List<CommentEntity> commentList = new ArrayList<>();
    private AppCommentAdapter commentAdapter;
    @BindView(R.id.tv_viewNum)
    TextView tv_viewNum;
    @BindView(R.id.tv_joinNum)
    TextView tv_joinNum;
    @BindView(R.id.bt_type)
    Button bt_type;
    private String acId;
    private int page = 1;
    private boolean register;
    private String registerId;
    private int participateNum;  //活动参与数
    private List<String> imgList = new ArrayList<>();
    private int replyPosition, childPosition;
    private File uploadFile;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teaching_research_at;
    }

    @Override
    public void initView() {
        acId = getIntent().getStringExtra("id");
        /*设置活动封面占屏幕高度的1/4*/
        LinearLayout.LayoutParams imgParams = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.getScreenHeight(context) / 7 * 2);
        at_img.setLayoutParams(imgParams);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        videoRV.setLayoutManager(layoutManager);
        fileAdapter = new FileAdapter(mFileInfos);
        videoRV.setAdapter(fileAdapter);
        commentAdapter = new AppCommentAdapter(context, commentList, getUserId());
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        commentRV.setNestedScrollingEnabled(false);
        commentRV.setLayoutManager(manager);
        commentRV.setAdapter(commentAdapter);
        registRxBus();
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/movement/view/" + acId;
        OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<TeachingMovementSingleResult>() {
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
            public void onResponse(TeachingMovementSingleResult singleResult) {
                loadingView.setVisibility(View.GONE);
                if (singleResult != null && singleResult.getResponseData() != null) {
                    updateUI(singleResult.getResponseData());
                    getComment();
                } else {
                    empty_detail.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /*更新活动相关信息*/
    private void updateUI(TeachingMovementEntity entity) {
        GlideImgManager.loadImage(context, entity.getImage(), R.drawable.app_default, R.drawable.app_default, at_img);
        at_title.setText(entity.getTitle());
        if (entity.getmMovementRelations() != null && entity.getmMovementRelations().size() > 0) {
            TeachingMovementEntity.MovementRelation relation = entity.getmMovementRelations().get(0);
            if (relation.getTimePeriod() != null) {
                tv_time.setText(TimeUtil.convertDayOfMinute(relation.getTimePeriod().getStartTime(),
                        relation.getTimePeriod().getEndTime()));
            }
            if (relation.getTimePeriod() != null) {
                long endTime = relation.getTimePeriod().getEndTime();
                tv_apply.setText("截止" + TimeUtil.convertDayOfMinute(endTime));
            }
            participateNum = relation.getParticipateNum();
            int limit = relation.getTicketNum();
            tv_limit.setText(participateNum + "/" + limit + "人");
            tv_viewNum.setText(String.valueOf(relation.getBrowseNum()));
            tv_joinNum.setText(String.valueOf(relation.getParticipateNum()));
        }
        tv_address.setText(entity.getLocation());
        tv_host.setText(entity.getSponsor());
        if (entity.getType() != null && entity.getType().equals("communicationMeeting")) {
            tv_atType.setText("跨校交流会");
        } else if (entity.getType() != null && entity.getType().equals("expertInteraction")) {
            tv_atType.setText("专家互动");
        } else if (entity.getType() != null && entity.getType().equals("lessonViewing")) {
            tv_atType.setText("创课观摩");
        }
        if (entity.getParticipationType() != null && entity.getParticipationType().equals("ticket")) {
            tv_participation.setText("须报名预约，凭电子票入场");
        } else if (entity.getParticipationType() != null && entity.getParticipationType().equals("free")) {
            tv_participation.setText("在线报名，免费入场");
        } else if (entity.getParticipationType() != null && entity.getParticipationType().equals("chair")) {
            tv_participation.setText("讲座视频录像+在线问答交流");
        }
        if (entity.getContent() != null && entity.getContent().length() > 0) {
            at_content.setVisibility(View.VISIBLE);
            at_content.setHtmlText(entity.getContent());
        } else {
            at_content.setVisibility(View.GONE);
            empty_content.setVisibility(View.VISIBLE);
        }
        if (entity.getmFileInfos() != null && entity.getmFileInfos().size() > 0) {
            videoRV.setVisibility(View.VISIBLE);
            empty_resources.setVisibility(View.GONE);
            for (MFileInfo mFileInfo : entity.getmFileInfos()) {
                imgList.add(mFileInfo.getUrl());
            }
            mFileInfos.addAll(entity.getmFileInfos());
            fileAdapter.notifyDataSetChanged();
        } else {
            videoRV.setVisibility(View.GONE);
            empty_resources.setVisibility(View.VISIBLE);
        }
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
        if (entity.getCreator() != null && entity.getCreator().getId() != null && entity.getCreator().getId().equals(getUserId())) {
            toolBar.setShow_right_button(true);
        } else {
            toolBar.setShow_right_button(false);
        }
        detailLayout.setVisibility(View.VISIBLE);
        layoutBottom.setVisibility(View.VISIBLE);
    }

    private void getComment() {
        tv_more_reply.setVisibility(View.GONE);
        String url = Constants.OUTRT_NET + "/m/comment?relation.id=" + acId
                + "&relation.type=movement&page=" + page + "&limit=5" + "&orders=CREATE_TIME.DESC";
        addSubscription(Flowable.just(url).map(new Function<String, CommentListResult>() {
            @Override
            public CommentListResult apply(String url) throws Exception {
                return get(url);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<CommentListResult>() {
                    @Override
                    public void accept(CommentListResult commentListResult) throws Exception {
                        if (commentListResult != null && commentListResult.getResponseData() != null &&
                                commentListResult.getResponseData().getmComments() != null) {
                            updateListUI(commentListResult.getResponseData().getmComments(), commentListResult.getResponseData().getPaginator());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        loadFailView1.setVisibility(View.VISIBLE);
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
                url = Constants.OUTRT_NET + "/m/comment?relation.id=" + acId + "&relation.type=movement"
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
            empty_comment.setVisibility(View.VISIBLE);
        }
    }

    class FileAdapter extends BaseArrayRecyclerAdapter<MFileInfo> {

        public FileAdapter(List<MFileInfo> mDatas) {
            super(mDatas);
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
        loadFailView1.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getComment();
            }
        });
        at_comment.setOnClickListener(context);
        tv_comment.setOnClickListener(context);
        at_content.setOnExpandStateChangeListener(new ExpandableTextView.OnExpandStateChangeListener() {
            @Override
            public void onExpandStateChanged(TextView textView, boolean isExpanded) {
                if (!isExpanded) {
                    scrollView.smoothScrollTo(0, textView.getBottom());
                }
            }
        });
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
                intent.putExtra("relationId", acId);
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
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            public void onError(Request request, Exception exception) {
                toast(context, "点赞失败");
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
        }, map);
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
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    commentList.remove(position);
                    commentAdapter.notifyDataSetChanged();
                    if (commentList.size() == 0) {
                        commentRV.setVisibility(View.GONE);
                        empty_comment.setVisibility(View.VISIBLE);
                    }
                    getComment();
                }
            }
        }, map);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_comment:
                showCommentDialog(false);
                break;
            case R.id.at_comment:
                showCommentDialog(false);
                break;
            case R.id.tv_more_reply:
                Intent intent = new Intent(context, AppMoreCommentActivity.class);
                intent.putExtra("relationId", acId);
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


    private void showBottomDialog() {
        View view = getLayoutInflater().inflate(
                R.layout.dialog_teaching_at, null);
        final AlertDialog bottomDialog = new AlertDialog.Builder(context).create();
        View tv_video = view.findViewById(R.id.tv_video);
        View tv_photo = view.findViewById(R.id.tv_photo);
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
                    case R.id.tv_video:
                        picketVideo();
                        break;
                    case R.id.tv_photo:
                        pickerPicture();
                        break;
                    case R.id.tv_share:
                        toast(context, "暂不支持");
                        break;
                    case R.id.tv_edit:

                        break;
                    case R.id.tv_delete:
                        showTipsDialog();
                        break;
                }
            }
        };
        tv_video.setOnClickListener(listener);
        tv_photo.setOnClickListener(listener);
        tv_share.setOnClickListener(listener);
        tv_delete.setOnClickListener(listener);
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

    private void pickerPicture() {
        MediaOption option = new MediaOption.Builder().setSelectType(MediaOption.TYPE_IMAGE)
                .setShowCamera(true)
                .build();
        MediaPicker.getInstance().init(option).selectMedia(context, onSelectMediaListener);
    }

    private void picketVideo() {
        MediaOption option = new MediaOption.Builder().setSelectType(MediaOption.TYPE_VIDEO)
                .setShowCamera(true)
                .build();
        MediaPicker.getInstance().init(option).selectMedia(context, onSelectMediaListener);
    }


    private MediaPicker.onSelectMediaCallBack onSelectMediaListener = new MediaPicker.onSelectMediaCallBack() {
        @Override
        public void onSelected(String path) {
            uploadFile = new File(path);
            upload();
        }
    };

    private void upload() {
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
                        public void accept(FileUploadDataResult uploadResult) throws Exception {
                            uploadDialog.dismiss();
                            if (uploadResult != null && uploadResult.getResponseData() != null
                                    && uploadResult.getResponseData().getmFileInfos() != null) {
                                mFileInfos.addAll(uploadResult.getResponseData().getmFileInfos());
                                fileAdapter.notifyDataSetChanged();
                                for (MFileInfo mFileInfo : uploadResult.getResponseData().getmFileInfos()) {
                                    imgList.add(mFileInfo.getUrl());
                                }
                                videoRV.setVisibility(View.VISIBLE);
                                empty_resources.setVisibility(View.GONE);
                                toastFullScreen("上传成功", true);
                            } else {
                                showErrorDialog();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
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
            String url = Constants.OUTRT_NET + "/m/movement/" + acId + "/upload";
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
                upload();
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
                deleteAt();
            }
        });
        materialDialog.setPositiveButton("取消", null);
        materialDialog.show();
    }

    /*删除活动*/
    private void deleteAt() {
        String url = Constants.OUTRT_NET + "/m/movement/" + acId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
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
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    MessageEvent event = new MessageEvent();
                    event.action = Action.DELETE_MOVEMENT;
                    RxBus.getDefault().post(event);
                    toastFullScreen("已成功删除，返回首页", true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 3000);
                } else {
                    toastFullScreen("删除失败", false);
                }
            }
        }, map);
    }

    private void registerActivity() {
        String url = Constants.OUTRT_NET + "/m/movement/register";
        Map<String, String> map = new HashMap<>();
        map.put("movement.id", acId);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<TeachingRegistAtResult>() {
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
            public void onResponse(TeachingRegistAtResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null && response.getResponseData().getId() != null) {
                    registerId = response.getResponseData().getId();
                    register = false;
                    bt_type.setText("取消报名");
                    participateNum += 1;
                    tv_joinNum.setText(String.valueOf(participateNum));
                    toast(context, "报名成功");
                    MessageEvent event = new MessageEvent();
                    event.action = Action.REGIST_MOVEMENT;
                    event.obj = registerId;
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
                toast(context, "与服务器连接失败");
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    register = true;
                    bt_type.setText("报名参与");
                    toast(context, "已取消报名");
                    participateNum -= 1;
                    if (participateNum <= 0) {
                        participateNum = 0;
                    }
                    tv_joinNum.setText(String.valueOf(participateNum));
                    MessageEvent event = new MessageEvent();
                    event.action = Action.UNREGIST_MOVEMENT;
                    RxBus.getDefault().post(event);
                } else {
                    toast(context, "取消报名失败");
                }
            }
        }, map));
    }

    /*发送主评论*/
    private void sendMainComment(String content) {
        String url = Constants.OUTRT_NET + "/m/comment";
        Map<String, String> map = new HashMap<>();
        map.put("relation.id", acId);
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
                    empty_comment.setVisibility(View.GONE);
                    CommentEntity entity = response.getResponseData();
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
                    if (commentList.size() < 5) {
                        commentList.add(response.getResponseData());
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

    /*发送子评论*/
    private void sendChildComment(String content) {
        String url = Constants.OUTRT_NET + "/m/comment";
        Map<String, String> map = new HashMap<>();
        map.put("relation.id", acId);
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
                    CommentEntity entity = response.getResponseData();
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
                    int childNum = commentList.get(childPosition).getChildNum() + 1;
                    commentList.get(childPosition).setChildNum(childNum);
                    commentAdapter.notifyDataSetChanged();
                    if (commentList.get(childPosition).getChildList().size() >= 10) {
                        toastFullScreen("评论成功", true);
                    } else {
                        commentList.get(childPosition).getChildList().add(response.getResponseData());
                        commentAdapter.notifyDataSetChanged();
                    }
                } else {
                    toastFullScreen("评论失败", false);
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

}
