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
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.haoyu.app.adapter.AppSurveyAdapter;
import com.haoyu.app.adapter.TestGridAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.entity.SurveyAnswer;
import com.haoyu.app.entity.SurveyQuestionListResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;

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
 * 创建日期：2016/12/24 on 10:14
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppPageSurveyActivity extends BaseActivity implements View.OnClickListener {
    private AppPageSurveyActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.tv_undone)
    TextView tv_undone;
    @BindView(R.id.viewPagerContent)
    RelativeLayout viewPagerContent;
    @BindView(R.id.banner)
    ViewPager viewPager;    //测验页码
    @BindView(R.id.bt_commit)
    Button bt_commit;  //交卷
    @BindView(R.id.ll_right)
    LinearLayout ll_right;
    @BindView(R.id.iv_prev)
    Button iv_prev;
    @BindView(R.id.iv_next)
    Button iv_next;  //上一页，下一页
    @BindView(R.id.finishContent)
    RelativeLayout finishContent;   //提交问卷成功显示布局
    @BindView(R.id.bt_close)
    Button bt_close;
    @BindView(R.id.bt_check)
    Button bt_check;   //关闭或者查看结果
    private boolean canSubmit;
    private String surveyType;   //问卷类型(课程问卷，工作坊问卷)
    private String relationId;
    private String activityId;
    private String surveyId;
    private String surveyTitle;
    private String state;
    private SurveyPagerAdapter adapter;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_page_survey;
    }

    @Override
    public void initView() {
        canSubmit = getIntent().getBooleanExtra("canSubmit", false);
        surveyType = getIntent().getStringExtra("type");
        relationId = getIntent().getStringExtra("relationId");
        activityId = getIntent().getStringExtra("activityId");
        surveyId = getIntent().getStringExtra("surveyId");
        surveyTitle = getIntent().getStringExtra("surveyTitle");
        state = getIntent().getStringExtra("state");
        if (state != null && state.equals("complete")) {
            updateLayout();
        } else {
            loadData();
        }
    }

    List<SurveyAnswer> surveyAnswers = new ArrayList<>();

    public void loadData() {
        String url;
        if (surveyType.equals("course")) {
            url = Constants.OUTRT_NET + "/" + relationId + "/study/m/survey_question/" + surveyId;
        } else {
            url = Constants.OUTRT_NET + "/student_" + relationId + "/m/survey_question/" + surveyId;
        }
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<SurveyQuestionListResult>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(SurveyQuestionListResult response) {
                if (response != null && response.getResponseData() != null && response.getResponseData().size() > 0) {
                    surveyAnswers.addAll(response.getResponseData());
                    if (surveyAnswers.size() <= 1) {
                        ll_right.setVisibility(View.GONE);
                        bt_commit.setVisibility(View.VISIBLE);
                    }
                    adapter = new SurveyPagerAdapter(surveyAnswers);
                    viewPager.setAdapter(adapter);
                    toolBar.setTitle_text((viewPager.getCurrentItem() + 1) + "/" + surveyAnswers.size());
                    iv_prev.setEnabled(false);
                    showGestureDialog();
                }
            }
        }));
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
                Common.hideSoftInput(context);
                toolBar.getIv_rightImage().setBackgroundColor(ContextCompat.getColor(context, R.color.pressedColor));
                showPopupWindow();
            }
        });
        iv_prev.setOnClickListener(context);
        iv_next.setOnClickListener(context);
        bt_commit.setOnClickListener(context);
        bt_close.setOnClickListener(context);
        bt_check.setOnClickListener(context);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                Common.hideSoftInput(context);
            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    iv_prev.setEnabled(false);
                    if (surveyAnswers.size() > 1) {
                        iv_next.setEnabled(true);
                        ll_right.setVisibility(View.VISIBLE);
                        bt_commit.setVisibility(View.INVISIBLE);
                    } else {
                        iv_next.setEnabled(false);
                        ll_right.setVisibility(View.INVISIBLE);
                        bt_commit.setVisibility(View.VISIBLE);
                    }
                } else if (i == surveyAnswers.size() - 1) {
                    iv_next.setEnabled(false);
                    ll_right.setVisibility(View.INVISIBLE);
                    bt_commit.setVisibility(View.VISIBLE);
                } else {
                    ll_right.setVisibility(View.VISIBLE);
                    bt_commit.setVisibility(View.INVISIBLE);
                    iv_prev.setEnabled(true);
                    iv_next.setEnabled(true);
                }
                if (i > 0) {
                    if (finishMap.get(i - 1) != null) {   //跳题
                        tv_undone.setVisibility(View.GONE);
                    } else {
                        tv_undone.setVisibility(View.VISIBLE);
                    }
                } else if (i == 0) {
                    tv_undone.setVisibility(View.GONE);
                }
                toolBar.setTitle_text((i + 1) + "/" + surveyAnswers.size());
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        Common.hideSoftInput(context);
        switch (v.getId()) {
            case R.id.iv_prev:
                if (viewPager.getCurrentItem() > 0) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                }
                return;
            case R.id.iv_next:
                if (viewPager.getCurrentItem() < surveyAnswers.size()) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                }
                return;
            case R.id.bt_commit:
                if (!canSubmit) {
                    showMaterialDialog("提示", "活动已结束，无法提交问卷！");
                    return;
                }
                int undone = 0;
                for (Integer page : finishMap.keySet()) {
                    if (finishMap.get(page) == null)
                        undone++;
                }
                MaterialDialog dialog = new MaterialDialog(context);
                if (undone == 0 && checkText()) {
                    dialog.setTitle(null);
                    dialog.setMessage("您已完成全部答题，提交后无法修改或删除，要提交问卷？");
                    dialog.setPositiveButton("提交问卷", new MaterialDialog.ButtonClickListener() {
                        @Override
                        public void onClick(View v, AlertDialog dialog) {
                            commit();
                        }
                    });
                    dialog.setNegativeButton("再检查一下", null);
                } else {
                    dialog.setTitle("提示");
                    String message = "您还有" + undone + "道题尚未作答，请填写完所有题目再次提交！";
                    dialog.setMessage(message);
                    dialog.setPositiveButton("我知道了", null);
                }
                dialog.show();
                return;
            case R.id.bt_close:
                finish();
                return;
            case R.id.bt_check:
                Intent intent = new Intent(context, AppSurveyResultActivity.class);
                intent.putExtra("relationId", relationId);
                intent.putExtra("type", surveyType);
                intent.putExtra("activityId", activityId);
                intent.putExtra("surveyId", surveyId);
                intent.putExtra("surveyTitle", surveyTitle);
                startActivity(intent);
                finish();
                return;
        }
    }

    private boolean checkText() {
        for (final Integer page : textMap.keySet()) {
            if (textMap.get(page) != null && textMap.get(page).trim().length() > 0 && surveyAnswers.get(page).getMinWords() > 0) {
                String text = textMap.get(page).trim();
                int minWords = surveyAnswers.get(page).getMinWords();
                int maxWords = surveyAnswers.get(page).getMaxWords();
                MaterialDialog dialog = new MaterialDialog(context);
                dialog.setTitle("提示");
                if (text.length() < minWords) {
                    String message = "第<font color='#11B1D5'> " + (page + 1) + " </font>道题的答题不能少于 <font color='#11B1D5'>" + minWords + " </font>个字，请重新答题！";
                    Spanned spanned = Html.fromHtml(message);
                    dialog.setMessage(spanned);
                    dialog.setPositiveButton("我知道了", new MaterialDialog.ButtonClickListener() {
                        @Override
                        public void onClick(View v, AlertDialog dialog) {
                            viewPager.setCurrentItem(page);
                        }
                    });
                    dialog.show();
                    return false;
                } else if (text.length() > maxWords) {
                    String message = "第<font color='#11B1D5'> " + (page + 1) + " </font>道题的答题不能超过 <font color='#11B1D5'>" + maxWords + " </font>个字，请重新答题！";
                    Spanned spanned = Html.fromHtml(message);
                    dialog.setMessage(spanned);
                    dialog.setPositiveButton("我知道了", new MaterialDialog.ButtonClickListener() {
                        @Override
                        public void onClick(View v, AlertDialog dialog) {
                            viewPager.setCurrentItem(page);
                        }
                    });
                    dialog.show();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 提交问卷答案
     */
    private void commit() {
        String userId = getUserId();
        String url;
        final Map<String, String> map = new HashMap<>();
        if (surveyType != null && surveyType.equals("course")) {
            url = Constants.OUTRT_NET + "/" + activityId + "/study/unique_uid_" + userId + "/m/survey_user";
            map.put("relation.id", relationId);
        } else {
            url = Constants.OUTRT_NET + "/student_" + relationId + "/unique_uid_" + userId + "/m/survey_user";
            map.put("relation.id", relationId);
        }
        map.put("survey.id", surveyId);
        map.put("creator.id", userId);
        for (int i = 0; i < surveyAnswers.size(); i++) {
            map.put("surveySubmissions[" + i + "].question.id", surveyAnswers.get(i).getId());
            map.put("surveySubmissions[" + i + "].question.type", surveyAnswers.get(i).getType());
            if (!surveyAnswers.get(i).getType().equals(SurveyAnswer.textEntry) && finishMap.get(i) != null) {
                ArrayMap<Integer, Boolean> isSelected = finishMap.get(i);
                StringBuilder sb = new StringBuilder();
                for (int position : isSelected.keySet()) {
                    if (isSelected.get(position)) {
                        sb.append(surveyAnswers.get(i).getmChoices().get(position).getId());
                        sb.append(",");
                    }
                }
                if (sb.lastIndexOf(",") != -1)
                    sb.deleteCharAt(sb.lastIndexOf(","));
                map.put("surveySubmissions[" + i + "].response", sb.toString());
            } else {
                map.put("surveySubmissions[" + i + "].response", textMap.get(i));
            }
        }
        showTipDialog();
        Flowable.just(url).map(new Function<String, BaseResponseResult>() {
            @Override
            public BaseResponseResult apply(String url) throws Exception {
                return commitSurvey(url, map);
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
                    updateLayout();
                    CourseSectionActivity activity = response.getResponseData().getmActivityResult().getmActivity();
                    Intent intent = new Intent();
                    intent.putExtra("activity", activity);
                    setResult(RESULT_OK, intent);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                hideTipDialog();
            }
        });
    }

    private BaseResponseResult commitSurvey(String url, Map<String, String> map) throws Exception {
        String json = OkHttpClientManager.postAsString(context, url, map);
        BaseResponseResult response = new Gson().fromJson(json, BaseResponseResult.class);
        return response;
    }

    private AppActivityViewResult getActivityInfo() throws Exception {
        String url;
        if (surveyType != null && surveyType.equals("course"))
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

    private void updateLayout() {
        viewPagerContent.setVisibility(View.GONE);
        toolBar.getIv_rightImage().setVisibility(View.INVISIBLE);
        toolBar.setTitle_text("调研问卷");
        finishContent.setVisibility(View.VISIBLE);
    }

    /*此map用于页面切换时答案选择标记位置，标记题目是否已经完成*/
    private ArrayMap<Integer, ArrayMap<Integer, Boolean>> finishMap = new ArrayMap<>();
    private ArrayMap<Integer, String> textMap = new ArrayMap<>();

    /*测验页码适配器*/
    class SurveyPagerAdapter extends PagerAdapter {
        private List<SurveyAnswer> surveys;

        public SurveyPagerAdapter(List<SurveyAnswer> surveys) {
            this.surveys = surveys;
            for (int i = 0; i < surveys.size(); i++) {
                finishMap.put(i, null);
            }
        }

        @Override
        public int getCount() {
            return surveys.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View contentView = getLayoutInflater().inflate(R.layout.testorsurvey_page, null);
            updatePage(contentView, position);
            container.addView(contentView, 0);//添加页卡
            return contentView;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        private void updatePage(View contentView, final int position) {
            final SurveyAnswer data = surveys.get(position);
            TextView testType = contentView.findViewById(R.id.test_type);
            TextView testTitle = contentView.findViewById(R.id.test_title);
            RecyclerView recyclerView = contentView.findViewById(R.id.recyclerView);
            FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
            layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            EditText et_content = contentView.findViewById(R.id.et_content);
            String msg;
            if (data.getType().equals(SurveyAnswer.singleChoice)) {
                msg = "单选题";
            } else if (data.getType().equals(SurveyAnswer.multipleChoice)) {
                msg = "多选题";
            } else if (data.getType().equals(SurveyAnswer.trueOrFalse)) {
                msg = "是非题";
            } else {
                msg = "问答题";
            }
            if (!data.getType().equals(SurveyAnswer.textEntry)) {
                et_content.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                et_content.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
            et_content.setText(textMap.get(position));
            if (data.getMinWords() > 0 && data.getMaxWords() > 0) {
                et_content.setHint("回答内容不能少于" + data.getMinWords() + "字，不能多于" + data.getMaxWords() + "字");
            } else if (data.getMinWords() > 0) {
                et_content.setHint("回答内容不能少于" + data.getMinWords() + "字");
            } else if (data.getMaxWords() > 0) {
                et_content.setHint("回答内容不能多于" + data.getMaxWords() + "字");
            } else {
                et_content.setHint("此处填入回答内容");
            }
            if (canSubmit) {
                et_content.setEnabled(true);
            } else {
                et_content.setEnabled(false);
            }
            et_content.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().trim().length() > 0) {
                        finishMap.put(position, new ArrayMap<Integer, Boolean>());
                        textMap.put(position, s.toString());
                    } else {
                        finishMap.put(position, null);
                        textMap.put(position, null);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            testType.setText(msg);
            testTitle.setText("\u3000\u3000\u3000\u2000" + data.getTitle());
            ArrayMap<Integer, Boolean> hasSelectMap = finishMap.get(position);
            if (data.getmChoices() != null) {
                AppSurveyAdapter adapter = new AppSurveyAdapter(data.getmChoices(), canSubmit, data, hasSelectMap);
                recyclerView.setAdapter(adapter);
                adapter.setOnSelectListener(new AppSurveyAdapter.OnSelectListener() {
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
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);//删除页卡
        }
    }
}
