package com.haoyu.app.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.fragment.WSQuestionFragment
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Common
import com.haoyu.app.view.AppToolBar

/**
 * 创建日期：2018/1/31.
 * 描述:工作坊互助问答页面
 * 作者:xiaoma
 */
class WSQuestionActivity : BaseActivity(), WSQuestionFragment.OnResponseListener {
    private val context = this
    private lateinit var toolBar: AppToolBar
    private lateinit var tvBottom: TextView
    private val radioButtons = arrayOfNulls<RadioButton>(2)
    private val texts = arrayOfNulls<String>(2)
    private val fragments = arrayOfNulls<Fragment>(2)
    private var relationId: String? = null
    private var tab = 1

    override fun setLayoutResID(): Int {
        return R.layout.activity_wsquestion
    }

    override fun initView() {
        relationId = intent.getStringExtra("relationId")
        toolBar = findViewById(R.id.toolBar)
        tvBottom = findViewById(R.id.tvBottom)
        toolBar.setOnLeftClickListener { finish() }
        radioButtons[0] = findViewById(R.id.rb_allQuestion)
        radioButtons[1] = findViewById(R.id.rb_myQuestion)
        texts[0] = resources.getString(R.string.allQuestion)
        texts[1] = resources.getString(R.string.myAskQuestion)
        setTab(1)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_allQuestion -> setTab(1)
                else -> setTab(2)
            }
        }
        tvBottom.setOnClickListener {
            val intent = Intent(context, WSQuestionEditActivity::class.java)
            intent.putExtra("relationId", relationId)
            startActivity(intent)
        }
    }

    private fun setTab(tab: Int) {
        this.tab = tab
        val transaction = supportFragmentManager.beginTransaction()
        for (fragment in fragments) {
            fragment?.let { transaction.hide(it) }
        }
        when (tab) {
            1 -> {
                if (fragments[0] == null) {
                    fragments[0] = WSQuestionFragment()
                    val bundle = Bundle().apply {
                        putInt("type", 1)
                        putString("relationId", relationId)
                        putString("relationType", "workshop_question")
                    }
                    fragments[0]?.let {
                        (it as WSQuestionFragment).setOnResponseListener(context)
                    }
                    fragments[0]?.arguments = bundle
                    transaction.add(R.id.content, fragments[0])
                } else {
                    transaction.show(fragments[0])
                }
            }
            else -> {
                if (fragments[1] == null) {
                    fragments[1] = WSQuestionFragment()
                    val bundle = Bundle().apply {
                        putInt("type", 2)
                        putString("relationId", relationId)
                        putString("relationType", "workshop_question")
                    }
                    fragments[1]?.arguments = bundle
                    transaction.add(R.id.content, fragments[1])
                } else {
                    transaction.show(fragments[1])
                }
            }
        }
        transaction.commit()
    }

    override fun getTotalCount(totalCount: Int) {
        if (tvBottom.visibility != View.VISIBLE) tvBottom.visibility = View.VISIBLE
        when (tab) {
            1 -> {
                radioButtons[0]?.text = "${texts[0]}（${Common.formatNum(totalCount)}）"
            }
            2 -> {
                radioButtons[1]?.text = "${texts[1]}（${Common.formatNum(totalCount)}）"
            }
        }
    }
}