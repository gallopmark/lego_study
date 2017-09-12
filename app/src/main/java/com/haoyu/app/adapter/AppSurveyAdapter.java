package com.haoyu.app.adapter;

import android.support.v4.util.ArrayMap;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.SurveyAnswer;
import com.haoyu.app.lego.student.R;

import java.util.List;

/**
 * 创建日期：2016/12/24 on 10:07
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppSurveyAdapter extends BaseArrayRecyclerAdapter<SurveyAnswer.MChoices> {
    private boolean canEdit;
    private SurveyAnswer surveyAnswer;
    private ArrayMap<Integer, Boolean> isSelected = new ArrayMap<>();

    public AppSurveyAdapter(List<SurveyAnswer.MChoices> mDatas, boolean canEdit, SurveyAnswer surveyAnswer, ArrayMap<Integer, Boolean> hasSelectMap) {
        super(mDatas);
        this.canEdit = canEdit;
        this.surveyAnswer = surveyAnswer;
        if (hasSelectMap != null) {
            isSelected = hasSelectMap;
        } else {
            for (int i = 0; i < mDatas.size(); i++) {
                isSelected.put(i, false);
            }
        }
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.test_answer_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, SurveyAnswer.MChoices mChoices, final int position) {
        View contentView = holder.obtainView(R.id.contentView);
        CheckBox cb_select = holder.obtainView(R.id.cb_select);
        TextView detail_tv = holder.obtainView(R.id.detail_tv);
        if (position < SurveyAnswer.MChoices.choices.length) {
            cb_select.setText(SurveyAnswer.MChoices.choices[position]);
        } else {
            cb_select.setText(SurveyAnswer.MChoices.choices[0]);
        }
        if (canEdit) {
            contentView.setEnabled(true);
            cb_select.setEnabled(true);
        } else {
            contentView.setEnabled(false);
            cb_select.setEnabled(false);
        }
        detail_tv.setText(mChoices.getContent());
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.contentView:
                        setChecked(position);
                        break;
                    case R.id.cb_select:
                        setChecked(position);
                        break;
                }
            }
        };
        contentView.setOnClickListener(listener);
        cb_select.setOnClickListener(listener);
        cb_select.setChecked(isSelected.get(position));
    }

    private void setChecked(int position) {
        boolean isChecked = !isSelected.get(position);
        if (surveyAnswer.getType().equals(SurveyAnswer.singleChoice) || surveyAnswer.getType().equals(SurveyAnswer.trueOrFalse)) {  //单选
            // 先将所有的置为FALSE
            for (Integer p : isSelected.keySet()) {
                isSelected.put(p, false);
            }
            // 再将当前选择CB的实际状态
            isSelected.put(position, isChecked);
        } else {    //多选
            int i = 0;
            for (boolean selected : isSelected.values()) {
                if (selected) {
                    i++;
                }
            }
            if (surveyAnswer.getMaxChoose() != 0 && i >= surveyAnswer.getMaxChoose()) {
                for (Integer p : isSelected.keySet()) {
                    isSelected.put(p, false);
                }
            }
            isSelected.put(position, isChecked);
        }
        if (selectListener != null) {
            selectListener.onSelect(isSelected);
        }
        notifyDataSetChanged();
    }

    private OnSelectListener selectListener;

    public void setOnSelectListener(OnSelectListener selectListener) {
        this.selectListener = selectListener;
    }

    public interface OnSelectListener {
        void onSelect(ArrayMap<Integer, Boolean> isSelected);
    }
}
