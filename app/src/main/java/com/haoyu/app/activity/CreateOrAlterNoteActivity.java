package com.haoyu.app.activity;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.dialog.PublicTipDialog;
import com.haoyu.app.entity.NoteEntity;
import com.haoyu.app.entity.NoteResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.OkHttpClientManager.ResultCallback;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建笔记，修改笔记界面
 */
public class CreateOrAlterNoteActivity extends BaseActivity implements OnClickListener {
    private String contentText;
    private CreateOrAlterNoteActivity context = this;
    private PublicTipDialog dialog;
    @BindView(R.id.et_content)
    EditText et_content;
    private boolean isAlter = false;
    private boolean isChanged = false;
    private NoteEntity note = null;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_submit)
    TextView tv_submit;
    @BindView(R.id.tv_title)
    TextView tv_title;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_create_note;
    }

    @Override
    public void initView() {
        overridePendingTransition(R.anim.fade_in, 0);
        isAlter = getIntent().getBooleanExtra("isAlter", false);
        if (isAlter) {
            tv_title.setText("改笔记");
            note = ((NoteEntity) getIntent().getSerializableExtra("note"));
            et_content.setText(note.getContent());
            Editable editable = et_content.getText();
            Selection.setSelection(editable, editable.length());
        } else {
            tv_title.setText("写笔记");
        }
    }

    @Override
    public void setListener() {
        iv_back.setOnClickListener(context);
        tv_submit.setOnClickListener(context);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_submit:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(context.getCurrentFocus()
                        .getWindowToken(), 0);
                if (et_content.getText().toString().trim().length() == 0) {
                    toast(context, "请输入笔记内容");
                } else {
                    if (isAlter) {
                        if (isChanged) {
                            alterNote();
                        }
                    } else {
                        createNote();
                    }
                }
                break;
        }
    }

    /* 修改笔记 */
    private void alterNote() {
        dialog = new PublicTipDialog(context);
        dialog.show();
        Map<String, String> map = new HashMap<String, String>();
        map.put("content", et_content.getText().toString());
        map.put("_method", "put");
        String url = Constants.OUTRT_NET + "/notes/" + note.getId();
        OkHttpClientManager.postAsyn(context, url, new ResultCallback<NoteResult>() {
            public void onError(Request request, Exception e) {
                dismiss();
                onNetWorkError(context);
            }

            public void onResponse(NoteResult response) {
                dismiss();
                if (response.getResponseCode().equals("00")) {
                    NoteEntity entity = response.getResponseData();
                    if (entity != null) {
                        MessageEvent event = new MessageEvent();
                        event.action = Action.ALTER_COURSE_NOTE;
                        event.obj = entity;
                        RxBus.getDefault().post(event);
                        finish();
                    } else {
                        if (response.getResponseMsg() != null) {
                            toast(context, response.getResponseMsg());
                        }
                    }
                }
            }
        }, map);
    }

    /* 创建笔记 */
    private void createNote() {
        dialog = new PublicTipDialog(context);
        dialog.show();
        Map<String, String> map = new HashMap<String, String>();
        map.put("relation.id", "sc_0fe4efc936b7439b9d4ecb82a3ec9d5e");
        map.put("relation.type", "course");
        map.put("content", et_content.getText().toString());
        String url = Constants.OUTRT_NET + "/notes";
        OkHttpClientManager.postAsyn(context, url, new ResultCallback<NoteResult>() {
            @Override
            public void onError(Request request, Exception e) {
                dismiss();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(NoteResult response) {
                dismiss();
                if (response.getResponseCode().equals("00")) {
                    NoteEntity entity = response.getResponseData();
                    if (entity != null) {
                        MessageEvent event = new MessageEvent();
                        event.action = Action.CREATE_COURSE_NOTE;
                        event.obj = entity;
                        RxBus.getDefault().post(event);
                        finish();
                    }
                } else {
                    if (response.getResponseMsg() != null) {
                        toast(context, response.getResponseMsg());
                    }
                }
            }
        }, map);
    }

    private void dismiss() {
        dialog.dismiss();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out);
    }
}
