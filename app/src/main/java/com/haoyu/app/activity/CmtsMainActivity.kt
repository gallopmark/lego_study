package com.haoyu.app.activity

import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.widget.RadioGroup
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.fragment.CmtsLsonMainFragment
import com.haoyu.app.fragment.CmtsMovMainFragment
import com.haoyu.app.fragment.CmtsSaysMainFragment
import com.haoyu.app.lego.student.R
import com.haoyu.app.view.AppToolBar

/**
 * 创建日期：2018/1/17.
 * 描述:研修社区
 * 作者:xiaoma
 */
class CmtsMainActivity : BaseActivity() {
    private lateinit var context: CmtsMainActivity
    private lateinit var toolBar: AppToolBar
    private val lines = arrayOfNulls<View>(3)
    private val fragments = arrayOfNulls<Fragment>(3)
    private var selected = 1
    override fun setLayoutResID(): Int {
        return R.layout.activity_cmts_main
    }

    override fun initView() {
        context = this
        setToolBar()
        lines[0] = findViewById(R.id.line_says)
        lines[1] = findViewById(R.id.line_class)
        lines[2] = findViewById(R.id.line_activity)
        setTab(1)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_says -> {
                    setTab(1)
                }
                R.id.rb_class -> {
                    setTab(2)
                }
                R.id.rb_activity -> {
                    setTab(3)
                }
            }
        }
    }

    private fun setToolBar() {
        toolBar = findViewById(R.id.toolBar)
        toolBar.setOnTitleClickListener(object : AppToolBar.TitleOnClickListener {
            override fun onLeftClick(view: View) {
                finish()
            }

            override fun onRightClick(view: View) {
                when (selected) {
                    1 -> {
                        startActivity(Intent(context, CmtsSaysEditActivity::class.java))
                    }
                    2 -> {
                        startActivity(Intent(context, CmtsLessonCreateActivity::class.java))
                    }
                }
            }
        })
    }

    private fun setTab(tab: Int) {
        selected = tab
        for (view in lines) {
            view?.visibility = View.INVISIBLE
        }
        val transaction = supportFragmentManager.beginTransaction()
        hideFragments(transaction)
        when (tab) {
            1 -> {
                toolBar.setShow_right_button(true)
                lines[0]?.visibility = View.VISIBLE
                if (fragments[0] == null) {
                    fragments[0] = CmtsSaysMainFragment()
                    transaction.add(R.id.content, fragments[0])
                } else {
                    transaction.show(fragments[0])
                }
            }
            2 -> {
                toolBar.setShow_right_button(false)
                lines[1]?.visibility = View.VISIBLE
                if (fragments[1] == null) {
                    fragments[1] = CmtsLsonMainFragment()
                    transaction.add(R.id.content, fragments[1])
                } else
                    transaction.show(fragments[1])
            }
            3 -> {
                toolBar.setShow_right_button(false)
                lines[2]?.visibility = View.VISIBLE
                if (fragments[2] == null) {
                    fragments[2] = CmtsMovMainFragment()
                    transaction.add(R.id.content, fragments[2])
                } else
                    transaction.show(fragments[2])
            }
        }
        transaction.commit()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        for (fragment in fragments) {
            fragment?.let {
                transaction.hide(it)
            }
        }
    }
}