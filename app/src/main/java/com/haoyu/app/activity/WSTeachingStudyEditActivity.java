package com.haoyu.app.activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.haoyu.app.adapter.DictEntryAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.DictEntryMobileEntity;
import com.haoyu.app.entity.DictEntryResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * Created by acer1 on 2017/2/16.
 * 听课评课
 */
public class WSTeachingStudyEditActivity extends BaseActivity implements View.OnClickListener {
    private WSTeachingStudyEditActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.et_content)
    EditText et_content;//标题，内容
    @BindView(R.id.ll_version)
    LinearLayout ll_version;
    @BindView(R.id.tv_version)
    TextView tv_version;//教材版本
    @BindView(R.id.ll_lecturer)
    LinearLayout ll_lecturer;
    @BindView(R.id.tv_lecture)
    TextView tv_lecturer;//授课人
    @BindView(R.id.tv_field)
    TextView tv_field;//现场评课
    @BindView(R.id.tv_record)
    TextView tv_record;//录制评课
    @BindView(R.id.tv_stages)
    TextView tv_stages;
    @BindView(R.id.tv_subjecta)
    TextView tv_subjecta;
    private String workshopId;
    private String workSectionId;//阶段id
    private List<DictEntryMobileEntity> subjectList;//学科集合
    private List<DictEntryMobileEntity> stageList;//学段集合
    private int REQUEST_USER = 1, REQUEST_ACTIVITY = 2;
    private MobileUser mUser;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_ws_teaching_study_edit;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        workSectionId = getIntent().getStringExtra("workSectionId");
        tv_lecturer.setText(getRealName());
        registRxBus();
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        ll_version.setOnClickListener(context);
        ll_lecturer.setOnClickListener(context);
        tv_field.setOnClickListener(context);
        tv_record.setOnClickListener(context);
        tv_stages.setOnClickListener(context);
        tv_subjecta.setOnClickListener(context);
    }

    @Override
    public void onClick(View v) {
        Common.hideSoftInput(context);
        switch (v.getId()) {
            case R.id.ll_version:
                /*教材版本*/
                CommentDialog dialog = new CommentDialog(context, "输入教材版本", "完成");
                dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
                    @Override
                    public void sendComment(String content) {
                        tv_version.setText(content);
                    }
                });
                dialog.show();
                break;
            case R.id.ll_lecturer:
                /*授课人*/
                Intent intent = new Intent(context, TeachingStudyLeacturerActivity.class);
                startActivityForResult(intent, REQUEST_USER);
                break;
            case R.id.tv_field:
                /*现场评课*/
                putIntent(false);
                break;
            case R.id.tv_record:
                /*实录评课*/
                putIntent(true);
                break;
            case R.id.tv_stages:
                Common.hideSoftInput(context);
                if (stageList == null) {
                    initStage();
                } else {
                    showStageWindow();
                }
                break;
            case R.id.tv_subjecta:
                if (subjectList == null) {
                    initSubject();
                } else {
                    showSubjectWindow();
                }
                break;
        }
    }

    private void putIntent(boolean needFile) {
        String title = et_title.getText().toString().toString().trim();
        String content = et_content.getText().toString().trim();
        String stages = tv_stages.getText().toString().trim();
        String subjects = tv_subjecta.getText().toString().trim();
        String bookVersion = tv_version.getText().toString().trim();
        String userName = tv_lecturer.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            showMaterialDialog("提示", "请输入标题");
        } else if (TextUtils.isEmpty(content)) {
            showMaterialDialog("提示", "请输入评课方向");
        } else if (TextUtils.isEmpty(stages)) {
            showMaterialDialog("提示", "请选择学段");
        } else if (TextUtils.isEmpty(subjects)) {
            showMaterialDialog("提示", "请选择学科");
        } else if (TextUtils.isEmpty(userName)) {
            showMaterialDialog("提示", "请选择授课人");
        } else if (TextUtils.isEmpty(bookVersion)) {
            showMaterialDialog("提示", "请输入教材版本");
        } else {
            Intent intent = new Intent(context, WSTeachingStudySubmitActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("content", content);
            intent.putExtra("workshopId", workshopId);
            intent.putExtra("workSectionId", workSectionId);
            intent.putExtra("bookversion", bookVersion);
            if (mUser != null) {
                intent.putExtra("lectureId", mUser.getId());
            } else {
                intent.putExtra("lectureId", getUserId());
            }
            intent.putExtra("workshopId", workshopId);
            intent.putExtra("workSectionId", workSectionId);
            intent.putExtra("needFile", false);
            intent.putExtra("stageId", stageId);
            intent.putExtra("subjectId", subjectId);
            intent.putExtra("needFile", needFile);
            startActivityForResult(intent, REQUEST_ACTIVITY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USER && resultCode == RESULT_OK && data != null) {
            mUser = (MobileUser) data.getSerializableExtra("user");
            tv_lecturer.setText(mUser.getRealName());
        } else if (requestCode == REQUEST_ACTIVITY && resultCode == RESULT_OK && data != null) {
            setResult(RESULT_OK, data);
            finish();
        }
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

    //
    private String stageId, subjectId;
    private int stageIndex = -1, subjectIndex = -1;

    private void showStageWindow() {
        Drawable shouqi = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(),
                shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_xiala);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(),
                zhankai.getMinimumHeight());
        tv_stages.setCompoundDrawables(null, null, shouqi, null);
        View view = View.inflate(context, R.layout.popupwindow_listview,
                null);
        final PopupWindow stageWindwow = new PopupWindow(view, tv_stages.getWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        stageWindwow.setFocusable(true);
        stageWindwow.setBackgroundDrawable(new BitmapDrawable());
        stageWindwow.setOutsideTouchable(true);
        stageWindwow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv_stages.setCompoundDrawables(null, null, zhankai, null);
            }
        });
        ListView lv = view.findViewById(R.id.listView);
        final DictEntryAdapter adapter = new DictEntryAdapter(context, stageList, stageIndex);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                stageIndex = position;
                stageWindwow.dismiss();
                stageId = stageList.get(position).getTextBookValue();

                tv_stages.setText(stageList.get(position).getTextBookName());
            }
        });
        stageWindwow.showAsDropDown(tv_stages);
    }

    /*获取学科 */
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
                onNetWorkError(context);
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

    private void showSubjectWindow() {
        Drawable shouqi = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(),
                shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_xiala);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(),
                zhankai.getMinimumHeight());
        tv_subjecta.setCompoundDrawables(null, null, shouqi, null);
        View view = View.inflate(context, R.layout.popupwindow_listview,
                null);
        final PopupWindow subjectWindow = new PopupWindow(view, tv_subjecta.getWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        subjectWindow.setFocusable(true);
        subjectWindow.setBackgroundDrawable(new BitmapDrawable());
        subjectWindow.setOutsideTouchable(true);
        subjectWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv_subjecta.setCompoundDrawables(null, null, zhankai, null);
            }
        });
        ListView lv = view.findViewById(R.id.listView);
        final DictEntryAdapter adapter = new DictEntryAdapter(context, subjectList, subjectIndex);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                subjectIndex = position;
                subjectWindow.dismiss();
                subjectId = subjectList.get(position).getTextBookValue();
                tv_subjecta.setText(subjectList.get(position).getTextBookName());
            }
        });
        subjectWindow.showAsDropDown(tv_subjecta);
    }
}
