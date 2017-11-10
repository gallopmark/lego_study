package com.haoyu.app.fragment;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.activity.AppMoreChildReplyActivity;
import com.haoyu.app.activity.AppMoreMainReplyActivity;
import com.haoyu.app.activity.AppMultiImageShowActivity;
import com.haoyu.app.activity.MFileInfoActivity;
import com.haoyu.app.activity.MFileInfosActivity;
import com.haoyu.app.adapter.AppDiscussionAdapter;
import com.haoyu.app.adapter.MFileInfoAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MFileInfoData;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.ReplyEntity;
import com.haoyu.app.entity.ReplyListResult;
import com.haoyu.app.entity.ReplyResult;
import com.haoyu.app.entity.TeachingLessonAttribute;
import com.haoyu.app.entity.TeachingLessonEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.HtmlTagHandler;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.GoodView;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.RoundRectProgressBar;
import com.haoyu.app.view.StickyScrollView;

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
 * 创建日期：2017/10/26 on 9:40
 * 描述:创课详情fragment
 * 作者:马飞奔 Administrator
 */
public class CmtsLessonFragment extends BaseFragment implements View.OnClickListener {
    @BindView(R.id.contentView)
    StickyScrollView contentView;
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

    @Override
    public int createView() {
        return R.layout.fragment_teaching_genclass;
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        lessonEntity = (TeachingLessonEntity) bundle.getSerializable("entity");
        lessonId = lessonEntity.getId();
        if (lessonEntity.getmDiscussionRelations() != null && lessonEntity.getmDiscussionRelations().size() > 0)
            relationId = lessonEntity.getmDiscussionRelations().get(0).getId();
        int remainDay = lessonEntity.getRemainDay();
        setProgress(remainDay);
        TeachingLessonEntity mLesson = (TeachingLessonEntity) bundle.getSerializable("mLesson");
        TeachingLessonAttribute mLessonAttribute = (TeachingLessonAttribute) bundle.getSerializable("attribute");
        if (mLesson != null)
            setLesson(mLesson);
        if (mLessonAttribute != null)
            setLessonAttribute(mLessonAttribute);
        FullyLinearLayoutManager llm_file = new FullyLinearLayoutManager(context);
        llm_file.setOrientation(FullyLinearLayoutManager.VERTICAL);
        rv_file.setLayoutManager(llm_file);
        FullyLinearLayoutManager llm_advise = new FullyLinearLayoutManager(context);
        llm_advise.setOrientation(FullyLinearLayoutManager.VERTICAL);
        rv_advise.setLayoutManager(llm_advise);
        adviseAdapter = new AppDiscussionAdapter(context, adviseList, getUserId());
        rv_advise.setAdapter(adviseAdapter);
    }

    public void setProgress(int remainDay) {
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
            layout_frame.setVisibility(View.VISIBLE);
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
            layout_activity.setVisibility(View.VISIBLE);
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
                    contentView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            contentView.smoothScrollBy(0, tag_introduce.getTop());
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
                    contentView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            contentView.smoothScrollBy(0, tag_frame.getTop());
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
                    contentView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            contentView.smoothScrollBy(0, tag_activity.getTop());
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
        if (text.trim().length() == 0)
            return true;
        return false;
    }

    private void addView(String title, String content, LinearLayout parent) {
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

    @Override
    public void initData() {
        getFiles();
        getAdvise();
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
                        errorAdvise.setVisibility(View.VISIBLE);
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
        tv_supportNum.setOnClickListener(this);
        tv_adviseNum.setOnClickListener(this);
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
        tv_more_reply.setOnClickListener(this);
        bottomView.setOnClickListener(this);
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
                onNetWorkError();
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
                    toast("您已点赞过");
                } else {
                    toast("点赞失败");
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
                onNetWorkError();
            }

            public void onResponse(ReplyResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    ReplyEntity entity = response.getResponseData();
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
                onNetWorkError();
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
                onNetWorkError();
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
                    toast("您已点赞过");
                } else {
                    toast("点赞失败");
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
                onNetWorkError();
            }

            @Override
            public void onResponse(ReplyResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    rv_advise.setVisibility(View.VISIBLE);
                    tv_emptyAdvise.setVisibility(View.GONE);
                    if (adviseList.size() < 5) {
                        ReplyEntity entity = response.getResponseData();
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
        } else if (event.action.equals("fileUpload")) {
            getFiles();
        }
    }

    private void setAdvise(int adviseNum) {
        tv_adviseCount.setText("收到\u1500" + adviseNum + "\u1500条建议");
    }
}
