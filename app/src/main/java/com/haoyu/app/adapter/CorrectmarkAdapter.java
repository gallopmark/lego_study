package com.haoyu.app.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.activity.MyMarkActivity;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.CourseTestEntity;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.ReceiveList;
import com.haoyu.app.entity.CorrectResult;
import com.haoyu.app.entity.MEvaluateItemSubmissions;
import com.haoyu.app.entity.MEvaluateSubmission;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.StarBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;

/**
 * Created by acer1 on 2017/3/10.
 * 学员互评
 */
public class CorrectmarkAdapter extends BaseArrayRecyclerAdapter<CorrectResult> {
    private MyMarkActivity mContext;
    private List<CorrectResult> correctResultList;

    private String aid;//活动id
    private String uid;//用户id
    //弹出框提示
    PopupWindow pop;
    private boolean isPop = false;
    private List<CourseTestEntity> testList = new ArrayList<>();
    private List<String> testPosition = new ArrayList<>();
    private double eveScore = 0;//得分
    private List<Integer> listPosition = new ArrayList<>();
    private List<Double> scorePosition = new ArrayList<>();
    private Map<Integer, MEvaluateSubmission> map = new HashMap<>();
    private int index = -1;
    public OpenResourceCallBack callBack;

    public CorrectmarkAdapter(MyMarkActivity context, List<CorrectResult> mDatas, String aid, String uid, List<ReceiveList> idList) {
        super(mDatas);
        this.mContext = context;
        this.correctResultList = mDatas;

        this.aid = aid;
        this.uid = uid;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final CorrectResult correctResult, final int position) {
        TextView mAssignmetnNum =
                holder.obtainView(R.id.assignment_num);
        final TextView mAssignmentScore =
                holder.obtainView(R.id.assignment_score);
        ;
        RecyclerView EvealuationList =
                holder.obtainView(R.id.content_evaluation_list);
        FullyLinearLayoutManager manager = new FullyLinearLayoutManager(mContext);
        manager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        EvealuationList.setLayoutManager(manager);
        RelativeLayout mShowAssignment =
                holder.obtainView(R.id.show_assignment);

        final LinearLayout mShowContent =
                holder.obtainView(R.id.show_content);
        final ImageView mAssignmetnImg =
                holder.obtainView(R.id.assignment_img);

        TextView mCorrectCommit =
                holder.obtainView(R.id.correct_commit);

        RecyclerView mFileListView = holder.obtainView(R.id.file_list);
        FullyLinearLayoutManager manager1 = new FullyLinearLayoutManager(mContext);
        manager1.setOrientation(FullyLinearLayoutManager.HORIZONTAL);
        mFileListView.setLayoutManager(manager1);

        if (correctResultList.size() > 0) {
            mAssignmetnNum.setText("作业" + (position + 1));
            if (correctResult.getResponseData() != null) {
                if (correctResult.getResponseData().getMEvaluateSubmission() != null && correctResult.getResponseData().getMEvaluateSubmission().getMEvaluateItemSubmissions() != null && correctResult.getResponseData().getMAssignmentUser() != null) {
                    String state = correctResult.getResponseData().getMAssignmentUser().getState();
                    EvaluationAdapter adapter = new EvaluationAdapter(position, state, correctResult.getResponseData().getMEvaluateSubmission().getMEvaluateItemSubmissions(), correctResult.getResponseData().getMAssignmentUser().getId());
                    EvealuationList.setAdapter(adapter);
                    int size = correctResult.getResponseData().getMEvaluateSubmission().getMEvaluateItemSubmissions().size();
                    if (correctResult.getResponseData().getMEvaluateSubmission().getMEvaluateItemSubmissions().size() > 0) {
                        map.put(position, correctResult.getResponseData().getMEvaluateSubmission());
                        double d = 0;
                        for (int i = 0; i < correctResult.getResponseData().getMEvaluateSubmission().getMEvaluateItemSubmissions().size(); i++) {
                            d += correctResult.getResponseData().getMEvaluateSubmission().getMEvaluateItemSubmissions().get(i).getScore();
                        }
                        mAssignmentScore.setText(String.valueOf((int) (d / size) + "分/" + 100 + "分"));
                    }

                    //打分
                    adapter.setOnCorrectEvaluationListener(new OnCorrectEvaluationListener() {
                        @Override
                        public void OnCorrectEvaluation(int parentPosition, int position, double score) {
                            eveScore = 0;
                            if (listPosition.contains(position) && listPosition.size() > 0 && scorePosition.size() > 0) {
                                int a = listPosition.indexOf(position);
                                scorePosition.remove(a);
                                listPosition.remove(a);

                            }
                            scorePosition.add(score);
                            listPosition.add(position);
                            List<MEvaluateItemSubmissions> submissionList = new ArrayList<>();
                            for (int i = 0; i < map.get(parentPosition).getMEvaluateItemSubmissions().size(); i++) {
                                if (position != i) {
                                    eveScore += map.get(parentPosition).getMEvaluateItemSubmissions().get(i).getScore();
                                    submissionList.add(map.get(parentPosition).getMEvaluateItemSubmissions().get(i));
                                } else {
                                    eveScore += score * 20;

                                    MEvaluateItemSubmissions submissions = map.get(parentPosition).getMEvaluateItemSubmissions().get(position);
                                    submissions.setScore(score * 20);
                                    submissionList.add(submissions);
                                }
                            }

                            MEvaluateSubmission submission = new MEvaluateSubmission();
                            submission.setMEvaluateItemSubmissions(submissionList);
                            map.put(parentPosition, submission);
                            int size = map.get(parentPosition).getMEvaluateItemSubmissions().size();
                            if (size > 0) {
                                //获取平均后的总分数
                                mAssignmentScore.setText(String.valueOf(((int) (eveScore / size))) + "分/" + 100 + "分");
                            }

                        }
                    });
                    mFileListView.setAdapter(new FileAdapter(correctResult.getResponseData().getMAssignmentUser().getmFileInfos()));
                }

            }
        }


        //
        mShowAssignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mShowContent.getVisibility() == View.GONE) {
                    mShowContent.setVisibility(View.VISIBLE);
                    mAssignmetnImg.setImageResource(R.drawable.zhankai);
                } else {
                    mShowContent.setVisibility(View.GONE);
                    mAssignmetnImg.setImageResource(R.drawable.course_dictionary_shouqi);
                }
            }
        });
        mCorrectCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index = position;
                initProvider(position);
            }
        });
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.correct_assignment_item;
    }

    class EvaluationAdapter extends BaseArrayRecyclerAdapter<MEvaluateItemSubmissions> {
        private List<MEvaluateItemSubmissions> evaluateItemSubmissionsList;
        public OnCorrectEvaluationListener OnCorrectEvaluationListener;
        private int parentPosition;

        public void setOnCorrectEvaluationListener(OnCorrectEvaluationListener correctEvaluationListener) {
            this.OnCorrectEvaluationListener = correctEvaluationListener;
        }

        public EvaluationAdapter(int position, String state, List<MEvaluateItemSubmissions> evaluateItemSubmissionsList, String evlationId) {
            super(evaluateItemSubmissionsList);
            this.evaluateItemSubmissionsList = evaluateItemSubmissionsList;

            this.parentPosition = position;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, MEvaluateItemSubmissions mEvaluateItemSubmissions, final int position) {
            TextView tv_pinglun_content1 =
                    holder.obtainView(R.id.tv_pinglun_content1);
            TextView mScoreNum =
                    holder.obtainView(R.id.score_num);
            final StarBar mRationBar =
                    holder.obtainView(R.id.ratingBar1);
            mRationBar.setFocusable(true);
            mRationBar.setClickable(true);
            if (evaluateItemSubmissionsList != null && evaluateItemSubmissionsList.get(position) != null && mEvaluateItemSubmissions != null && mEvaluateItemSubmissions.getContent() != null) {
                tv_pinglun_content1.setText(mEvaluateItemSubmissions.getContent());
                mScoreNum.setText("得分" + (position + 1) + ":");
                float markScore = (float) evaluateItemSubmissionsList.get(position).getScore() / 20;
                mRationBar.setStarMark(markScore);
            }

            mRationBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    float d = mRationBar.getStarMark();
                    MEvaluateItemSubmissions mEvaluateItemSubmissions = evaluateItemSubmissionsList.get(position);
                    if (testPosition.size() > 0 && testPosition.contains(String.valueOf(position)) && testList.size() > 0) {
                        int index = testPosition.indexOf(String.valueOf(position));
                        testPosition.remove(index);
                        testList.remove(index);
                    }
                    CourseTestEntity test = new CourseTestEntity();
                    test.setKey(mEvaluateItemSubmissions.getId());
                    test.setScore(String.valueOf((int) (mRationBar.getStarMark() * 20)));
                    testList.add(test);
                    OnCorrectEvaluationListener.OnCorrectEvaluation(parentPosition, position, d);
                }
            });

        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.correct_content_evaluation;
        }
    }

    class FileAdapter extends BaseArrayRecyclerAdapter<MFileInfo> {
        private List<MFileInfo> mFileInfosList;

        public FileAdapter(List<MFileInfo> mDatas) {
            super(mDatas);
            this.mFileInfosList = mDatas;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, MFileInfo mFileInfo, int position) {
            TextView mFileName = holder.obtainView(R.id.file_name);
            ImageView mFileType = holder.obtainView(R.id.file_img);
            if (mFileInfosList.size() > 0) {
                final MFileInfo fileInfos = mFileInfosList.get(position);
                mFileName.setText(fileInfos.getFileName());
                String type = fileInfos.getUrl();
                if (type.endsWith(".doc") || type.endsWith(".docx")) {
                    mFileType.setImageResource(R.drawable.resources_doc);
                } else if (type.endsWith(".xls") || type.endsWith(".xlsx")) {
                    mFileType.setImageResource(R.drawable.resources_xls);
                } else if (type.endsWith(".ppt") || holder.equals(".pptx")) {
                    mFileType.setImageResource(R.drawable.resources_ppt);
                } else if (type.endsWith("pdf")) {
                    mFileType.setImageResource(R.drawable.resources_ppt);
                } else {
                    mFileType.setImageResource(R.drawable.resources_unknown);
                }
                mFileType.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (callBack != null) {
                            callBack.open(fileInfos);
                        }
                    }
                });
            }
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.correct_filelist;
        }
    }

    private Map<String, String> map2 = new HashMap<>();

    //提交批阅
    private void submitAssignment(String esid, String mEvaluateRelationId, String mEvaluateRelation_RelationId) {
        String url = Constants.OUTRT_NET + "/" + aid + "/study/unique_uid_" + uid + "/m/evaluate/submission/" + esid + "?evaluateRelation.id=" + mEvaluateRelationId + "&evaluateRelation.relation.id=" + mEvaluateRelation_RelationId;
        if (testList.size() > 0) {
            for (int i = 0; i < testList.size(); i++) {
                CourseTestEntity t = testList.get(i);
                String test = "&evaluateItemSubmissionMap[" + t.getKey() + "].score=" + t.getScore();
                url += test;

            }
            if (map.get(index) != null && map.get(index).getMEvaluateItemSubmissions() != null && map.get(index).getMEvaluateItemSubmissions().size() > listPosition.size()) {
                for (int k = 0; k < map.get(index).getMEvaluateItemSubmissions().size(); k++) {
                    if (listPosition.size() > k && k != listPosition.get(k)) {
                        MEvaluateItemSubmissions item = map.get(index).getMEvaluateItemSubmissions().get(k);
                        url += "&evaluateItemSubmissionMap[" + item.getId() + "].score=" + item.getScore();
                    }
                }
            }
        } else {
            if (index != -1 && map != null && map.get(index) != null) {
                MEvaluateSubmission submission = map.get(index);
                for (int j = 0; j < submission.getMEvaluateItemSubmissions().size(); j++) {
                    MEvaluateItemSubmissions item = submission.getMEvaluateItemSubmissions().get(j);
                    String test = "&evaluateItemSubmissionMap[" + item.getId() + "].score=" + item.getScore();
                    url += test;
                }
            }

        }

        //获取提交批阅的结果
        OkHttpClientManager.putAsyn(mContext, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
                mContext.showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                mContext.hideTipDialog();
                mContext.toast(mContext, "提交失败，请稍后尝试");
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                mContext.hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    mContext.toast(mContext, "提交成功");
                    testList.clear();
                    listPosition.clear();
                    scorePosition.clear();
                    testList.clear();
                    testPosition.clear();
                } else {
                    mContext.toast(mContext, "提交失败，请稍后尝试");
                }
            }
        }, map2);

    }

    private void initProvider(final int i) {
        View v = mContext.getLayoutInflater().inflate(R.layout.activity_correct, null);
        View view = mContext.getLayoutInflater().inflate(R.layout.correct_dialog_assignment_grade, null);
        pop = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setBackgroundDrawable(new BitmapDrawable());
        view.getBackground().setAlpha(80);
        pop.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        TextView bt_Confirm = (TextView) view.findViewById(R.id.bt_Confirm);
        TextView bt_Cancel = (TextView) view.findViewById(R.id.bt_cancel);
        TextView popWarn = (TextView) view.findViewById(R.id.pop_warn);
        popWarn.setText("确认对作业" + (i + 1) + "的打分吗？\n提交后将不能进行修改!");
        bt_Cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (pop.isShowing()) {
                    isPop = false;
                    pop.dismiss();
                }
            }
        });
        bt_Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pop != null && pop.isShowing()) {
                    pop.dismiss();
                    isPop = true;
                }
                CorrectResult correctResult = correctResultList.get(i);
                if (correctResult != null && correctResult.getResponseData() != null && correctResult.getResponseData().getMEvaluateSubmission() != null && correctResult.getResponseData().getMAssignmentUser() != null) {
                    String esid = correctResultList.get(i).getResponseData().getMEvaluateSubmission().getId();
                    String evaluateRelationId = correctResult.getResponseData().getMEvaluateSubmission().getEvaluateRelationId();
                    String assignmentUserId = correctResult.getResponseData().getMAssignmentUser().getId();
                    submitAssignment(esid, evaluateRelationId, assignmentUserId);
                }
                isPop = false;
            }
        });

    }

    public interface OpenResourceCallBack {
        void open(MFileInfo mFileInfo);

    }

    interface OnCorrectEvaluationListener {
        void OnCorrectEvaluation(int parentPosition, int position, double score);
    }

    public void setCallBack(OpenResourceCallBack callBack) {
        this.callBack = callBack;
    }
}
