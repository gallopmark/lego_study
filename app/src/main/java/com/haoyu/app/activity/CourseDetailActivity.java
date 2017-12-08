package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.CourseDetailData;
import com.haoyu.app.entity.CourseMobileEntity;
import com.haoyu.app.entity.MCourseRegister;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;

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
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.iv_image)
    ImageView iv_image;  //课程封面图片
    @BindView(R.id.tv_title)
    TextView tv_title; // 课程标题
    @BindView(R.id.tv_type)
    TextView tv_type; //课程类型
    @BindView(R.id.tv_period)
    TextView tv_period; // 学时
    @BindView(R.id.tv_enroll)
    TextView tv_enroll; // 报读数
    @BindView(R.id.tv_emptyTeacher)
    TextView tv_emptyTeacher;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.tv_summary)
    TextView tv_summary;
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
        int height = ScreenUtils.getScreenHeight(context) / 7 * 2;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv_image.getLayoutParams();
        params.height = height;
        iv_image.setLayoutParams(params);
    }

    /**
     * 通过课程Id获取课程相关信息
     */
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/course_center/detail/" + courseId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<CourseDetailData>>() {

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
            public void onResponse(BaseResponseResult<CourseDetailData> response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                }
            }
        }));
    }

    private void updateUI(CourseDetailData responseData) {
        scrollView.setVisibility(View.VISIBLE);
        bt_signUp.setVisibility(View.VISIBLE);
        if (responseData.getmCourse() != null) {
            setDetail(responseData.getmCourse());
        }
        if (responseData.getmCourseRegister() != null) {
            setSignState(responseData.getmCourseRegister());
        }
        if (responseData.getTeachers().size() > 0) {
            setTeachers(responseData.getTeachers());
        } else {
            recyclerView.setVisibility(View.GONE);
            tv_emptyTeacher.setVisibility(View.VISIBLE);
        }
    }

    private void setDetail(CourseMobileEntity entity) {
        if (entity.getImage() != null) {
            GlideImgManager.loadImage(context.getApplicationContext(), entity.getImage(), R.drawable.app_default,
                    R.drawable.app_default, iv_image);
        } else {
            iv_image.setImageResource(R.drawable.app_default);
        }
        tv_title.setText(entity.getTitle());
        tv_type.setText(entity.getType());
        tv_period.setText(String.valueOf(entity.getStudyHours()) + "学时");
        tv_enroll.setText(entity.getRegisterNum() + "人报读");
        if (entity.getIntro() != null && entity.getIntro().length() > 0) {
            Spanned spanned = Html.fromHtml(entity.getIntro(), new HtmlHttpImageGetter(tv_summary, Constants.REFERER, true), null);
            tv_summary.setText(spanned);
        } else {
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.empty_content);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            int drawablePadding = PixelFormat.dp2px(context, 16);
            tv_summary.setText("暂无简介");
            tv_summary.setGravity(Gravity.CENTER);
            tv_summary.setTextColor(ContextCompat.getColor(context, R.color.blow_gray));
            tv_summary.setCompoundDrawables(null, drawable, null, null);
            tv_summary.setCompoundDrawablePadding(drawablePadding);
        }
    }

    private void setSignState(MCourseRegister register) {
        registId = register.getId();
        String state = register.getState();
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

    private void setTeachers(List<MobileUser> teachers) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        TeacherAdapter adapter = new TeacherAdapter(teachers);
        recyclerView.setAdapter(adapter);
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

    private class TeacherAdapter extends BaseArrayRecyclerAdapter<MobileUser> {

        public TeacherAdapter(List<MobileUser> mDatas) {
            super(mDatas);
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.course_teacher_item;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, MobileUser user, int position) {
            ImageView ic_teacher = holder.obtainView(R.id.ic_teacher);
            TextView tv_name = holder.obtainView(R.id.tv_name);
            TextView tv_dept = holder.obtainView(R.id.tv_dept);
            if (user.getAvatar() != null) {
                GlideImgManager.loadCircleImage(context, user.getAvatar(), R.drawable.user_default, R.drawable.user_default, ic_teacher);
            } else {
                ic_teacher.setImageResource(R.drawable.user_default);
            }
            tv_name.setText(user.getRealName());
            if (TextUtils.isEmpty(user.getDeptName())) {
                tv_dept.setVisibility(View.GONE);
            } else {
                tv_dept.setVisibility(View.VISIBLE);
                tv_dept.setText(user.getDeptName());
            }
        }
    }
}
