package com.haoyu.app.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import com.haoyu.app.activity.AppQuestionEditActivity
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Common

/**
 * 创建日期：2018/1/15.
 * 描述:课程学习问答（全部、我关注的、我提问的）
 * 作者:xiaoma
 */
class PageQuestionFragment : BaseFragment(), PageAllQuestionFragment.OnResponseListener {
    private lateinit var rbAllQuestion: RadioButton
    private var f1: PageAllQuestionFragment? = null
    private var f2: PageNoticeQuestionFragment? = null
    private var f3: PageMyQuestionFragment? = null
    private var relationId: String? = null

    override fun createView(): Int {
        return R.layout.fragment_page_question
    }

    override fun initView(view: View) {
        relationId = arguments?.getString("entityId")
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        rbAllQuestion = view.findViewById(R.id.rb_allQuestion)
        val btCreate = view.findViewById<Button>(R.id.bt_createQA)
        setTab(1)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_allQuestion -> {
                    setTab(1)
                }
                R.id.rb_myNotice -> {
                    setTab(2)
                }
                R.id.rb_myQuestion -> {
                    setTab(3)
                }
            }
        }
        btCreate.setOnClickListener({
            val intent = Intent()
            intent.setClass(context, AppQuestionEditActivity::class.java)
            intent.putExtra("relationId", relationId)
            intent.putExtra("relationType", "course_study")
            startActivity(intent)
        })
    }

    private fun setTab(tab: Int) {
        val transaction = childFragmentManager.beginTransaction()
        hideFragments(transaction)
        when (tab) {
            1 -> {
                if (f1 == null) {
                    f1 = PageAllQuestionFragment()
                    f1?.let {
                        val bundle = Bundle()
                        bundle.putString("type", "course")
                        bundle.putString("relationId", relationId)
                        bundle.putString("relationType", "course_study")
                        it.arguments = bundle
                        it.setOnResponseListener(this)
                        transaction.add(R.id.content, it)
                    }
                } else {
                    transaction.show(f1)
                }
            }
            2 -> {
                if (f2 == null) {
                    f2 = PageNoticeQuestionFragment()
                    f2?.let {
                        val bundle = Bundle()
                        bundle.putString("relationId", relationId)
                        it.arguments = bundle
                        transaction.add(R.id.content, it)
                    }
                } else {
                    transaction.show(f2)
                }
            }
            3 -> {
                if (f3 == null) {
                    f3 = PageMyQuestionFragment()
                    f3?.let {
                        val bundle = Bundle()
                        bundle.putString("type", "course")
                        bundle.putString("relationId", relationId)
                        bundle.putString("relationType", "course_study")
                        it.arguments = bundle
                        transaction.add(R.id.content, it)
                    }
                } else {
                    transaction.show(f3)
                }
            }
        }
        transaction.commit()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        if (f1 != null) {
            transaction.hide(f1)
        }
        if (f2 != null) {
            transaction.hide(f2)
        }
        if (f3 != null) {
            transaction.hide(f3)
        }
    }

    override fun getTotalCount(totalCount: Int) {
        rbAllQuestion.text = "全部 (${Common.formatNum(totalCount)})"
    }
}