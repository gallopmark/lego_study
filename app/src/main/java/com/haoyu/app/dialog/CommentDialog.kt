package com.haoyu.app.dialog

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.ScreenUtils

/**
 * 创建日期：2018/1/26.
 * 描述:评论对话框
 * 作者:xiaoma
 */
class CommentDialog(context: Context) : AlertDialog(context) {
    private var hint: String? = null
    private var etText: String? = null
    private var btText: String? = null
    private var sendCommentListener: OnSendCommentListener? = null

    constructor(context: Context, hint: String) : this(context) {
        this.hint = hint
    }

    constructor(context: Context, hint: String, btText: String) : this(context) {
        this.hint = hint
        this.btText = btText
    }

    constructor(context: Context, hint: String, etText: String, btText: String) : this(context) {
        this.hint = hint
        this.etText = etText
        this.btText = btText
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        val window = window
        window.setContentView(R.layout.dialog_comment)
        val editText = findViewById<EditText>(R.id.et_content)
        editText.requestFocus()
        editText.isFocusable = true
        hint?.let { editText.hint = hint }
        etText?.let {
            editText.setText(it)
            editText.setSelection(it.length)//将光标移至文字末尾
        }
        val btSend = findViewById<Button>(R.id.bt_send)
        btSend.isEnabled = false
        btText?.let { btSend.text = btText }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btSend.isEnabled = s.toString().trim().isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        btSend.setOnClickListener {
            val content = editText.text.toString()
            sendCommentListener?.sendComment(content)
            dismiss()
        }
        window.setLayout(ScreenUtils.getScreenWidth(context), WindowManager.LayoutParams.WRAP_CONTENT)
        window.setWindowAnimations(R.style.dialog_anim)
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        window.setGravity(Gravity.BOTTOM)
    }

    interface OnSendCommentListener {
        fun sendComment(content: String)
    }

    fun setSendCommentListener(sendCommentListener: OnSendCommentListener) {
        this.sendCommentListener = sendCommentListener
    }
}