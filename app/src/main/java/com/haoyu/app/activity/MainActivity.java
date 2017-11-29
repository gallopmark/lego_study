package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.haoyu.app.adapter.MyTrainAdapter;
import com.haoyu.app.adapter.MyTrainCommunityAdapter;
import com.haoyu.app.adapter.MyTrainCourseListAdapter;
import com.haoyu.app.adapter.MyTrainWorkShopListAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.base.LegoApplication;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.CaptureResult;
import com.haoyu.app.entity.CourseMobileEntity;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.MobileUserTrainInfoResult;
import com.haoyu.app.entity.MyTrainCommunityResult;
import com.haoyu.app.entity.MyTrainCourseResult;
import com.haoyu.app.entity.MyTrainMobileEntity;
import com.haoyu.app.entity.MyTrainResultEntity;
import com.haoyu.app.entity.MyTrainWorkShopResult;
import com.haoyu.app.entity.TimePeriod;
import com.haoyu.app.entity.UserInfoResult;
import com.haoyu.app.entity.VersionEntity;
import com.haoyu.app.entity.WorkShopMobileEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.service.VersionUpdateService;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

/**
 * 创建日期：2016/12/27 on 19:14
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    private MainActivity context = this;
    @BindView(R.id.toggle)
    ImageView toggle;
    @BindView(R.id.iv_scan)
    View iv_scan;  //扫一扫
    @BindView(R.id.iv_msg)
    ImageView iv_msg;
    @BindView(R.id.empty_train)
    View empty_train;
    @BindView(R.id.commuity_learn)
    View commuity_learn;   //空培训  进入社区交流教学经验
    @BindView(R.id.rl_myTrain)
    View rl_myTrain;
    @BindView(R.id.tv_myTrain)
    TextView tv_myTrain;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.svPersonalInfo)
    NestedScrollView svPersonalInfo;
    @BindView(R.id.info_content)
    LinearLayout info_content;
    @BindView(R.id.tv_courseResult)
    TextView tv_courseResult;
    @BindView(R.id.tv_workShopResult)
    TextView tv_workShopResult;
    @BindView(R.id.tv_communityResult)
    TextView tv_communityResult;
    @BindView(R.id.ll_course)
    LinearLayout ll_course;
    @BindView(R.id.ll_workshop)
    LinearLayout ll_workshop;
    @BindView(R.id.ll_community)
    LinearLayout ll_community;
    @BindView(R.id.mCourseLayout)
    TextView mCourseLayout;
    @BindView(R.id.mWorkShopLayout)
    TextView mWorkShopLayout;
    @BindView(R.id.empty_course)
    View empty_course;      //未选课
    @BindView(R.id.myCourseListView)
    RecyclerView myCourseListView;   //我的课程列表
    @BindView(R.id.empty_workshop)
    View empty_workshop;
    @BindView(R.id.ll_CommunityLayout)
    LinearLayout ll_CommunityLayout;
    @BindView(R.id.mWorkShopLV)
    RecyclerView mWorkShopLV;        //我的工作坊列表
    @BindView(R.id.mCommuityLV)
    RecyclerView mCommuityLV;        //研修社区列表
    private List<MyTrainMobileEntity> myTrains = new ArrayList<>();
    private List<MyTrainCourseResult> courses = new ArrayList<>();
    private MyTrainCourseListAdapter courseAdapter;
    private List<MyTrainWorkShopResult> workShops = new ArrayList<>();
    private MyTrainWorkShopListAdapter workShopAdapter;
    private List<MyTrainCommunityResult> communitys = new ArrayList<>();
    private MyTrainCommunityAdapter communityAdapter;
    private SlidingMenu menu;
    private ImageView iv_userIco;   //侧滑菜单用户头像
    private TextView tv_userName;   //侧滑菜单用户名
    private TextView tv_deptName;   //侧滑菜单用户部门名称
    private ArrayMap<String, MobileUserTrainInfoResult> mInfoMap = new ArrayMap<>();  //将已经加载的数据添加到map集合，避免重复加载
    private final static int SCANNIN_GREQUEST_CODE = 1, REQUSET_USERINFO_CODE = 2;
    private boolean showCommuity;  //课程是否是自主选课，是否限制学时
    private String trainId;
    private TimePeriod trainingTime;
    private int errorType;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        menu.setShadowWidthRes(R.dimen.shadow_width);
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);

        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        View menuView = LayoutInflater.from(context).inflate(R.layout.app_homepage_menu, null);
        initMenuView(menuView);
        menu.setMenu(menuView);
        myCourseListView.setNestedScrollingEnabled(false);
        mWorkShopLV.setNestedScrollingEnabled(false);
        mCommuityLV.setNestedScrollingEnabled(false);
        FullyLinearLayoutManager courseLayoutManager = new FullyLinearLayoutManager(context);
        courseLayoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        myCourseListView.setLayoutManager(courseLayoutManager);
        myCourseListView.setNestedScrollingEnabled(false);
        FullyLinearLayoutManager workShopLayoutManager = new FullyLinearLayoutManager(context);
        workShopLayoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        mWorkShopLV.setLayoutManager(workShopLayoutManager);
        FullyLinearLayoutManager communityLayoutManager = new FullyLinearLayoutManager(context);
        communityLayoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        mCommuityLV.setLayoutManager(communityLayoutManager);
        courseAdapter = new MyTrainCourseListAdapter(context, courses);
        myCourseListView.setAdapter(courseAdapter);
        workShopAdapter = new MyTrainWorkShopListAdapter(context, workShops);
        mWorkShopLV.setAdapter(workShopAdapter);
        communityAdapter = new MyTrainCommunityAdapter(context, communitys);
        mCommuityLV.setAdapter(communityAdapter);
        registRxBus();  //订阅事件
        getVersion();
    }

    private void initMenuView(View menuView) {
        View ll_userInfo = getView(menuView, R.id.ll_userInfo);
        ll_userInfo.setOnClickListener(context);
        iv_userIco = getView(menuView, R.id.iv_userIco);
        GlideImgManager.loadCircleImage(context, getAvatar(), R.drawable.user_default,
                R.drawable.user_default, iv_userIco);
        tv_userName = getView(menuView, R.id.tv_userName);
        tv_deptName = getView(menuView, R.id.tv_deptName);
        if (TextUtils.isEmpty(getRealName()))
            tv_userName.setText("请填写用户名");
        else
            tv_userName.setText(getRealName());
        if (TextUtils.isEmpty(getDeptName()))
            tv_deptName.setText("请选择单位");
        else
            tv_deptName.setText(getDeptName());
        TextView tv_learn = getView(menuView, R.id.tv_learn);  //学习
        tv_learn.setOnClickListener(context);
        TextView tv_teaching = getView(menuView, R.id.tv_teaching); //教研
        tv_teaching.setOnClickListener(context);
        TextView tv_peer = getView(menuView, R.id.tv_peer);  //同行
        tv_peer.setOnClickListener(context);
        TextView tv_message = getView(menuView, R.id.tv_message);  //消息
        tv_message.setOnClickListener(context);
        TextView tv_course = getView(menuView, R.id.tv_course);  //选课中心
        tv_course.setOnClickListener(context);
        TextView tv_workshop = getView(menuView, R.id.tv_workshop);   //工作坊群
        tv_workshop.setOnClickListener(context);
        TextView tv_consulting = getView(menuView, R.id.tv_consulting);  //教务咨询
        tv_consulting.setOnClickListener(context);
        TextView tv_settings = getView(menuView, R.id.tv_settings);  //设置
        tv_settings.setOnClickListener(context);
        getUserInfo();
    }

    private void getUserInfo() {
        String url = Constants.OUTRT_NET + "/m/user/" + getUserId();
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<UserInfoResult>() {

            @Override
            public void onError(Request request, Exception e) {
            }

            @Override
            public void onResponse(UserInfoResult response) {
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                }
            }
        }));
    }

    private void updateUI(MobileUser user) {
        GlideImgManager.loadCircleImage(context.getApplicationContext(), user.getAvatar(), R.drawable.user_default,
                R.drawable.user_default, iv_userIco);
        if (TextUtils.isEmpty(user.getRealName()))
            tv_userName.setText("请填写用户名");
        else
            tv_userName.setText(user.getRealName());
        if (user.getmDepartment() != null && user.getmDepartment().getDeptName() != null)
            tv_deptName.setText(user.getmDepartment().getDeptName());
        else
            tv_deptName.setText("请选择单位");
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/uc/listMyTrain";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<List<MyTrainMobileEntity>>>() {

            @Override
            public void onBefore(Request request) {
                loadFailView.setVisibility(View.GONE);
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                errorType = 1;
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(BaseResponseResult<List<MyTrainMobileEntity>> response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().size() > 0) {
                    updateTrainListUI(response.getResponseData());
                    trainId = response.getResponseData().get(0).getId();
                    trainingTime = response.getResponseData().get(0).getmTrainingTime();
                    getUserTrainInfo(trainId);
                } else {
                    empty_train.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    /*更新我的培训列表*/
    private void updateTrainListUI(List<MyTrainMobileEntity> responseData) {
        svPersonalInfo.setVisibility(View.VISIBLE);
        myTrains.addAll(responseData);
        tv_myTrain.setText(myTrains.get(0).getName());
    }

    /*获取个人培训信息*/
    private void getUserTrainInfo(final String trainId) {
        if (mInfoMap.get(trainId) == null) {
            String url = Constants.OUTRT_NET + "/m/uc/getUserTrainInfo?trainId=" + trainId;
            addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<MobileUserTrainInfoResult>() {
                @Override
                public void onBefore(Request request) {
                    loadingView.setVisibility(View.VISIBLE);
                    loadFailView.setVisibility(View.GONE);
                    info_content.setVisibility(View.GONE);
                }

                @Override
                public void onError(Request request, Exception e) {
                    errorType = 2;
                    loadingView.setVisibility(View.GONE);
                    loadFailView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onResponse(MobileUserTrainInfoResult response) {
                    loadingView.setVisibility(View.GONE);
                    mInfoMap.put(trainId, response);
                    updateUI(response);
                }
            }));
        } else {
            MobileUserTrainInfoResult response = mInfoMap.get(trainId);
            updateUI(response);
        }
    }

    private void updateUI(MobileUserTrainInfoResult response) {
        info_content.setVisibility(View.VISIBLE);
        if (response != null && response.getResponseData() != null && response.getResponseData().getTrainResult() != null) {
            updateTrainInfoUI(response.getResponseData().getTrainResult());
        }
        if (response != null && response.getResponseData() != null && response.getResponseData().getmCourseRegisters() != null) {
            updateCourseListUI(response.getResponseData().getmCourseRegisters());
        }
        if (response != null && response.getResponseData() != null && response.getResponseData().getmWorkshopUsers() != null) {
            updateWorkShopListUI(response.getResponseData().getmWorkshopUsers());
        }
        if (response != null && response.getResponseData() != null && response.getResponseData().getmCommunityResult() != null) {
            updateCommunityListUI(response.getResponseData().getmCommunityResult());
        }
    }

    /*更新培训情况信息*/
    private void updateTrainInfoUI(MyTrainResultEntity trainResult) {
        if (trainResult.getTrainType() != null && !trainResult.getTrainType().contains("course")) {
            tv_courseResult.setText("此项无需考核");
        } else {
            tv_courseResult.setText("已选" + trainResult.getRegisterCourseNum() + "合格" + trainResult.getPassCourseNum() + "门");
        }
        if (trainResult.getTrainType() != null && !trainResult.getTrainType().contains("workshop")) {
            tv_workShopResult.setText("此项无需考核");
        } else {
            tv_workShopResult.setText("获得" + trainResult.getGetWstsPoint() + "/" + trainResult.getWstsPoint() + "积分");
        }
        if (trainResult.getTrainType() != null && !trainResult.getTrainType().contains("community")) {
            showCommuity = false;
            tv_communityResult.setText("此项无需考核");
        } else {
            showCommuity = true;
            tv_communityResult.setText("获得" + trainResult.getGetCmtsPoint() + "/" + trainResult.getCmtsPoint() + "积分");
        }
    }

    /*更新课程列表*/
    private void updateCourseListUI(List<MyTrainCourseResult> myTrainCourseResults) {
        courses.clear();
        for (MyTrainCourseResult result : myTrainCourseResults) {
            if (result.getState() != null && result.getState().equals("pass")) {
                courses.add(result);
            }
        }
        if (courses.size() > 0) {
            courseAdapter.notifyDataSetChanged();
            myCourseListView.setVisibility(View.VISIBLE);
            myCourseListView.setFocusable(false);
            empty_course.setVisibility(View.GONE);
        } else {
            myCourseListView.setVisibility(View.GONE);
            empty_course.setVisibility(View.VISIBLE);
        }
    }

    /*更新工作坊列表*/
    private void updateWorkShopListUI(List<MyTrainWorkShopResult> myTrainWorkShopResults) {
        if (myTrainWorkShopResults.size() > 0) {
            workShops.clear();
            workShops.addAll(myTrainWorkShopResults);
            workShopAdapter.notifyDataSetChanged();
            mWorkShopLV.setVisibility(View.VISIBLE);
            mWorkShopLV.setFocusable(false);
            empty_workshop.setVisibility(View.GONE);
        } else {
            mWorkShopLV.setVisibility(View.GONE);
            empty_workshop.setVisibility(View.VISIBLE);
        }
    }

    private void updateCommunityListUI(MyTrainCommunityResult myTrainCommunityResult) {
        if (showCommuity) {
            ll_CommunityLayout.setVisibility(View.VISIBLE);
            communitys.clear();
            communitys.add(myTrainCommunityResult);
            mCommuityLV.setFocusable(false);
            communityAdapter.notifyDataSetChanged();
        } else {
            ll_CommunityLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void setListener() {
        iv_msg.setOnClickListener(context);
        toggle.setOnClickListener(context);
        commuity_learn.setOnClickListener(context);
        iv_scan.setOnClickListener(context);
        rl_myTrain.setOnClickListener(context);
        ll_course.setOnClickListener(context);
        ll_workshop.setOnClickListener(context);
        ll_community.setOnClickListener(context);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                if (errorType == 1)
                    initData();
                else
                    getUserTrainInfo(trainId);
            }
        });
        courseAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                String state = courses.get(position).getState();
                if (state != null && state.equals("pass") && courses.get(position).getmCourse() != null) {
                    CourseMobileEntity entity = courses.get(position).getmCourse();
                    if (entity.getmTimePeriod() != null && entity.getmTimePeriod().getState() != null && entity.getmTimePeriod().getState().equals("未开始")) {
                        showDialog("未开始");
                        return;
                    }
                    String courseId = entity.getId();
                    String courseTitle = entity.getTitle();
                    Intent intent = new Intent(context, CourseTabActivity.class);
                    if (trainingTime != null && trainingTime.getState() != null && trainingTime.getState().equals("进行中"))
                        intent.putExtra("training", true);
                    else if (trainingTime != null && trainingTime.getMinutes() > 0)
                        intent.putExtra("training", true);
                    else
                        intent.putExtra("training", false);
                    intent.putExtra("courseId", courseId);
                    intent.putExtra("courseTitle", courseTitle);
                    startActivity(intent);
                } else {
                    showDialog(state);
                }
            }
        });

        workShopAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                WorkShopMobileEntity entity = workShops.get(position).getmWorkshop();
                int point = workShops.get(position).getPoint();
                if (entity != null) {
                    String workshopId = entity.getId();
                    String workshopTitle = entity.getTitle();
                    Intent intent = new Intent(context, WorkshopHomeActivity.class);
                    if (trainingTime != null && trainingTime.getState() != null && trainingTime.getState().equals("进行中"))
                        intent.putExtra("training", true);
                    else if (trainingTime != null && trainingTime.getMinutes() > 0)
                        intent.putExtra("training", true);
                    else
                        intent.putExtra("training", false);
                    intent.putExtra("workshopId", workshopId);
                    intent.putExtra("point", point);
                    intent.putExtra("workshopTitle", workshopTitle);
                    startActivity(intent);
                }
            }
        });
        communityAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                startActivity(new Intent(context, CmtsMainActivity.class));
            }
        });
    }

    private void showDialog(String state) {
        MaterialDialog tipDialog = new MaterialDialog(context);
        String message;
        if (state != null && state.equals("未开始"))
            message = "课程尚未开放";
        else if (state != null && state.equals("submit"))
            message = "您的选课正在审核中";
        else if (state != null && state.equals("nopass"))
            message = "您的选课审核不通过";
        else
            message = "课程尚未开放";
        tipDialog.setTitle("温馨提示");
        tipDialog.setMessage(message);
        tipDialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        tipDialog.setPositiveButton("我知道了", null);
        tipDialog.show();
    }

    private long mExitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && menu.isMenuShowing()) {
            menu.toggle(true);
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && !menu.isMenuShowing()) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                toast(context, "再按一次退出" + getResources().getString(R.string.app_name));
                mExitTime = System.currentTimeMillis();
            } else {
                LegoApplication.exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.toggle:
                menu.toggle(true);
                break;
            case R.id.iv_scan:
                Intent cameraIntent = new Intent(context, AppCaptureActivity.class);
                startActivityForResult(cameraIntent, SCANNIN_GREQUEST_CODE);
                break;
            case R.id.iv_msg:
                StringBuilder sb = new StringBuilder();
                if (myTrains.size() > 0) {
                    for (int i = 0; i < myTrains.size(); i++) {
                        sb.append(myTrains.get(i).getId());
                        sb.append(",");
                    }
                    sb.deleteCharAt(sb.lastIndexOf(","));
                }
                intent.setClass(context, AnnouncementActivity.class);
                intent.putExtra("relationId", sb.toString());
                startActivity(intent);
                break;
            case R.id.commuity_learn:
                startActivity(new Intent(context, CmtsMainActivity.class));
                break;
            case R.id.rl_myTrain:
                setPopupView(tv_myTrain, myTrains);
                break;
            case R.id.ll_course:
                scrollToPosition(mCourseLayout);
                break;
            case R.id.ll_workshop:
                scrollToPosition(mWorkShopLayout);
                break;
            case R.id.ll_community:
                scrollToPosition(ll_CommunityLayout);
                break;
            case R.id.ll_userInfo:  //侧滑菜单个人信息
                intent.setClass(context, AppUserInfoActivity.class);
                startActivityForResult(intent, REQUSET_USERINFO_CODE);
                break;
            case R.id.tv_learn:  //侧滑菜单学习
                menu.toggle(true);
                break;
            case R.id.tv_teaching:  //侧滑菜单教研
                startActivity(new Intent(context, CmtsMainActivity.class));
                break;
            case R.id.tv_workshop:  //侧滑菜单工作坊群
                intent.setClass(context, WorkshopGroupActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_peer:   //侧滑菜单同行
                startActivity(new Intent(context, PeerActivity.class));
                break;
            case R.id.tv_message:  //侧滑菜单消息
                intent.setClass(context, MessageActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_consulting:  //侧滑菜单教务咨询
                startActivity(new Intent(context, EducationConsultActivity.class));
                break;
            case R.id.tv_settings:  //侧滑菜单设置
                intent.setClass(context, SettingActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 滑动到指定位置
     */
    private void scrollToPosition(final View view) {
        svPersonalInfo.smoothScrollTo(0, view.getBottom());
    }

    private int selectItem = 0;

    private void setPopupView(final TextView tv, final List<MyTrainMobileEntity> list) {
        Drawable shouqi = ContextCompat.getDrawable(context, R.drawable.course_dictionary_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(), shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context, R.drawable.course_dictionary_xiala);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(), zhankai.getMinimumHeight());
        tv.setCompoundDrawables(null, null, shouqi, null);
        View view = getLayoutInflater().inflate(R.layout.popupwindow_listview, null);
        final PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ListView lv = view.findViewById(R.id.listView);
        final MyTrainAdapter adapter = new MyTrainAdapter(context, list, selectItem);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectItem = position;
                trainId = list.get(selectItem).getId();
                trainingTime = list.get(selectItem).getmTrainingTime();
                adapter.setSelectItem(selectItem);
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                tv.setText(list.get(selectItem).getName());
                getUserTrainInfo(trainId);
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv.setCompoundDrawables(null, null, zhankai, null);
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAsDropDown(tv);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
             /* 处理二维码扫描结果*/
                if (data != null && data.getExtras() != null) {
                    Bundle bundle = data.getExtras();
                    if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                        String result = bundle.getString(CodeUtils.RESULT_STRING);
                        parseCaptureResult(result);
                    } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                        toast(context, "解析二维码失败");
                    }
                }
                break;
            case REQUSET_USERINFO_CODE:
                if (data != null) {
                    String avatar = data.getStringExtra("avatar");
                    GlideImgManager.loadCircleImage(context.getApplicationContext(), avatar, R.drawable.user_default, R.drawable.user_default, iv_userIco);
                }
                break;
        }
    }

    private void parseCaptureResult(String result) {
        if (result.contains("qtId") && result.contains("service")) {   //扫一扫登录
            try {
                Gson gson = new Gson();
                CaptureResult mCaptureResult = gson.fromJson(result, CaptureResult.class);
                String qtId = mCaptureResult.getQtId();
                String service = mCaptureResult.getService();
                String url = Constants.LOGIN_URL;
                login(url, qtId, service);
            } catch (Exception e) {
                toast(context, "解析二维码失败");
            }
        } else if ((result.startsWith("http") || result.startsWith("https")) && result.contains(Constants.REFERER)) {
            signedOn(result);
        } else {
            showMaterialDialog("提示", "非本应用规定的二维码");
        }
    }

    private void login(final String url, final String qtId, final String service) {
        showLoadingDialog("登录验证");
        Flowable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return OkHttpClientManager.getInstance().scanLogin(context, url, qtId, service);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isSuccessful) throws Exception {
                        hideLoadingDialog();
                        if (isSuccessful) {
                            toast(context, "验证成功");
                        } else {
                            toast(context, "验证失败");
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        hideLoadingDialog();
                        toast(context, "验证失败");
                    }
                });
    }

    private void signedOn(String url) {
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
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
                    toast(context, "签到成功");
                } else {
                    if (response != null && response.getResponseMsg() != null)
                        toast(context, response.getResponseMsg());
                    else
                        toast(context, "签到失败");
                }
            }
        }));
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.CHANGE_USER_ICO) && event.obj != null && event.obj instanceof String) {
            String avatar = (String) event.obj;
            GlideImgManager.loadCircleImage(context, avatar, R.drawable.user_default, R.drawable.user_default, iv_userIco);
        } else if (event.action.equals(Action.CHANGE_USER_NAME) && event.obj != null && event.obj instanceof String) {
            String realName = (String) event.obj;
            tv_userName.setText(realName);
        } else if (event.action.equals(Action.CHANGE_DEPT_NAME) && event.obj != null && event.obj instanceof String) {
            String deptName = (String) event.obj;
            tv_deptName.setText(deptName);
        } else if (event.action.equals(Action.SUBMIT_CHOOSE_COURSE)) {
            mInfoMap.remove(trainId);
            getUserTrainInfo(trainId);
        } else if (event.action.equals(Action.CREATE_WORKSHOP)) {
            mInfoMap.remove(trainId);
            getUserTrainInfo(trainId);
        }
    }

    private void getVersion() {
        addSubscription(OkHttpClientManager.getAsyn(context, Constants.updateUrl, new OkHttpClientManager.ResultCallback<VersionEntity>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(VersionEntity entity) {
                if (entity.getVersionCode() > Common.getVersionCode(context)) {
                    updateTips(entity);
                }
            }
        }));
    }

    private void updateTips(final VersionEntity entity) {
        final MaterialDialog dialog = new MaterialDialog(context);
        dialog.setMessage(entity.getUpdateLog());
        dialog.setTitle("发现新版本");
        dialog.setNegativeButton("稍后下载", null);
        dialog.setPositiveButton("立即下载", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                startService(entity);
            }
        });
        dialog.show();
    }

    private void startService(VersionEntity entity) {
        Intent intent = new Intent(context, VersionUpdateService.class);
        intent.putExtra("url", entity.getDownurl());
        intent.putExtra("versionName", entity.getVersionName());
        startService(intent);
        if (!Common.isNotificationEnabled(context)) {
            openTips();
        }
    }

    private void openTips() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("通知已关闭，是否允许应用推送通知？");
        dialog.setPositiveButton("开启", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                Common.openSettings(context);
            }
        });
        dialog.setNegativeButton("取消", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                toast(context, "已进入后台更新");
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(context, VersionUpdateService.class));
    }
}

