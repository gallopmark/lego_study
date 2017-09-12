package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.CourseMobileEntity;
import com.haoyu.app.entity.CourseSingleResult;
import com.haoyu.app.entity.MCourseRegister;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.CustomViewPager;
import com.haoyu.app.view.ExpandableTextView;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

public class CourseDetailActivity extends BaseActivity {
    private CourseDetailActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.contentView)
    RelativeLayout contentView;
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;
    @BindView(R.id.course_image)
    ImageView course_image;  //课程封面图片
    @BindView(R.id.course_type)
    TextView course_type; //课程类型
    @BindView(R.id.course_period)
    TextView course_period; // 学时
    @BindView(R.id.course_enroll)
    TextView course_enroll; // 报读数
    @BindView(R.id.course_title)
    TextView course_title; // 课程标题
    @BindView(R.id.tv_summary)
    ExpandableTextView tv_summary; // 课程简介
    @BindView(R.id.empty_summary)
    TextView empty_summary;
    @BindView(R.id.emptyTeacher)
    View emptyTeacher;
    @BindView(R.id.teacherIndicator)
    LinearLayout teacherIndicator;  //授课教师分页指示
    private ImageView[] indicatorViews;
    @BindView(R.id.teacherPager)
    CustomViewPager teacherPager;  // 授课教师分页
    @BindView(R.id.bt_signUp)
    Button bt_signUp;  //报读课程
    private String courseId;  //课程Id
    private String registId;  //已报读课程Id
    private boolean isEnroll;  //是否已经报读

    @Override
    public int setLayoutResID() {
        return R.layout.activity_course_details;
    }

    @Override
    public void initView() {
        courseId = getIntent().getStringExtra("courseId");
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.getScreenHeight(context) / 7 * 2);
        course_image.setLayoutParams(ivParams);
    }

    /**
     * 通过课程Id获取课程相关信息
     */
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/course_center/detail/" + courseId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<CourseSingleResult>() {

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
            public void onResponse(CourseSingleResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    contentView.setVisibility(View.VISIBLE);
                    updateUI(response.getResponseData());
                }
            }
        }));
    }

    private void updateUI(CourseSingleResult.CourseSingleResponseData responseData) {
        if (responseData.getmCourse() != null) {
            CourseMobileEntity entity = responseData.getmCourse();
            if (entity.getIntro() != null && entity.getIntro().length() > 0) {
                tv_summary.setVisibility(View.VISIBLE);
                Spanned spanned = Html.fromHtml(entity.getIntro());
                tv_summary.setText(spanned);
            } else {
                empty_summary.setVisibility(View.VISIBLE);
            }
            course_title.setText(entity.getTitle());
            course_type.setText(entity.getType());
            course_period.setText(String.valueOf(entity.getStudyHours()) + "学时");
            course_enroll.setText(entity.getRegisterNum() + "人报读");
            if (entity.getImage() != null) {
                Glide.with(context).load(entity.getImage()).placeholder(R.drawable.app_default)
                        .error(R.drawable.app_default)
                        .dontAnimate().into(course_image);
            } else {
                course_image.setImageResource(R.drawable.app_default);
            }
        }
        if (responseData.getmCourseRegister() != null) {
            registId = responseData.getmCourseRegister().getId();
            String state = responseData.getmCourseRegister().getState();
            if (state != null && state.equals("noSubmit")) {
                isEnroll = false;
                bt_signUp.setText("报读课程");
                bt_signUp.setEnabled(true);
            } else if (state != null && state.equals("submit")) {
                isEnroll = true;
                bt_signUp.setText("等待审核");
                bt_signUp.setEnabled(false);
            } else if (state != null && state.equals("pass")) {
                isEnroll = true;
                bt_signUp.setText("取消报读");
                bt_signUp.setEnabled(true);
            } else if (state != null && state.equals("nopass")) {
                isEnroll = true;
                bt_signUp.setText("取消报读");
                bt_signUp.setEnabled(true);
            } else if (state != null && state.equals("not_commit")) {
                isEnroll = true;
                bt_signUp.setText("取消报读");
                bt_signUp.setEnabled(true);
            } else {
                isEnroll = false;
                bt_signUp.setText("报读课程");
                bt_signUp.setEnabled(true);
            }
        }
        if (responseData.getTeachers() != null && responseData.getTeachers().size() > 0) {
            teacherPager.setVisibility(View.VISIBLE);
            updateUI(responseData.getTeachers());
        } else {
            emptyTeacher.setVisibility(View.VISIBLE);
        }
    }

    private void updateUI(List<MobileUser> teachers) {
        if (teachers.size() > 1) {
            indicatorViews = new ImageView[teachers.size()];
            for (int i = 0; i < teachers.size(); i++) {   //位置从0开始 页数从1开始
                indicatorViews[i] = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.leftMargin = (int) getResources().getDimension(R.dimen.margin_size_4);
                indicatorViews[i].setLayoutParams(params);
                indicatorViews[i].setImageResource(R.drawable.course_yuandian_default);
                teacherIndicator.addView(indicatorViews[i]);
            }
            indicatorViews[0].setImageResource(R.drawable.course_yuandian_press);
        }
        updateTeachPager(teachers);
    }

    private void updateTeachPager(List<MobileUser> teachers) {
        TeacherPagerAdapter mAdapter = new TeacherPagerAdapter(teachers);
        teacherPager.setAdapter(mAdapter);
        teacherPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (indicatorViews != null && indicatorViews.length > 0) {
                    for (int i = 0; i < indicatorViews.length; i++) {
                        if (i == position)
                            indicatorViews[i].setImageResource(R.drawable.course_yuandian_press);
                        else
                            indicatorViews[i].setImageResource(R.drawable.course_yuandian_default);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        bt_signUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEnroll) {   //如果已经报读,则取消报读
                    cancelEnroll();
                } else {  //否则发起报读课程请求
                    registEnroll();
                }
            }
        });
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        tv_summary.setOnExpandStateChangeListener(new ExpandableTextView.OnExpandStateChangeListener() {
            @Override
            public void onExpandStateChanged(TextView textView, boolean isExpanded) {
                if (!isExpanded) {
                    scrollView.smoothScrollTo(0, (int) textView.getY());
                }
            }
        });
    }

    private void cancelEnroll() {
        String userId = getUserId();
        String url = Constants.OUTRT_NET + "/unique_uid_" + userId + "/m/course_register/delete/" + registId;
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
                toast(context, "请求失败,请稍后再试");
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    isEnroll = false;
                    bt_signUp.setText("报读课程");
                    bt_signUp.setEnabled(true);
                } else {
                    if (response != null && response.getResponseMsg() != null) {
                        toast(context, response.getResponseMsg());
                    }
                }
            }
        }, map));
    }

    private void registEnroll() {
        String userId = getUserId();
        String url = Constants.OUTRT_NET + "/unique_uid_" + userId + "/m/course_register";
        Map<String, String> map = new HashMap<>();
        map.put("course.id", courseId);
        map.put("state", "not_commit");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MCourseRegister>>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                toast(context, "请求失败,请稍后再试");
            }

            @Override
            public void onResponse(BaseResponseResult<MCourseRegister> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null && response.getResponseData().getId() != null) {
                    registId = response.getResponseData().getId();
                    isEnroll = true;
                    bt_signUp.setText("取消报读");
                    bt_signUp.setEnabled(true);
                    if (!iKnow)
                        tipDialog();
                } else {
                    if (response != null && response.getResponseMsg() != null) {
                        toast(context, response.getResponseMsg());
                    }
                }
            }
        }, map));
    }

    private boolean iKnow = false;

    private void tipDialog() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("温馨提示");
        dialog.setMessage("还差一步完成选课，请到已选中提交课程清单");
        dialog.setPositiveButton("我知道了", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                iKnow = true;
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                iKnow = true;
            }
        });
        dialog.show();
    }

    class TeacherPagerAdapter extends PagerAdapter {
        private List<View> pages = new ArrayList<>();
        private List<MobileUser> mDatas;

        public TeacherPagerAdapter(List<MobileUser> mDatas) {
            this.mDatas = mDatas;
            for (int i = 0; i < mDatas.size(); i++) {
                View view = getLayoutInflater().inflate(R.layout.course_teacher_list_item, null);
                pages.add(view);
            }
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return pages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = pages.get(position);
            ImageView ic_teacher = view.findViewById(R.id.ic_teacher);
            TextView tv_teacher_name = view.findViewById(R.id.tv_teacher_name);
            TextView tv_teacher_dept = view.findViewById(R.id.tv_teacher_dept);
            if (mDatas.get(position).getAvatar() != null) {
                GlideImgManager.loadCircleImage(context, mDatas.get(position).getAvatar(),
                        R.drawable.user_default, R.drawable.user_default, ic_teacher);
            } else {
                ic_teacher.setImageResource(R.drawable.user_default);
            }
            tv_teacher_name.setText(mDatas.get(position).getRealName());
            if (mDatas.get(position).getDeptName() != null) {
                tv_teacher_dept.setText(mDatas.get(position).getDeptName());
            } else {
                tv_teacher_dept.setVisibility(View.GONE);
            }
            container.addView(pages.get(position), 0);//添加页卡
            return pages.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);//删除页卡
        }
    }
}
