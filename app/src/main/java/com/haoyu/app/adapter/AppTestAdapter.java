package com.haoyu.app.adapter;

import android.support.v4.util.ArrayMap;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.AppTestMobileEntity;
import com.haoyu.app.lego.student.R;

import java.util.List;

/**
 * 创建日期：2016/12/1 on 10:50
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppTestAdapter extends BaseArrayRecyclerAdapter<AppTestMobileEntity.InteractionOptions> {
    private boolean canEdit;
    private String testType;
    private ArrayMap<Integer, Boolean> isSelected = new ArrayMap<>();

    public AppTestAdapter(List<AppTestMobileEntity.InteractionOptions> mDatas, boolean canEdit, String testType, ArrayMap<Integer, Boolean> hasSelectMap) {
        super(mDatas);
        this.canEdit = canEdit;
        this.testType = testType;
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
    public void onBindHoder(RecyclerHolder holder, AppTestMobileEntity.InteractionOptions testAnswer, final int position) {
        View contentView = holder.obtainView(R.id.contentView);
        final CheckBox cb_select = holder.obtainView(R.id.cb_select);
        TextView detail_tv = holder.obtainView(R.id.detail_tv);
        if (canEdit) {
            contentView.setEnabled(true);
            cb_select.setEnabled(true);
        } else {
            contentView.setEnabled(false);
            cb_select.setEnabled(false);
        }
        if (position < AppTestMobileEntity.InteractionOptions.choices.length) {
            cb_select.setText(AppTestMobileEntity.InteractionOptions.choices[position]);
        } else {
            cb_select.setText(AppTestMobileEntity.InteractionOptions.choices[0]);
        }
        detail_tv.setText(testAnswer.getText());
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.contentView:
                        if (cb_select.isChecked())
                            cb_select.setChecked(false);
                        else
                            cb_select.setChecked(true);
                        setChecked(position, cb_select.isChecked());
                        break;
                    case R.id.cb_select:
                        setChecked(position, cb_select.isChecked());
                        break;
                }
            }
        };
        contentView.setOnClickListener(listener);
        cb_select.setOnClickListener(listener);
        cb_select.setChecked(isSelected.get(position));
    }

    private void setChecked(int position, boolean isChecked) {
        if (testType.equals(AppTestMobileEntity.AppTestQuestion.MULTIPLE_CHOICE)) {  //多选
            isSelected.put(position, isChecked);
        } else {    //单选
            // 先将所有的置为FALSE
            for (Integer p : isSelected.keySet()) {
                isSelected.put(p, false);
            }
            // 再将当前选择CB的实际状态
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
