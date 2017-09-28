package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.haoyu.app.adapter.AppTestAdapter;
import com.haoyu.app.adapter.TestGridAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.AppTestMobileEntity;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 创建日期：2016/11/28 on 9:14
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppPageTestActivity extends BaseActivity implements View.OnClickListener {
    private AppPageTestActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.tv_undone)
    TextView tv_undone;
    @BindView(R.id.banner)
    ViewPager viewPager;    //测验页码
    @BindView(R.id.bt_submit)
    Button bt_submit;  //交卷
    @BindView(R.id.iv_prev)
    Button iv_prev;
    @BindView(R.id.iv_next)
    Button iv_next;  //上一页，下一页
    private AppTestMobileEntity mTestEntity;
    private boolean canSubmit;     //是否可以提交
    private String testType, relationId, activityId, tuid;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_page_test;
    }

    @Override
    public void initView() {
        canSubmit = getIntent().getBooleanExtra("canSubmit", false);
        testType = getIntent().getStringExtra("testType");
        relationId = getIntent().getStringExtra("relationId");
        mTestEntity = (AppTestMobileEntity) getIntent().getSerializableExtra("mTestEntity");
        activityId = getIntent().getStringExtra("activityId");
        tuid = getIntent().getStringExtra("tuid");
    }

    List<AppTestMobileEntity.AppTestQuestion> testList = new ArrayList<>();

    public void initData() {
        if (mTestEntity != null && mTestEntity.getmQuestions() != null) {
            testList.addAll(mTestEntity.getmQuestions());
            TestPagerAdapter adapter = new TestPagerAdapter(mTestEntity.getmQuestions());
            viewPager.setAdapter(adapter);
            toolBar.setTitle_text((viewPager.getCurrentItem() + 1) + "/" + testList.size());
            if (testList.size() < 1)
                bt_submit.setVisibility(View.VISIBLE);
            iv_prev.setEnabled(false);
            showGestureDialog();
        }
    }

    private void showGestureDialog() {
        final Dialog dialog = new Dialog(context, R.style.GestureDialog);
        dialog.show();
        View view = getLayoutInflater().inflate(R.layout.dialog_gesture_tips, null);
        view.findViewById(R.id.bt_know).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        dialog.setContentView(view, params);
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
                toolBar.getIv_rightImage().setBackgroundColor(ContextCompat.getColor(context, R.color.pressedColor));
                showPopupWindow();
            }
        });
        iv_prev.setOnClickListener(context);
        iv_next.setOnClickListener(context);
        bt_submit.setOnClickListener(context);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    iv_prev.setEnabled(false);
                    if (testList.size() > 1)
                        iv_next.setEnabled(true);
                    else {
                        iv_next.setEnabled(false);
                        bt_submit.setVisibility(View.VISIBLE);
                    }
                } else if (i >= testList.size() - 1) {
                    iv_next.setEnabled(false);
                    bt_submit.setVisibility(View.VISIBLE);
                } else {
                    bt_submit.setVisibility(View.GONE);
                    iv_prev.setEnabled(true);
                    iv_next.setEnabled(true);
                }
                if (i > 0) {
                    if (finishMap.get(i - 1) != null && finishMap.get(i - 1).size() > 0) {   //跳题
                        tv_undone.setVisibility(View.GONE);
                    } else {
                        tv_undone.setVisibility(View.VISIBLE);
                    }
                } else if (i == 0) {
                    tv_undone.setVisibility(View.GONE);
                }
                toolBar.setTitle_text((i + 1) + "/" + testList.size());
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_prev:
                if (viewPager.getCurrentItem() > 0) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                }
                return;
            case R.id.iv_next:
                if (viewPager.getCurrentItem() < testList.size()) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                }
                return;
            case R.id.bt_submit:
                if (!canSubmit) {
                    showMaterialDialog("提示", "活动已结束，无法提交答卷！");
                    return;
                }
                int undone = 0;
                for (Integer page : finishMap.keySet()) {
                    if (finishMap.get(page) == null)
                        undone++;
                }
                MaterialDialog dialog = new MaterialDialog(context);
                dialog.setTitle("提示");
                String message;
                String positiveText;
                String negativeText;
                if (undone == 0) {
                    message = "您已答完所有题目，要交卷吗？";
                    positiveText = "再检查一下";
                    negativeText = "交卷";
                } else {
                    message = "您还有" + undone + "道题尚未作答，要交卷吗？";
                    negativeText = "交卷";
                    positiveText = "继续作答";
                }
                dialog.setMessage(message);
                dialog.setPositiveButton(positiveText, null);
                dialog.setNegativeButton(negativeText, new MaterialDialog.ButtonClickListener() {
                    @Override
                    public void onClick(View v, AlertDialog dialog) {
                        commit();
                    }
                });
                dialog.show();
                return;
        }
    }

    /*交卷*/
    private void commit() {
        String userId = getUserId();
        String url;
        if (testType != null && testType.equals("course")) {
            url = Constants.OUTRT_NET + "/" + activityId + "/study/unique_uid_" + userId + "/m/test/user/" + tuid + "/finishTest";
        } else {
            url = Constants.OUTRT_NET + "/student_" + relationId + "/unique_uid_" + userId + "/m/test/user/" + tuid + "/finishTest";
        }
        List<OkHttpClientManager.Param> paramsList = new ArrayList<>();
        paramsList.add(new OkHttpClientManager.Param("_method", "put"));
        for (int i = 0; i < testList.size(); i++) {
            paramsList.add(new OkHttpClientManager.Param("qti_item_" + testList.get(i).getItemKey() + "_RESPONSE", ""));
            if (finishMap.get(i) != null) {
                ArrayMap<Integer, Boolean> isSelected = finishMap.get(i);
                for (int position : isSelected.keySet()) {
                    if (isSelected.get(position)) {
                        paramsList.add(new OkHttpClientManager.Param("qti_response_" + testList.get(i).getItemKey() + "_RESPONSE", testList.get(i).getInteractionOptions().get(position).getId()));
                    }
                }
            }
        }
        final OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[paramsList.size()];
        for (int i = 0; i < paramsList.size(); i++) {
            params[i] = paramsList.get(i);
        }
        showTipDialog();
        addSubscription(Flowable.just(url).map(new Function<String, BaseResponseResult>() {
            @Override
            public BaseResponseResult apply(String url) throws Exception {
                return commitTest(url, params);
            }
        }).map(new Function<BaseResponseResult, AppActivityViewResult>() {
            @Override
            public AppActivityViewResult apply(BaseResponseResult response) throws Exception {
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00"))
                    return getActivityInfo();
                return null;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<AppActivityViewResult>() {
            @Override
            public void accept(AppActivityViewResult response) throws Exception {
                hideTipDialog();
                if (response != null && response.getResponseData() != null && response.getResponseData().getmActivityResult() != null && response.getResponseData().getmActivityResult().getmActivity() != null) {
                    toastFullScreen("您已完成交卷", true);
                    CourseSectionActivity activity = response.getResponseData().getmActivityResult().getmActivity();
                    Intent intent = new Intent();
                    intent.putExtra("activity", activity);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                hideTipDialog();
            }
        }));
    }

    private BaseResponseResult commitTest(String url, OkHttpClientManager.Param[] params) throws Exception {
        String json = OkHttpClientManager.postAsString(context, url, params);
        BaseResponseResult response = new Gson().fromJson(json, BaseResponseResult.class);
        return response;
    }

    private AppActivityViewResult getActivityInfo() throws Exception {
        String url;
        if (testType != null && testType.equals("course"))
            url = Constants.OUTRT_NET + "/" + activityId + "/study/m/activity/ncts/" + activityId + "/view";
        else
            url = Constants.OUTRT_NET + "/student_" + relationId + "/m/activity/wsts/" + activityId + "/view";
        String json = OkHttpClientManager.getAsString(context, url);
        AppActivityViewResult result = new Gson().fromJson(json, AppActivityViewResult.class);
        return result;
    }

    private void showPopupWindow() {
        final View popupView = getLayoutInflater().inflate(R.layout.test_pulldown_layout, null);
        final PopupWindow mPopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true);
        GridView gridView = popupView.findViewById(R.id.gridView);
        ArrayMap<Integer, Boolean> arrayMap = new ArrayMap<>();
        for (Integer p : finishMap.keySet()) {
            if (finishMap.get(p) != null) {
                arrayMap.put(p, true);
            } else {
                arrayMap.put(p, false);
            }
        }
        TestGridAdapter adapter = new TestGridAdapter(context, arrayMap);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewPager.setCurrentItem(position);
                mPopupWindow.dismiss();
            }
        });
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.windowbackground));
        mPopupWindow.setBackgroundDrawable(colorDrawable);
        final View view = toolBar.getIv_rightImage();
        mPopupWindow.showAsDropDown(view);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.defaultColor));
            }
        });
    }

    /*此map用于页面切换时答案选择标记位置，标记题目是否已经完成*/
    private ArrayMap<Integer, ArrayMap<Integer, Boolean>> finishMap = new ArrayMap<>();

    /*测验页码适配器*/
    class TestPagerAdapter extends PagerAdapter {
        private List<AppTestMobileEntity.AppTestQuestion> tests;

        public TestPagerAdapter(List<AppTestMobileEntity.AppTestQuestion> tests) {
            this.tests = tests;
            for (int i = 0; i < tests.size(); i++) {
                finishMap.put(i, null);
            }
        }

        @Override
        public int getCount() {
            return tests.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View contentView = getLayoutInflater().inflate(R.layout.testorsurvey_page, null);
            updatePage(position, contentView);
            container.addView(contentView, 0);//添加页卡
            return contentView;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        private void updatePage(final int position, View contentView) {
            final AppTestMobileEntity.AppTestQuestion data = tests.get(position);
            TextView testType = contentView.findViewById(R.id.test_type);
            TextView testTitle = contentView.findViewById(R.id.test_title);
            RecyclerView recyclerView = contentView.findViewById(R.id.recyclerView);
            FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
            layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            String msg;
            if (data.getQuesType().equals(AppTestMobileEntity.AppTestQuestion.SINGLE_CHOICE)) {
                msg = "单选题";
            } else if (data.getQuesType().equals(AppTestMobileEntity.AppTestQuestion.MULTIPLE_CHOICE)) {
                msg = "多选题";
            } else if (data.getQuesType().equals(AppTestMobileEntity.AppTestQuestion.TRUE_FALSE)) {
                msg = "是非题";
            } else {
                msg = "问答题";
            }
            testType.setText(msg);
            testTitle.setText("\u3000\u3000\u3000\u2000" + data.getTitle());
            ArrayMap<Integer, Boolean> hasSelectMap = finishMap.get(position);
            AppTestAdapter adapter = new AppTestAdapter(data.getInteractionOptions(), canSubmit, data.getQuesType(), hasSelectMap);
            recyclerView.setAdapter(adapter);
            adapter.setOnSelectListener(new AppTestAdapter.OnSelectListener() {
                @Override
                public void onSelect(ArrayMap<Integer, Boolean> isSelected) {
                    boolean isSelect = false;
                    for (Integer i : isSelected.keySet()) {
                        if (isSelected.get(i)) {
                            isSelect = true;
                        }
                    }
                    if (isSelect) {
                        finishMap.put(position, isSelected);
                    } else {
                        finishMap.put(position, null);
                    }
                }
            });
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);//删除页卡
        }
    }
}
