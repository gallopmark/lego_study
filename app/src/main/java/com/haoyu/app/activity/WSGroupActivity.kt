package com.haoyu.app.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.RadioButton
import android.widget.RadioGroup
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.fragment.WSGroupFragment
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Common
import com.haoyu.app.view.AppToolBar

/**
 * 创建日期：2018/1/30.
 * 描述:工作坊列表界面
 * 作者:xiaoma
 */
class WSGroupActivity : BaseActivity(), WSGroupFragment.OnResponseListener {
    private val context = this
    private val radioButtons = arrayOfNulls<RadioButton>(2)
    private val fragments = arrayOfNulls<Fragment>(2)
    private var tab = 1

    override fun setLayoutResID(): Int {
        return R.layout.activity_wsgroup
    }

    override fun initView() {
        val toolBar = findViewById<AppToolBar>(R.id.toolBar)
        toolBar.setOnLeftClickListener { finish() }
        radioButtons[0] = findViewById(R.id.rb_related)
        radioButtons[1] = findViewById(R.id.rb_all)
        setTab(tab)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_related -> setTab(1)
                else -> setTab(2)
            }
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
                    fragments[0] = WSGroupFragment()
                    fragments[0]?.let {
                        it.arguments = Bundle().apply { putInt("type", 1) }
                        (it as WSGroupFragment).setOnResponseListener(context)
                    }
                    transaction.add(R.id.container, fragments[0])
                } else {
                    transaction.show(fragments[0])
                }
            }
            else -> {
                if (fragments[1] == null) {
                    fragments[1] = WSGroupFragment()
                    fragments[1]?.let {
                        it.arguments = Bundle().apply { putInt("type", 2) }
                        (it as WSGroupFragment).setOnResponseListener(context)
                    }
                    transaction.add(R.id.container, fragments[1])
                } else {
                    transaction.show(fragments[1])
                }
            }
        }
        transaction.commit()
    }

    override fun getTotalCount(totalCount: Int) {
        when (tab) {
            1 -> radioButtons[0]?.text = "与我相关（${Common.formatNum(totalCount)}）"
            else -> radioButtons[1]?.text = "全部（${Common.formatNum(totalCount)}）"
        }
    }
}