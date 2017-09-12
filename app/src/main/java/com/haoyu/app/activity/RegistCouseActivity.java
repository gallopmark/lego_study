package com.haoyu.app.activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.haoyu.app.adapter.CourseTopicAdapter;
import com.haoyu.app.adapter.DictEntryAdapter;
import com.haoyu.app.adapter.RegistCourseAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.CourseMobileEntity;
import com.haoyu.app.entity.CourseTopic;
import com.haoyu.app.entity.DictEntryMobileEntity;
import com.haoyu.app.entity.DictEntryResult;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.RegistCourseListResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/1/6 on 16:08
 * 描述:选课中心
 * 作者:马飞奔 Administrator
 */
public class RegistCouseActivity extends BaseActivity implements View.OnClickListener, XRecyclerView.LoadingListener {
    private RegistCouseActivity context = this;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_selected)
    TextView tv_selected;
    @BindView(R.id.tv_stage)
    TextView tv_stage;
    @BindView(R.id.tv_subject)
    TextView tv_subject;
    @BindView(R.id.ll_category)
    LinearLayout ll_category;
    @BindView(R.id.tv_category)
    TextView tv_category;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.empty_list)
    TextView empty_list;
    private List<CourseMobileEntity> coursesList = new ArrayList<>();  //我的课程集合
    private RegistCourseAdapter mAdapter;
    private int page = 1;
    private boolean isRefresh, isLoadMore, initData = true;
    private List<DictEntryMobileEntity> subjectList; // 学科集合
    private List<DictEntryMobileEntity> stageList; // 学段集合
    private List<CourseTopic> courseTopicList;   //课程主题集合
    private String subjectType, stageType, courseTopicId;
    private String trainId;
    private boolean hasTopic, isNoLimit;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_regist_course;
    }

    @Override
    public void initView() {
        trainId = getIntent().getStringExtra("trainId");
        hasTopic = getIntent().getBooleanExtra("hasTopic", false);
        isNoLimit = getIntent().getBooleanExtra("isNoLimit", false);
        if (hasTopic) {
            ll_category.setVisibility(View.VISIBLE);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        xRecyclerView.setArrowImageView(R.drawable.refresh_arrow);
        mAdapter = new RegistCourseAdapter(context, coursesList);
        xRecyclerView.setAdapter(mAdapter);
        xRecyclerView.setLoadingListener(context);
        registRxBus();
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/course_center?page=" + page + "&courseRelation.relation.id=" + trainId;
        if (stageType != null) {
            url += "&stage=" + stageType;
        }
        if (subjectType != null) {
            url += "&subject=" + subjectType;
        }
        if (courseTopicId != null) {
            url += "&courseTopic.id=" + courseTopicId;
        }
        Log.e("url", url);
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<RegistCourseListResult>() {
            @Override
            public void onBefore(Request request) {
                if (initData) {
                    loadingView.setVisibility(View.VISIBLE);
                    xRecyclerView.setVisibility(View.GONE);
                }
                empty_list.setVisibility(View.GONE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                xRecyclerView.setVisibility(View.GONE);
                empty_list.setVisibility(View.GONE);
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    page -= 1;
                    xRecyclerView.loadMoreComplete(false);
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onResponse(RegistCourseListResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null
                        && response.getResponseData().getmCourses() != null
                        && response.getResponseData().getmCourses().size() > 0) {
                    updateUI(response.getResponseData().getmCourses(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    } else {
                        xRecyclerView.setVisibility(View.GONE);
                        empty_list.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    private void updateUI(List<CourseMobileEntity> mList, Paginator paginator) {
        xRecyclerView.setVisibility(View.VISIBLE);
        empty_list.setVisibility(View.GONE);
        if (initData) {
            coursesList.clear();
        } else if (isRefresh) {
            coursesList.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        coursesList.addAll(mList);
        mAdapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void setListener() {
        iv_back.setOnClickListener(context);
        tv_selected.setOnClickListener(context);
        tv_subject.setOnClickListener(context);
        tv_stage.setOnClickListener(context);
        ll_category.setOnClickListener(context);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                try {
                    String courseId = coursesList.get(position - 1).getId();
                    Intent intent = new Intent(context, CourseDetailActivity.class);
                    intent.putExtra("courseId", courseId);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        isRefresh = true;
        isLoadMore = false;
        initData = false;
        page = 1;
        initData();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        initData = false;
        page += 1;
        initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_selected:
                Intent intent = new Intent(context, CourseRegistStateActivity.class);
                intent.putExtra("trainId", trainId);
                intent.putExtra("isNoLimit", isNoLimit);
                startActivity(intent);
                break;
            case R.id.tv_subject:
                if (subjectList == null) {
                    initSubject();
                } else {
                    showSubjectWindow();
                }
                break;
            case R.id.tv_stage:
                if (stageList == null) {
                    initStage();
                } else {
                    showStageWindow();
                }
                break;
            case R.id.ll_category:
                if (courseTopicList == null) {
                    initCourseTopic();
                } else {
                    showTopicWindow();
                }
                break;
        }
    }

    private void initSubject() {
        String url = Constants.OUTRT_NET + "/m/textBook?textBookTypeCode=SUBJECT";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<DictEntryResult>() {

            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(DictEntryResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    subjectList = new ArrayList<>();
                    DictEntryMobileEntity entity = new DictEntryMobileEntity();
                    entity.setTextBookName("所有学科");
                    subjectList.add(entity);
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
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(DictEntryResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    stageList = new ArrayList<>();
                    DictEntryMobileEntity entity = new DictEntryMobileEntity();
                    entity.setTextBookName("所有学段");
                    stageList.add(entity);
                    stageList.addAll(response.getResponseData());
                    showStageWindow();
                }
            }
        }));
    }

    /*访问课程主题*/
    private void initCourseTopic() {
        String url = Constants.OUTRT_NET + "/m/course_topic";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<List<CourseTopic>>>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult<List<CourseTopic>> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    courseTopicList = new ArrayList<>();
                    CourseTopic courseTopic = new CourseTopic();
                    courseTopic.setTitle("所有类别");
                    courseTopicList.add(courseTopic);
                    courseTopicList.addAll(response.getResponseData());
                    showTopicWindow();
                }
            }
        }));
    }

    private int stageSelect = 0;

    private void showStageWindow() {
        Drawable zhankai = ContextCompat.getDrawable(context, R.drawable.course_dictionary_shouqi);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(), zhankai.getMinimumHeight());
        tv_stage.setCompoundDrawables(null, null, zhankai, null);
        View view = getLayoutInflater().inflate(R.layout.popupwindow_listview,
                null);
        ListView lv = view.findViewById(R.id.listView);
        final DictEntryAdapter adapter = new DictEntryAdapter(context, stageList, stageSelect);
        lv.setAdapter(adapter);
        final PopupWindow popupWindow = new PopupWindow(view, tv_stage.getWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        lv.setSelection(stageSelect);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                stageSelect = position;
                adapter.setSelectItem(stageSelect);
                tv_stage.setText(stageList.get(position).getTextBookName());
                popupWindow.dismiss();
                if (position > 0) {
                    stageType = stageList.get(position).getTextBookValue();
                } else {
                    stageType = null;
                }
                isRefresh = false;
                isLoadMore = false;
                initData = true;
                page = 1;
                initData();
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Drawable shouqi = ContextCompat.getDrawable(context, R.drawable.course_dictionary_xiala);
                shouqi.setBounds(0, 0, shouqi.getMinimumWidth(), shouqi.getMinimumHeight());
                tv_stage.setCompoundDrawables(null, null, shouqi, null);
            }
        });
        popupWindow.showAsDropDown(tv_stage);
    }

    private int subjectSelect = 0;

    private void showSubjectWindow() {
        Drawable zhankai = ContextCompat.getDrawable(context, R.drawable.course_dictionary_shouqi);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(), zhankai.getMinimumHeight());
        tv_subject.setCompoundDrawables(null, null, zhankai, null);
        View view = getLayoutInflater().inflate(R.layout.popupwindow_listview,
                null);
        ListView lv = view.findViewById(R.id.listView);
        final DictEntryAdapter adapter = new DictEntryAdapter(context, subjectList, subjectSelect);
        lv.setAdapter(adapter);
        final PopupWindow popupWindow = new PopupWindow(view, tv_subject.getWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        lv.setSelection(subjectSelect);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                subjectSelect = position;
                adapter.setSelectItem(subjectSelect);
                tv_subject.setText(subjectList.get(position).getTextBookName());
                popupWindow.dismiss();
                if (position > 0) {
                    subjectType = subjectList.get(position).getTextBookValue();
                } else {
                    subjectType = null;
                }
                isRefresh = false;
                isLoadMore = false;
                initData = true;
                page = 1;
                initData();
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Drawable shouqi = ContextCompat.getDrawable(context, R.drawable.course_dictionary_xiala);
                shouqi.setBounds(0, 0, shouqi.getMinimumWidth(), shouqi.getMinimumHeight());
                tv_subject.setCompoundDrawables(null, null, shouqi, null);
            }
        });
        popupWindow.showAsDropDown(tv_subject);
    }

    private int topicSelect = 0;

    private void showTopicWindow() {
        Drawable zhankai = ContextCompat.getDrawable(context, R.drawable.course_dictionary_shouqi);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(), zhankai.getMinimumHeight());
        tv_category.setCompoundDrawables(null, null, zhankai, null);
        View view = getLayoutInflater().inflate(R.layout.popupwindow_listview,
                null);
        ListView lv = view.findViewById(R.id.listView);
        final CourseTopicAdapter adapter = new CourseTopicAdapter(context, courseTopicList, topicSelect);
        lv.setAdapter(adapter);
        final PopupWindow popupWindow = new PopupWindow(view, ll_category.getWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        lv.setSelection(topicSelect);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                topicSelect = position;
                adapter.setSelectItem(topicSelect);
                tv_category.setText(courseTopicList.get(position).getTitle());
                popupWindow.dismiss();
                if (position > 0) {
                    courseTopicId = courseTopicList.get(position).getId();
                } else {
                    courseTopicId = null;
                }
                isRefresh = false;
                isLoadMore = false;
                initData = true;
                initData();
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Drawable shouqi = ContextCompat.getDrawable(context, R.drawable.course_dictionary_xiala);
                shouqi.setBounds(0, 0, shouqi.getMinimumWidth(), shouqi.getMinimumHeight());
                tv_category.setCompoundDrawables(null, null, shouqi, null);
            }
        });
        popupWindow.showAsDropDown(ll_category);
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.SUBMIT_CHOOSE_COURSE)) {
            finish();
        }
    }
}
