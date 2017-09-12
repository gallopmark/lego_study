package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.AppTestMobileEntity;
import com.haoyu.app.entity.MTestSubmission;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.FullyLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 创建日期：2017/3/6 on 9:58
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppTestResultListAdapter extends BaseArrayRecyclerAdapter<AppTestMobileEntity.AppTestQuestion> {

    private Context context;
    private Map<String, MTestSubmission> testSubmissionMap;
    private List<String> mChoices = new ArrayList<>();
    private String choices[] = new String[]{"A", "B", "C", "D", "E",
            "F", "G", "H", "I", "J", "K", "L", "M", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public AppTestResultListAdapter(Context context, List<AppTestMobileEntity.AppTestQuestion> mDatas, Map<String, MTestSubmission> testSubmissionMap) {
        super(mDatas);
        this.context = context;
        this.testSubmissionMap = testSubmissionMap;
        for (int i = 0; i < 30; i++) {
            mChoices.add("Choice" + i);
        }
    }

    public void addDatas(List<AppTestMobileEntity.AppTestQuestion> mList, Map<String, MTestSubmission> testMap) {
        mDatas.addAll(mList);
        testSubmissionMap.putAll(testMap);
        notifyDataSetChanged();
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.app_test_result_list_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, AppTestMobileEntity.AppTestQuestion question, int position) {
        TextView tv_testIndex = holder.obtainView(R.id.tv_testIndex);
        TextView test_type = holder.obtainView(R.id.test_type);
        TextView test_title = holder.obtainView(R.id.test_title);
        RecyclerView recyclerView = holder.obtainView(R.id.recyclerView);
        LinearLayout tipLayout = holder.obtainView(R.id.tipLayout);
        TextView tv_correct = holder.obtainView(R.id.tv_correct);
        TextView tv_isCorrect = holder.obtainView(R.id.tv_isCorrect);
//        TextView tv_correctAnswer = holder.obtainView(R.id.tv_correctAnswer);
        if (testSubmissionMap.get(question.getId()) != null && testSubmissionMap.get(question.getId()).isCorrect()) {
            tipLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            tv_testIndex.setBackgroundResource(R.drawable.app_test_answer_correct);
            tv_correct.setText("√");
            tv_isCorrect.setText("正确");
            tv_correct.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
            tv_isCorrect.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
//            tv_correctAnswer.setVisibility(View.GONE);
        } else {
            tipLayout.setBackgroundResource(R.drawable.shape_test_result_error);
            tv_testIndex.setBackgroundResource(R.drawable.app_test_answer_error);
            tv_correct.setText("×");
            tv_isCorrect.setText("错误");
            tv_correct.setTextColor(ContextCompat.getColor(context, R.color.red));
            tv_isCorrect.setTextColor(ContextCompat.getColor(context, R.color.red));
//            tv_correctAnswer.setVisibility(View.VISIBLE);
        }
//        if (testSubmissionMap.get(question.getId()) != null && testSubmissionMap.get(question.getId()).getCandidateResponses() != null) {
//            tv_correctAnswer.setText("正确答案:" + getChoice(testSubmissionMap.get(question.getId()).getCandidateResponses()));
//        } else {
//            tv_correctAnswer.setText("正确答案:..");
//        }
        String msg = "";
        if (question.getQuesType().equals(AppTestMobileEntity.AppTestQuestion.SINGLE_CHOICE)) {
            msg = "单选题";
        } else if (question.getQuesType().equals(AppTestMobileEntity.AppTestQuestion.MULTIPLE_CHOICE)) {
            msg = "多选题";
        } else if (question.getQuesType().equals(AppTestMobileEntity.AppTestQuestion.TRUE_FALSE)) {
            msg = "是非题";
        } else {
            msg = "问答题";
        }
        tv_testIndex.setText(String.valueOf(position + 1));
        test_type.setText(msg);
        test_title.setText("\u3000\u3000\u3000\u2000" + question.getTitle());
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
        layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        QuestionAdapter questionAdapter = new QuestionAdapter(context, question.getInteractionOptions(), question.getId());
        recyclerView.setAdapter(questionAdapter);
    }

    class QuestionAdapter extends BaseArrayRecyclerAdapter<AppTestMobileEntity.InteractionOptions> {

        private Context context;
        private List<Integer> choiceIndexs = new ArrayList<>();

        public QuestionAdapter(Context context, List<AppTestMobileEntity.InteractionOptions> mDatas, String questionId) {
            super(mDatas);
            this.context = context;
            if (testSubmissionMap.get(questionId) != null && testSubmissionMap.get(questionId).getCandidateResponses() != null) {
                choiceIndexs = getChoiceIndex(testSubmissionMap.get(questionId).getCandidateResponses());
            }
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.app_test_result_question_item;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, AppTestMobileEntity.InteractionOptions entity, int position) {
            TextView tv_select = holder.obtainView(R.id.tv_select);
            TextView detail_tv = holder.obtainView(R.id.detail_tv);
            if (position < AppTestMobileEntity.InteractionOptions.choices.length) {
                tv_select.setText(AppTestMobileEntity.InteractionOptions.choices[position]);
            } else {
                tv_select.setText("..");
            }
            if (choiceIndexs.contains(position)) {
                tv_select.setBackgroundResource(R.drawable.test_select_check);
                tv_select.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                tv_select.setBackgroundResource(R.drawable.test_select_default);
                tv_select.setTextColor(ContextCompat.getColor(context, R.color.black));
            }
            detail_tv.setText(entity.getText());
        }
    }

    private String getChoice(List<String> mDatas) {
        StringBuilder sb = new StringBuilder();
        for (String str : mDatas) {
            int index = mChoices.indexOf(str);
            if (index != -1 && index < choices.length) {
                sb.append(choices[index]);
                sb.append("、");
            } else {
                sb.append("..");
                sb.append("、");
            }
        }
        sb.deleteCharAt(sb.lastIndexOf("、"));
        return sb.toString();
    }

    private List<Integer> getChoiceIndex(List<String> mDatas) {
        List<Integer> indexs = new ArrayList<>();
        for (String str : mDatas) {
            int index = mChoices.indexOf(str);
            if (index != -1 && index < choices.length) {
                indexs.add(index);
            }
        }
        return indexs;
    }
}
