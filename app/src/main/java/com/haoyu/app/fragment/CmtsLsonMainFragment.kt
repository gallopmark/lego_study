package com.haoyu.app.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.lego.student.R
import java.math.BigDecimal

/**
 * 创建日期：2018/1/18.
 * 描述:教研创课
 * 作者:xiaoma
 */
class CmtsLsonMainFragment : BaseFragment(), CmtsLsonChildFragment.OnResponseListener {
    private val radioButtons = arrayOfNulls<RadioButton>(2)
    private val texts = arrayOfNulls<String>(2)
    private val fragments = arrayOfNulls<Fragment>(2)
    private var tab = 1

    override fun createView(): Int {
        return R.layout.fragment_cmtsmain
    }

    override fun initView(view: View) {
        radioButtons[0] = view.findViewById(R.id.rb_all)
        radioButtons[1] = view.findViewById(R.id.rb_my)
        texts[0] = resources.getString(R.string.gen_class_all)
        texts[1] = resources.getString(R.string.gen_class_my)
        radioButtons[0]?.text = texts[0]
        radioButtons[1]?.text = texts[1]
        setTab(1)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_all -> {
                    setTab(1)
                }
                R.id.rb_my -> {
                    setTab(2)
                }
            }
        }
    }

    private fun setTab(tab: Int) {
        this.tab = tab
        val transaction = childFragmentManager.beginTransaction()
        for (fragment in fragments) {
            fragment?.let {
                transaction.hide(it)
            }
        }
        when (tab) {
            1 -> {
                if (fragments[0] == null) {
                    fragments[0] = CmtsLsonChildFragment()
                    fragments[0]?.let {
                        val bundle = Bundle()
                        bundle.putInt("type", 1)
                        it.arguments = bundle
                        (it as CmtsLsonChildFragment).setOnResponseListener(this)
                        transaction.add(R.id.content, it)
                    }
                } else {
                    transaction.show(fragments[0])
                }
            }
            2 -> {
                if (fragments[1] == null) {
                    fragments[1] = CmtsLsonChildFragment()
                    fragments[1]?.let {
                        val bundle = Bundle()
                        bundle.putInt("type", 2)
                        it.arguments = bundle
                        (it as CmtsLsonChildFragment).setOnResponseListener(this)
                        transaction.add(R.id.content, it)
                    }
                } else {
                    transaction.show(fragments[1])
                }
            }
        }
        transaction.commit()
    }

    override fun getTotalCount(totalCount: Int) {
        when (tab) {
            1 -> {
                radioButtons[0]?.text = texts[0] + "（" + getCount(totalCount) + "）"
            }
            2 -> {
                radioButtons[1]?.text = texts[1] + "（" + getCount(totalCount) + "）"
            }
        }
    }

    private fun getCount(count: Int): String {
        if (count < 10000) return count.toString()
        var num = count.toDouble() / 10000
        var bd = BigDecimal(num)
        num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).toDouble()
        if (num < 10000) {
            when {
                num / 1000 > 1 -> {
                    bd = BigDecimal(num / 1000)
                    num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).toDouble()
                    return num.toString() + "千万"
                }
                num / 100 > 1 -> {
                    bd = BigDecimal(num / 100)
                    num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).toDouble()
                    return num.toString() + "百万"
                }
                num / 10 > 1 -> {
                    bd = BigDecimal(num / 10)
                    num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).toDouble()
                    return num.toString() + "十万"
                }
                else -> return num.toString() + "万"
            }
        }
        return "大于1亿"
    }
}