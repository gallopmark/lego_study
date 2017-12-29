package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
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
import com.haoyu.app.entity.TeachingLessonAttribute;
import com.haoyu.app.entity.TeachingLessonData;
import com.haoyu.app.entity.TeachingLessonEntity;
import com.haoyu.app.filePicker.LFilePicker;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.HtmlTagHandler;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.PixelFormat;
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
 * 创建日期：2017/10/25 on 17:54
 * 描述:创课详情页
 * 作者:马飞奔 Administrator
 */
public class CmtsLsonInfoActivity extends BaseActivity implements View.OnClickListener {
    private CmtsLsonInfoActivity context = this;
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
    @BindView(R.id.mRrogressBar)
    RoundRectProgressBar mRrogressBar; //创课进度
    @BindView(R.id.tv_day)
    TextView tv_day; //创课剩余天数
    @BindView(R.id.tv_title)
    TextView tv_title; //创课标题
    @BindView(R.id.iv_userIco)
    ImageView iv_userIco; //创建人头像
    @BindView(R.id.tv_userName)
    TextView tv_userName; //创建人
    @BindView(R.id.tv_createTime)
    TextView tv_createTime; //创建时间
    @BindView(R.id.tv_heatNum)
    TextView tv_heatNum;  //热度
    @BindView(R.id.tv_supportNum)
    TextView tv_supportNum; //点赞数
    @BindView(R.id.tv_adviseNum)
    TextView tv_adviseNum; //提建议
    /***************/
    @BindView(R.id.tag_introduce)
    LinearLayout tag_introduce;   //创课介绍
    @BindView(R.id.iv_introduce)
    ImageView iv_introduce;  //创课介绍展开或收起
    @BindView(R.id.ll_introduce)
    LinearLayout ll_introduce;   //创课介绍详细布局
    /***************/
    @BindView(R.id.layout_frame)
    LinearLayout layout_frame;
    @BindView(R.id.tag_frame)
    LinearLayout tag_frame;  //课程框架设计
    @BindView(R.id.iv_frame)
    ImageView iv_frame; //课程框架设计展开或收起
    @BindView(R.id.ll_frame)
    LinearLayout ll_frame; //框架设计内容布局
    /***************/
    @BindView(R.id.layout_activity)
    LinearLayout layout_activity;
    @BindView(R.id.tag_activity)
    LinearLayout tag_activity;  //教学活动设计
    @BindView(R.id.iv_activity)
    ImageView iv_activity;  //教学活动设计展开或收起
    @BindView(R.id.ll_activity)
    LinearLayout ll_activity;  //教学活动设计详细布局
    @BindView(R.id.tv_checkAll)
    TextView tv_checkAll; //查看全部资源按钮
    @BindView(R.id.loadingFile)
    LoadingView loadingFile; //加载资源进度提示
    @BindView(R.id.loadfileError)
    LoadFailView loadfileError;  //加载资源失败
    @BindView(R.id.rv_file)
    RecyclerView rv_file; //资源文件列表
    @BindView(R.id.empty_resources)
    LinearLayout empty_resources; //空资源文件
    @BindView(R.id.tv_adviseCount)
    TextView tv_adviseCount;  //收到建议数
    @BindView(R.id.errorAdvise)
    LoadFailView errorAdvise;  //加载建议失败
    @BindView(R.id.tv_emptyAdvise)
    TextView tv_emptyAdvise;    //空建议列表
    @BindView(R.id.rv_advise)
    RecyclerView rv_advise;   //建议列表
    @BindView(R.id.tv_more_reply)
    TextView tv_more_reply;  //更多建议按钮
    @BindView(R.id.bottomView)
    TextView bottomView; //底部提建议按钮
    private TeachingLessonEntity lessonEntity;
    private String lessonId, relationId;  //创课id，关联关系id
    private int supportNum, adviseNum;  //点赞数，提建议数
    private List<ReplyEntity> adviseList = new ArrayList<>();
    private AppDiscussionAdapter adviseAdapter;
    private int childPosition, replyPosition;
    private String replyUrl;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_cmtslsoninfo;
    }

    @Override
    public void initView() {
        context = this;
        setToolBar();
        lessonId = getIntent().getStringExtra("lessonId");
        String title = getResources().getString(R.string.gen_class_detail);
        String empty_text = getResources().getString(R.string.gen_class_emptylist);
        toolBar.setTitle_text(title);
        tv_empty.setText(empty_text);
        LinearLayoutManager llm_file = new LinearLayoutManager(context);
        llm_file.setOrientation(LinearLayoutManager.VERTICAL);
        rv_file.setLayoutManager(llm_file);
        LinearLayoutManager llm_advise = new LinearLayoutManager(context);
        llm_advise.setOrientation(LinearLayoutManager.VERTICAL);
        rv_advise.setLayoutManager(llm_advise);
        adviseAdapter = new AppDiscussionAdapter(context, adviseList, getUserId());
        rv_advise.setAdapter(adviseAdapter);
        registRxBus();
        replyUrl = Constants.OUTRT_NET + "/m/discussion/post?discussionUser.discussionRelation.id=" + relationId + "&orders=CREATE_TIME.ASC";
    }

    private void setToolBar() {
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
    }

    @Override
    public void initData() {
        final String url = Constants.OUTRT_NET + "/m/lesson/cmts/view/" + lessonId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<TeachingLessonData>>() {
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
            public void onResponse(BaseResponseResult<TeachingLessonData> result) {
                loadingView.setVisibility(View.GONE);
                if (result != null && result.getResponseData() != null && result.getResponseData().getmLesson() != null) {
                    updateUI(result.getResponseData().getmLesson(), result.getResponseData().getmLessonAttribute());
                    getFiles();
                    getAdvise();
                } else {
                    tv_empty.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateUI(TeachingLessonEntity mLesson, TeachingLessonAttribute attribute) {
        ssv_content.setVisibility(View.VISIBLE);
        lessonEntity = mLesson;
        if (lessonEntity.getCreator() != null && lessonEntity.getCreator().getId() != null && lessonEntity.getCreator().getId().equals(getUserId())) {
            toolBar.setShow_right_button(true);
        }
        if (lessonEntity.getmDiscussionRelations() != null && lessonEntity.getmDiscussionRelations().size() > 0) {
            relationId = lessonEntity.getmDiscussionRelations().get(0).getId();
        }
        int remainDay = lessonEntity.getRemainDay();
        setRemainDay(remainDay);
        setLesson(lessonEntity);
        if (attribute != null) {
            setLessonAttribute(attribute);
        }
    }

    private void setRemainDay(int remainDay) {
        mRrogressBar.setMax(60);
        mRrogressBar.setProgress(60 - remainDay);
        if (remainDay <= 0) {
            tv_day.setText("已结束");
        } else {
            tv_day.setText("还剩" + remainDay + "天");
        }
    }

    public void setLesson(TeachingLessonEntity mLesson) {
        tv_title.setText(mLesson.getTitle());
        if (mLesson.getCreator() != null && mLesson.getCreator().getAvatar() != null)
            GlideImgManager.loadCircleImage(context, mLesson.getCreator().getAvatar(),
                    R.drawable.user_default, R.drawable.user_default, iv_userIco);
        else
            iv_userIco.setImageResource(R.drawable.user_default);
        if (mLesson.getCreator() != null)
            tv_userName.setText(mLesson.getCreator().getRealName());
        tv_createTime.setText("发布于" + TimeUtil.getSlashDate(mLesson.getCreateTime()));
        int browseNum = 0, adviseNum = 0;
        if (mLesson.getmDiscussionRelations() != null && mLesson.getmDiscussionRelations().size() > 0) {
            browseNum = mLesson.getmDiscussionRelations().get(0).getBrowseNum();
            supportNum = mLesson.getmDiscussionRelations().get(0).getSupportNum();
            adviseNum = mLesson.getmDiscussionRelations().get(0).getReplyNum();
        }
        tv_heatNum.setText("热度（" + browseNum + "）");
        tv_supportNum.setText("赞（" + supportNum + "）");
        tv_adviseNum.setText("提建议（" + adviseNum + "）");
        if (!isEmpty(mLesson.getContent())) {
            String title = getResources().getString(R.string.gen_class_topic);
            addView(title, mLesson.getContent(), ll_introduce);
        }
    }

    public void setLessonAttribute(TeachingLessonAttribute att) {
        if (!isEmpty(att.getRealia())) {
            String realia = getResources().getString(R.string.gen_class_realia);
            addView(realia, att.getRealia(), ll_introduce);
        }
        if (!isEmpty(att.getRealiaSummary())) {
            String realiaSummary = getResources().getString(R.string.gen_class_realiaSummary);
            addView(realiaSummary, att.getRealiaSummary(), ll_introduce);
        }
        if (!isEmpty(att.getRealiaUseReason())) {
            String userReason = getResources().getString(R.string.gen_class_realiaUseReason);
            addView(userReason, att.getRealiaUseReason(), ll_introduce);
        }
        if (isEmpty(att.getTopicBase()) && isEmpty(att.getLearnDetail()) && isEmpty(att.getDesignPrinciple())) {
            layout_frame.setVisibility(View.GONE);
        } else {
            Resources resources = getResources();
            if (!isEmpty(att.getTopicBase())) {
                String topicBase = resources.getString(R.string.gen_class_topicBase);
                addView(topicBase, att.getTopic(), ll_frame);
            }
            if (!isEmpty(att.getLearnDetail())) {
                String learnDetail = resources.getString(R.string.gen_class_learnDetail);
                addView(learnDetail, att.getLearnDetail(), ll_frame);
            }
            if (!isEmpty(att.getDesignPrinciple())) {
                String designPrinciple = resources.getString(R.string.gen_class_designPrinciple);
                addView(designPrinciple, att.getDesignPrinciple(), ll_frame);
            }
        }
        if (isEmpty(att.getStemElement()) && isEmpty(att.getExamples()) && isEmpty(att.getModels()) && isEmpty(att.getRethink()) && isEmpty(att.getExpand())) {
            layout_activity.setVisibility(View.GONE);
        } else {
            Resources resources = getResources();
            if (!isEmpty(att.getStemElement())) {
                String stem = resources.getString(R.string.gen_class_stem_element);
                addView(stem, att.getStemElement(), ll_activity);
            }
            if (!isEmpty(att.getExamples())) {
                String examples = resources.getString(R.string.gen_class_examples);
                addView(examples, att.getExamples(), ll_activity);
            }
            if (!isEmpty(att.getModels())) {
                String models = resources.getString(R.string.gen_class_models);
                addView(models, att.getModels(), ll_activity);
            }
            if (!isEmpty(att.getRethink())) {
                String rethink = resources.getString(R.string.gen_class_rethink);
                addView(rethink, att.getRethink(), ll_activity);
            }
            if (!isEmpty(att.getExpand())) {
                String expand = resources.getString(R.string.gen_class_expand);
                addView(expand, att.getExpand(), ll_activity);
            }
        }
        tag_introduce.setOnClickListener(new View.OnClickListener() {
            boolean isExpand = false;

            @Override
            public void onClick(View view) {
                if (isExpand) {
                    ll_introduce.setVisibility(View.VISIBLE);
                    iv_introduce.setImageResource(R.drawable.course_dictionary_shouqi);
                    isExpand = false;
                } else {
                    ssv_content.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ssv_content.smoothScrollBy(0, tag_introduce.getTop());
                        }
                    }, 10);
                    ll_introduce.setVisibility(View.GONE);
                    iv_introduce.setImageResource(R.drawable.course_dictionary_xiala);
                    isExpand = true;
                }
            }
        });
        tag_frame.setOnClickListener(new View.OnClickListener() {
            private boolean isExpand = true;

            @Override
            public void onClick(View view) {
                if (isExpand) {
                    ll_frame.setVisibility(View.VISIBLE);
                    iv_frame.setImageResource(R.drawable.course_dictionary_shouqi);
                    isExpand = false;
                } else {
                    ssv_content.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ssv_content.smoothScrollBy(0, tag_frame.getTop());
                        }
                    }, 10);
                    ll_frame.setVisibility(View.GONE);
                    iv_frame.setImageResource(R.drawable.course_dictionary_xiala);
                    isExpand = true;
                }
            }
        });
        tag_activity.setOnClickListener(new View.OnClickListener() {
            private boolean isExpand = true;

            @Override
            public void onClick(View view) {
                if (isExpand) {
                    ll_activity.setVisibility(View.VISIBLE);
                    iv_activity.setImageResource(R.drawable.course_dictionary_shouqi);
                    isExpand = false;
                } else {
                    ssv_content.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ssv_content.smoothScrollBy(0, tag_activity.getTop());
                        }
                    }, 10);
                    ll_activity.setVisibility(View.GONE);
                    iv_activity.setImageResource(R.drawable.course_dictionary_xiala);
                    isExpand = true;
                }
            }
        });
    }

    private boolean isEmpty(String text) {
        if (text == null)
            return true;
        return text.trim().length() == 0;
    }

    private void addView(String title, String content, LinearLayout parent) {
        if (parent.getVisibility() != View.VISIBLE) {
            parent.setVisibility(View.VISIBLE);
        }
        TextView tv_title = createTitleView();
        tv_title.setText(title);
        TextView tv_content = createContentView();
        setSpannedText(tv_content, content);
        parent.addView(tv_title);
        parent.addView(tv_content);
    }

    private TextView createTitleView() {
        TextView tv = new TextView(context);
        tv.setTextSize(16);
        tv.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        return tv;
    }

    private TextView createContentView() {
        TextView tv = new TextView(context);
        tv.setTextSize(16);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = PixelFormat.dp2px(context, 12);
        params.bottomMargin = PixelFormat.dp2px(context, 12);
        tv.setLayoutParams(params);
        return tv;
    }

    private void setSpannedText(TextView tv, String text) {
        Html.ImageGetter imageGetter = new HtmlHttpImageGetter(tv, Constants.REFERER, true);
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
        if (text != null) {
            Spanned spanned = Html.fromHtml(text, imageGetter, tagHandler);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setText(spanned);
        } else {
            tv.setText(null);
        }
    }

    private void getFiles() {
        String url = Constants.OUTRT_NET + "/m/file?fileRelations[0].relation.id=" + lessonId + "&fileRelations[0].relation.type=discussion&limit=2&orders=CREATE_TIME.DESC";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MFileInfoData>>() {
            @Override
            public void onBefore(Request request) {
                loadingFile.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingFile.setVisibility(View.GONE);
                loadfileError.setVisibility(View.VISIBLE);
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
                    intent.putExtra("relationId", lessonId);
                    intent.putExtra("relationType", "discussion");
                    startActivity(intent);
                }
            });
        } else {
            empty_resources.setVisibility(View.VISIBLE);
        }
    }

    private void getAdvise() {
        String url = replyUrl + "&page=1" + "&limit=5";
        addSubscription(Flowable.just(url).map(new Function<String, ReplyListResult>() {
            @Override
            public ReplyListResult apply(String url) throws Exception {
                return getReply(url);
            }
        }).map(new Function<ReplyListResult, ReplyListResult>() {
            @Override
            public ReplyListResult apply(ReplyListResult result) throws Exception {
                if (result != null && result.getResponseData() != null && result.getResponseData().getmDiscussionPosts().size() > 0) {
                    return getChildReply(result, result.getResponseData().getmDiscussionPosts());
                }
                return result;
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
                        errorAdvise.setVisibility(View.VISIBLE);
                    }
                }));
    }

    /*获取主回复*/
    private ReplyListResult getReply(String url) throws Exception {
        String json = OkHttpClientManager.getAsString(context, url);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, ReplyListResult.class);
    }

    /*通过主回复id获取子回复*/
    private ReplyListResult getChildReply(ReplyListResult result, List<ReplyEntity> list) {
        for (int i = 0; i < list.size(); i++) {
            String mainPostId = list.get(i).getId();
            String url = replyUrl + "&mainPostId=" + mainPostId;
            try {
                ReplyListResult mResult = getReply(url);
                if (mResult != null && mResult.getResponseData() != null) {
                    List<ReplyEntity> childList = mResult.getResponseData().getmDiscussionPosts();
                    result.getResponseData().getmDiscussionPosts().get(i).setChildReplyEntityList(childList);
                }
            } catch (Exception e) {
                continue;
            }
        }
        return result;
    }

    private void updateAdvise(List<ReplyEntity> mDatas, Paginator paginator) {
        adviseList.clear();
        if (mDatas.size() > 0) {
            adviseList.addAll(mDatas);
            adviseAdapter.notifyDataSetChanged();
            if (paginator != null) {
                adviseNum = paginator.getTotalCount();
                setAdvise(adviseNum);
                if (paginator.getHasNextPage()) {
                    tv_more_reply.setVisibility(View.VISIBLE);
                }
            }
            rv_advise.setVisibility(View.VISIBLE);
        } else {
            setEmpty_text();
            rv_advise.setVisibility(View.GONE);
        }
        bottomView.setVisibility(View.VISIBLE);
    }

    private void setEmpty_text() {
        tv_emptyAdvise.setVisibility(View.VISIBLE);
        String text = "目前还没人提建议，\n赶紧去发表您的建议吧！";
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
        tv_emptyAdvise.setMovementMethod(LinkMovementMethod.getInstance());
        tv_emptyAdvise.setText(ssb);
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        tv_supportNum.setOnClickListener(context);
        tv_adviseNum.setOnClickListener(context);
        loadfileError.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getFiles();
            }
        });
        errorAdvise.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                getAdvise();
            }
        });
        tv_more_reply.setOnClickListener(context);
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
                    int childPostCount = adviseList.get(position).getChildPostCount() + 1;
                    adviseList.get(position).setChildPostCount(childPostCount);
                    if (adviseList.get(position).getChildReplyEntityList() != null && adviseList.get(position).getChildReplyEntityList().size() < 10) {
                        ReplyEntity entity = response.getResponseData();
                        entity.setCreator(getCreator(entity.getCreator()));
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
                        tv_emptyAdvise.setVisibility(View.VISIBLE);
                        rv_advise.setVisibility(View.GONE);
                    }
                    adviseNum--;
                    setAdvise(adviseNum);
                    getAdvise();
                }
            }
        }, map));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_error:
                getFiles();
                break;
            case R.id.tv_supportNum:
                createLike();
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
        map.put("relation.id", lessonId);
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
                    goodView.show(tv_supportNum);
                    supportNum++;
                    tv_supportNum.setText("赞（" + supportNum + "）");
                    MessageEvent event = new MessageEvent();
                    event.action = Action.SUPPORT_STUDY_CLASS;
                    if (lessonEntity.getmDiscussionRelations() != null && lessonEntity.getmDiscussionRelations().size() > 0) {
                        lessonEntity.getmDiscussionRelations().get(0).setSupportNum(supportNum);
                    }
                    event.obj = lessonEntity;
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
                    rv_advise.setVisibility(View.VISIBLE);
                    tv_emptyAdvise.setVisibility(View.GONE);
                    if (adviseList.size() < 5) {
                        ReplyEntity entity = response.getResponseData();
                        entity.setCreator(getCreator(entity.getCreator()));
                        adviseList.add(entity);
                        adviseAdapter.notifyDataSetChanged();
                        if (rv_advise.getVisibility() != View.VISIBLE) {
                            rv_advise.setVisibility(View.VISIBLE);
                        }
                    } else {
                        tv_more_reply.setVisibility(View.VISIBLE);
                        toastFullScreen("发送成功", true);
                    }
                    adviseNum++;
                    setAdvise(adviseNum);
                    MessageEvent event = new MessageEvent();
                    event.action = Action.GIVE_STUDY_ADVICE;
                    if (lessonEntity.getmDiscussionRelations() != null && lessonEntity.getmDiscussionRelations().size() > 0) {
                        lessonEntity.getmDiscussionRelations().get(0).setReplyNum(adviseNum);
                    }
                    event.obj = lessonEntity;
                    RxBus.getDefault().post(event);
                } else {
                    toastFullScreen("发送失败", true);
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

    private void setAdvise(int adviseNum) {
        tv_adviseCount.setText("收到 " + adviseNum + " 条建议");
    }

    private void showBottomDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_teaching_cc, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        TextView tv_upload = view.findViewById(R.id.tv_upload);
        TextView tv_delete = view.findViewById(R.id.tv_delete);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_upload:
                        openFilePicker();
                        break;
                    case R.id.tv_delete:
                        showTipsDialog();
                        break;
                    case R.id.tv_cancel:
                        break;
                }
                dialog.dismiss();
            }
        };
        tv_upload.setOnClickListener(listener);
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
                File file = new File(filePath);
                uploadFile(file);
            }
        }
    }

    private void uploadFile(final File file) {
        if (file != null && file.exists()) {
            String url = Constants.OUTRT_NET + "/m/file/uploadTemp";
            final FileUploadDialog dialog = new FileUploadDialog(context, file.getName(), "正在上传");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            final Disposable mSubscription = Flowable.just(url).map(new Function<String, FileUploadResult>() {
                @Override
                public FileUploadResult apply(String url) throws Exception {
                    return commitFile(url, dialog, file);
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
                            dialog.dismiss();
                            if (response != null && response.getResponseCode() != null &&
                                    response.getResponseCode().equals("00")) {
                                getFiles();
                            } else {
                                showErrorDialog(file);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            dialog.dismiss();
                        }
                    });
            dialog.setCancelListener(new FileUploadDialog.CancelListener() {
                @Override
                public void cancel() {
                    showCancelDialog(mSubscription, dialog);
                }
            });
        } else {
            showMaterialDialog("提示", "上传的文件不存在，请重新选择文件");
        }
    }

    /*上传资源到临时文件*/
    private FileUploadResult commitFile(String url, final FileUploadDialog dialog, File file) throws Exception {
        Gson gson = new GsonBuilder().create();
        String resultStr = OkHttpClientManager.post(context, url, file, file.getName(), new OkHttpClientManager.ProgressListener() {
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
            String url = Constants.OUTRT_NET + "/m/lesson/cmts/" + lessonId + "/upload";
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
                uploadFile(file);
            }
        });
        dialog.show();
    }

    /*取消上传显示dialog*/
    private void showCancelDialog(final Disposable mSubscription, final FileUploadDialog dialog) {
        MaterialDialog mDialog = new MaterialDialog(context);
        mDialog.setTitle("提示");
        mDialog.setMessage("你确定取消本次上传吗？");
        mDialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        mDialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                dialog.dismiss();
                mSubscription.dispose();
            }
        });
        mDialog.setNegativeButton("关闭", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                dialog.dismiss();
                if (dialog != null && !dialog.isShowing()) {
                    dialog.show();
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
        String url = Constants.OUTRT_NET + "/m/lesson/cmts/" + lessonId;
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
                    event.obj = lessonEntity;
                    finish();
                } else {
                    toast(context, "删除失败");
                }
            }
        }, map));
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.CREATE_MAIN_REPLY) && event.obj != null && event.obj instanceof ReplyEntity) {
            adviseNum++;
            tv_adviseCount.setText("收到" + adviseNum + "条建议");
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
            setAdvise(adviseNum);
        }
    }
}
