package com.haoyu.app.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import com.haoyu.app.activity.CourseFaqEditActivity
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Common

/**
 * 创建日期：2018/1/15.
 * 描述:课程学习问答（全部、我关注的、我提问的）
 * 作者:xiaoma
 */
class PageQuestionFragment : BaseFragment(), CourseFaqFragment.OnResponseListener {
    private lateinit var rbAllQuestion: RadioButton
    private val fragments = arrayOfNulls<Fragment>(3)
    private var relationId: String? = null
    private var text: String? = null

    override fun createView(): Int {
        return R.layout.fragment_page_question
    }

    override fun initView(view: View) {
        relationId = arguments?.getString("entityId")
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        rbAllQuestion = view.findViewById(R.id.rb_allQuestion)
        val btCreate = view.findViewById<Button>(R.id.bt_createQA)
        text = resources.getString(R.string.allQuestion)
        setTab(1)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_allQuestion -> setTab(1)
                R.id.rb_myNotice -> setTab(2)
                R.id.rb_myQuestion -> setTab(3)
            }
        }
        btCreate.setOnClickListener({
            val intent = Intent()
            intent.setClass(context, CourseFaqEditActivity::class.java)
            intent.putExtra("relationId", relationId)
            startActivity(intent)
        })
    }

    private fun setTab(tab: Int) {
        val transaction = childFragmentManager.beginTransaction()
        hideFragments(transaction)
        when (tab) {
            1 -> {
                if (fragments[0] == null) {
                    fragments[0] = CourseFaqFragment()
                    val bundle = Bundle().apply {
                        putInt("type", 1)
                        putString("relationId", relationId)
                        putString("relationType", "course_study")
                    }
                    fragments[0]?.arguments = bundle
                    (fragments[0] as CourseFaqFragment).setOnResponseListener(this)
                    transaction.add(R.id.content, fragments[0])
                } else {
                    transaction.show(fragments[0])
                }
            }
            2 -> {
                if (fragments[1] == null) {
                    fragments[1] = CourseFaqFragment()
                    val bundle = Bundle().apply {
                        putInt("type", 2)
                        putString("relationId", relationId)
                        putString("relationType", "course_study")
                    }
                    fragments[1]?.arguments = bundle
                    transaction.add(R.id.content, fragments[1])
                } else {
                    transaction.show(fragments[1])
                }
            }
            3 -> {
                if (fragments[2] == null) {
                    fragments[2] = CourseFaqFragment()
                    val bundle = Bundle().apply {
                        putInt("type", 3)
                        putString("relationId", relationId)
                        putString("relationType", "course_study")
                    }
                    fragments[2]?.arguments = bundle
                    transaction.add(R.id.content, fragments[2])
                } else {
                    transaction.show(fragments[2])
                }
            }
        }
        transaction.commit()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        for (fragment in fragments) {
            fragment?.let { transaction.hide(it) }
        }
    }

    override fun getTotalCount(totalCount: Int) {
        rbAllQuestion.text = "$text（${Common.formatNum(totalCount)}）"
    }
}