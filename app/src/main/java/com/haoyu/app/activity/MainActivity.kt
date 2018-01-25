package com.haoyu.app.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArrayMap
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.google.gson.Gson
import com.haoyu.app.adapter.MyTrainCourseAdapter
import com.haoyu.app.adapter.MyTrainWSAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.base.LegoApplication
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.*
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.service.VersionUpdateService
import com.haoyu.app.utils.*
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.zxing.CodeUtils
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Request

/**
 * 创建日期：2018/1/23.
 * 描述:主页面
 * 作者:xiaoma
 */
class MainActivity : BaseActivity(), View.OnClickListener {
    private var context = this
    private lateinit var ivToggle: ImageView
    private lateinit var ivScan: View  //扫一扫
    private lateinit var ivMsg: ImageView
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var nsvContent: NestedScrollView
    private lateinit var tvMyTrain: TextView
    private val llLayouts = arrayOfNulls<LinearLayout>(3)
    private lateinit var llContent: LinearLayout
    private lateinit var rvCourse: RecyclerView
    private lateinit var rvWorkshop: RecyclerView
    private lateinit var courseAdapter: MyTrainCourseAdapter
    private lateinit var wsAdapter: MyTrainWSAdapter
    private val courseDatas = ArrayList<CourseMobileEntity>()
    private val wsDatas = ArrayList<WorkShopMobileEntity>()
    private val myTrains = ArrayList<MyTrainMobileEntity>()
    private lateinit var menu: SlidingMenu
    private val menuViews = arrayOfNulls<TextView>(8)
    private val mInfoMap = ArrayMap<String, MyTrainInfo>()  //将已经加载的数据添加到map集合，避免重复加载
    private var showCommuity: Boolean = false  //课程是否是自主选课，是否限制学时
    private var ids: String? = null
    private var trainId: String? = null
    private var trainingTime: TimePeriod? = null
    override fun setLayoutResID(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        findViews()
        rvCourse.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        rvWorkshop.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        courseAdapter = MyTrainCourseAdapter(context, courseDatas)
        rvCourse.adapter = courseAdapter
        wsAdapter = MyTrainWSAdapter(context, wsDatas)
        rvWorkshop.adapter = wsAdapter
        initMenu()
        getVersion()
    }

    private fun findViews() {
        ivToggle = findViewById(R.id.ivToggle)
        ivScan = findViewById(R.id.iv_scan)
        ivMsg = findViewById(R.id.iv_msg)
        loadingView = findViewById(R.id.loadingView)
        loadFailView = findViewById(R.id.loadFailView)
        nsvContent = findViewById(R.id.nsv_content)
        tvMyTrain = findViewById(R.id.tv_myTrain)
        llContent = findViewById(R.id.ll_content)
        llLayouts[0] = findViewById(R.id.ll_course)
        llLayouts[1] = findViewById(R.id.ll_workshop)
        llLayouts[2] = findViewById(R.id.ll_community)
        rvCourse = findViewById(R.id.rv_course)
        rvWorkshop = findViewById(R.id.rv_workshop)
    }

    private fun initMenu() {
        menu = SlidingMenu(context)
        menu.mode = SlidingMenu.LEFT
        // 设置触摸屏幕的模式
        menu.touchModeAbove = SlidingMenu.TOUCHMODE_FULLSCREEN
        menu.setShadowWidthRes(R.dimen.shadow_width)
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset)
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f)
        menu.attachToActivity(context, SlidingMenu.SLIDING_CONTENT)
        val menuView = layoutInflater.inflate(R.layout.app_homepage_menu, LinearLayout(context), false)
        initMenuView(menuView)
        menu.menu = menuView
        getUserInfo()
    }

    private fun initMenuView(menuView: View) {
        val llUserInfo = menuView.findViewById<LinearLayout>(R.id.ll_userInfo)
        llUserInfo.setOnClickListener { startActivityForResult(Intent(context, AppUserInfoActivity::class.java), 2) }
        val ivUserIco = menuView.findViewById<ImageView>(R.id.iv_userIco)
        GlideImgManager.loadCircleImage(context, avatar, R.drawable.user_default, R.drawable.user_default, ivUserIco)
        val tvUserName = menuView.findViewById<TextView>(R.id.tv_userName)
        val tvDeptName = menuView.findViewById<TextView>(R.id.tv_deptName)
        tvUserName.text = if (TextUtils.isEmpty(realName)) "请填写用户名" else realName
        tvDeptName.text = if (TextUtils.isEmpty(deptName)) "请选择单位" else deptName
        menuViews[0] = menuView.findViewById(R.id.tv_learn)  //学习
        menuViews[1] = menuView.findViewById(R.id.tv_teaching) //教研
        menuViews[2] = menuView.findViewById(R.id.tv_peer)  //同行
        menuViews[3] = menuView.findViewById(R.id.tv_message)  //消息
        menuViews[4] = menuView.findViewById(R.id.tv_course)  //选课中心
        menuViews[5] = menuView.findViewById(R.id.tv_workshop)   //工作坊群
        menuViews[6] = menuView.findViewById(R.id.tv_consulting)  //教务咨询
        menuViews[7] = menuView.findViewById(R.id.tv_settings)  //设置
        for (view in menuViews) {
            view?.setOnClickListener(context)
        }
    }

    private fun getUserInfo() {
        val url = "${Constants.OUTRT_NET}/m/user/$userId"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<MobileUser>>() {
            override fun onError(request: Request?, e: Exception) {

            }

            override fun onResponse(response: BaseResponseResult<MobileUser>?) {
                response?.responseData?.let {
                    updateUI(it)
                }
            }
        }))
    }

    private fun updateUI(user: MobileUser) {
        val ivUserIco = menu.menu?.findViewById<ImageView>(R.id.iv_userIco)
        GlideImgManager.loadCircleImage(applicationContext, user.avatar, R.drawable.user_default, R.drawable.user_default, ivUserIco)
        val tvUserName = menu.menu?.findViewById<TextView>(R.id.tv_userName)
        tvUserName?.text = if (TextUtils.isEmpty(user.realName)) "请填写用户名" else user.realName
        val tvDeptName = menu.findViewById<TextView>(R.id.tv_deptName)
        tvDeptName.text = if (TextUtils.isEmpty(user.getmDepartment()?.deptName)) "请选择单位" else user.getmDepartment().deptName
    }

    override fun initData() {
        val url = "${Constants.OUTRT_NET}/m/uc/listMyTrain"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<List<MyTrainMobileEntity>>>() {

            override fun onBefore(request: Request) {
                loadFailView.visibility = View.GONE
                loadingView.visibility = View.VISIBLE
            }

            override fun onError(request: Request, e: Exception) {
                loadingView.visibility = View.GONE
                loadFailView.visibility = View.VISIBLE
            }

            override fun onResponse(response: BaseResponseResult<List<MyTrainMobileEntity>>?) {
                loadingView.visibility = View.GONE
                if (response?.getResponseData() != null) {
                    updateData(response.getResponseData())
                } else {
                    loadFailView.visibility = View.VISIBLE
                }
            }
        }))
    }

    private fun updateData(mDatas: List<MyTrainMobileEntity>) {
        if (mDatas.isNotEmpty()) {
            nsvContent.visibility = View.VISIBLE
            myTrains.addAll(mDatas)
            val sb = StringBuilder()
            for (i in 0 until myTrains.size) {
                sb.append(myTrains[i].id)
                if (i < myTrains.size - 1) {
                    sb.append(",")
                }
            }
            ids = sb.toString()
            tvMyTrain.text = myTrains[0].name
            trainId = mDatas[0].id
            trainingTime = mDatas[0].getmTrainingTime()
            trainId?.let { getTrainInfo(it) }
        } else {
            onEmptyTrain()
        }
    }

    private fun onEmptyTrain() {
        findViewById<LinearLayout>(R.id.ll_emptyTrain).visibility = View.VISIBLE
        val tvCmtsLearn = findViewById<TextView>(R.id.tv_cmtsLearn)
        tvCmtsLearn.setOnClickListener({ startActivity(Intent(context, CmtsMainActivity::class.java)) })
    }

    /*获取个人培训信息*/
    private fun getTrainInfo(trainId: String) {
        if (mInfoMap[trainId] == null) {
            onRequest(trainId)
        } else {
            val response = mInfoMap[trainId]
            updateUI(response)
        }
    }

    private fun onRequest(trainId: String) {
        val url = "${Constants.OUTRT_NET}/m/uc/getUserTrainInfo?trainId=$trainId"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<MyTrainInfo>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onError(trainId)
            }

            override fun onResponse(response: MyTrainInfo) {
                hideTipDialog()
                mInfoMap[trainId] = response
                updateUI(response)
            }
        }))
    }

    private fun onError(trainId: String) {
        val lfv = findViewById<LoadFailView>(R.id.lfv)
        lfv.visibility = View.VISIBLE
        lfv.setOnRetryListener { getTrainInfo(trainId) }
    }

    private fun updateUI(response: MyTrainInfo?) {
        if (llContent.visibility != View.VISIBLE) llContent.visibility = View.VISIBLE
        response?.responseData?.let {
            it.trainResult?.let {
                updateTrainInfoUI(it)
            }
            it.getmCourseRegisters()?.let {
                updateCourseListUI(it)
            }
            it.getmWorkshopUsers()?.let {
                updateWorkShopListUI(it)
            }
            it.getmCommunityResult()?.let {
                updateCommunityListUI(it)
            }
        }
    }

    /*更新培训情况信息*/
    private fun updateTrainInfoUI(trainResult: MyTrainInfo.TrainResult) {
        val tvCourseResult = findViewById<TextView>(R.id.tv_courseResult)
        if (trainResult.trainType != null && !trainResult.trainType.contains("course")) {
            tvCourseResult.text = "此项无需考核"
        } else {
            tvCourseResult.text = "已选${trainResult.registerCourseNum}合格${trainResult.passCourseNum}门"
        }
        val tvWSResult = findViewById<TextView>(R.id.tv_workShopResult)
        if (trainResult.trainType != null && !trainResult.trainType.contains("workshop")) {
            tvWSResult.text = "此项无需考核"
        } else {
            tvWSResult.text = "获得${trainResult.getWstsPoint}/${trainResult.wstsPoint}积分"
        }
        val tvCmtsResult = findViewById<TextView>(R.id.tv_communityResult)
        if (trainResult.trainType != null && !trainResult.trainType.contains("community")) {
            showCommuity = false
            tvCmtsResult.text = "此项无需考核"
        } else {
            showCommuity = true
            tvCmtsResult.text = "获得${trainResult.getCmtsPoint}/${trainResult.cmtsPoint}积分"
        }
    }

    /*更新课程列表*/
    private fun updateCourseListUI(list: List<MyTrainInfo.CourseRegisters>) {
        courseDatas.clear()
        for (register in list) {
            register.getmCourse()?.let {
                it.state = register.state
                courseDatas.add(it)
            }
        }
        val tvEmpty = findViewById<TextView>(R.id.tv_emptyCourse)
        if (courseDatas.isNotEmpty()) {
            courseAdapter.notifyDataSetChanged()
            rvCourse.visibility = View.VISIBLE
            rvCourse.isFocusable = false
            tvEmpty.visibility = View.GONE
        } else {
            rvCourse.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        }
    }

    /*更新工作坊列表*/
    private fun updateWorkShopListUI(mDatas: List<WorkShopMobileUser>) {
        wsDatas.clear()
        for (wsUser in mDatas) {
            wsUser.getmWorkshop()?.let {
                it.point = wsUser.point
                wsDatas.add(it)
            }
        }
        val tvEmpty = findViewById<TextView>(R.id.tv_emptyWS)
        if (wsDatas.isNotEmpty()) {
            wsAdapter.notifyDataSetChanged()
            rvWorkshop.visibility = View.VISIBLE
            rvWorkshop.isFocusable = false
            tvEmpty.visibility = View.GONE
        } else {
            rvWorkshop.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        }
    }

    private fun updateCommunityListUI(cmts: MyTrainInfo.CommunityResult) {
        val layout = findViewById<LinearLayout>(R.id.ll_cmtsLayout)
        if (showCommuity) {
            layout.visibility = View.VISIBLE
            setCmtsLayout(cmts)
        } else {
            layout.visibility = View.GONE
        }
    }

    private fun setCmtsLayout(cmts: MyTrainInfo.CommunityResult) {
        val width = ScreenUtils.getScreenWidth(context) / 3 - 20
        val height = width / 3 * 2
        val params = LinearLayout.LayoutParams(width, height)
        val ivCmts = findViewById<ImageView>(R.id.iv_cmts)
        ivCmts.layoutParams = params
        ivCmts.setImageResource(R.drawable.app_default)
        val tvPeriod = findViewById<TextView>(R.id.tv_cmtsPeriod)
        cmts.getmCommunityRelation()?.timePeriod?.let {
            val text = "${TimeUtil.getSlashDate(it.startTime)}至${TimeUtil.getSlashDate(it.endTime)}"
            tvPeriod.text = text
        }
        val tvHour = findViewById<TextView>(R.id.tv_cmtsStudyHour)
        val tvScore = findViewById<TextView>(R.id.tv_cmtsScore)
        cmts.getmCommunityRelation()?.let {
            tvHour.text = "${it.studyHours}学时"
            tvScore.text = "获得${cmts.score}/${it.score}积分"
        }
        val llCmts = findViewById<LinearLayout>(R.id.ll_cmts)
        llCmts.setOnClickListener { startActivity(Intent(context, CmtsMainActivity::class.java)) }
    }

    override fun setListener() {
        ivToggle.setOnClickListener(context)
        ivScan.setOnClickListener(context)
        ivMsg.setOnClickListener(context)
        loadFailView.setOnRetryListener { initData() }
        tvMyTrain.setOnClickListener(context)
        for (layout in llLayouts) {
            layout?.setOnClickListener(context)
        }
        courseAdapter.setOnItemClickListener { _, _, _, position ->
            val state = courseDatas[position].state
            if (state != null && state == "pass") {
                val entity = courseDatas[position]
                if (entity.getmTimePeriod()?.state != null && entity.getmTimePeriod().state == "未开始") {
                    showDialog("未开始")
                } else {
                    val courseId = entity.id
                    val courseTitle = entity.title
                    val intent = Intent(context, CourseTabActivity::class.java)
                    trainingTime?.state?.let {
                        if (it == "进行中") {
                            intent.putExtra("training", true)
                        }
                    }
                    trainingTime?.minutes?.let {
                        if (it > 0) {
                            intent.putExtra("training", true)
                        }
                    }
                    intent.putExtra("courseId", courseId)
                    intent.putExtra("courseTitle", courseTitle)
                    startActivity(intent)
                }
            } else {
                showDialog(state)
            }
        }
        wsAdapter.setOnItemClickListener({ _, _, _, position ->
            val entity = wsDatas[position]
            val intent = Intent(context, WSHomePageActivity::class.java).apply {
                putExtra("workshopId", entity.id)
                putExtra("point", entity.point)
                putExtra("workshopTitle", entity.title)
            }
            trainingTime?.state?.let {
                if (it == "进行中") {
                    intent.putExtra("training", true)
                }
            }
            trainingTime?.minutes?.let {
                if (it > 0) {
                    intent.putExtra("training", true)
                }
            }
            startActivity(intent)
        })
    }

    private fun showDialog(state: String?) {
        val dialog = MaterialDialog(context)
        val message: String = if (state != null && state == "未开始") {
            "课程尚未开放"
        } else if (state != null && state == "submit") {
            "您的选课正在审核中"
        } else if (state != null && state == "nopass") {
            "您的选课审核不通过"
        } else {
            "课程尚未开放"
        }
        dialog.setTitle("温馨提示")
        dialog.setMessage(message)
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor))
        dialog.setPositiveButton("我知道了", null)
        dialog.show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivToggle -> menu.toggle(true)
            R.id.iv_scan -> startActivityForResult(Intent(context, AppCaptureActivity::class.java), 1)
            R.id.iv_msg -> startActivity(Intent(context, AnnouncementActivity::class.java).apply { putExtra("relationId", ids) })
            R.id.tv_cmtsLearn -> startActivity(Intent(context, CmtsMainActivity::class.java))
            R.id.tv_myTrain -> setPopupView(tvMyTrain, myTrains)
            R.id.ll_course -> scrollToPosition(findViewById(R.id.tv_course))
            R.id.ll_workshop -> scrollToPosition(findViewById(R.id.tv_workshop))
            R.id.ll_community -> scrollToPosition(findViewById(R.id.ll_cmtsLayout))
            R.id.ll_cmts -> startActivity(Intent(context, CmtsMainActivity::class.java))
            R.id.tv_learn -> menu.toggle(true)  //侧滑菜单学习
            R.id.tv_teaching -> startActivity(Intent(context, CmtsMainActivity::class.java)) //侧滑菜单教研
            R.id.tv_workshop -> startActivity(Intent(context, WorkshopGroupActivity::class.java))  //侧滑菜单工作坊群
            R.id.tv_peer -> startActivity(Intent(context, PeerActivity::class.java)) //侧滑菜单同行
            R.id.tv_message -> startActivity(Intent(context, MessageActivity::class.java)) //侧滑菜单消息
            R.id.tv_consulting -> startActivity(Intent(context, EducationConsultActivity::class.java)) //侧滑菜单教务咨询
            R.id.tv_settings -> startActivity(Intent(context, SettingActivity::class.java)) //侧滑菜单设置
        }
    }

    /**
     * 滑动到指定位置
     */
    private fun scrollToPosition(view: View) {
        Handler().apply { postDelayed({ nsvContent.smoothScrollTo(0, view.bottom) }, 10) }
    }

    private var selectItem = 0

    private fun setPopupView(tv: TextView, list: List<MyTrainMobileEntity>) {
        val shouqi = ContextCompat.getDrawable(context, R.drawable.course_dictionary_shouqi)
        shouqi?.setBounds(0, 0, shouqi.minimumWidth, shouqi.minimumHeight)
        val zhankai = ContextCompat.getDrawable(context, R.drawable.course_dictionary_xiala)
        zhankai?.setBounds(0, 0, zhankai.minimumWidth, zhankai.minimumHeight)
        tv.setCompoundDrawables(null, null, shouqi, null)
        val recyclerView = RecyclerView(context)
        recyclerView.setBackgroundResource(R.drawable.dictionary_background)
        recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        val popupWindow = PopupWindow(recyclerView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val adapter = MyTrainAdapter(list, selectItem)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener { _, _, _, position ->
            selectItem = position
            trainId = list[selectItem].id
            trainingTime = list[selectItem].getmTrainingTime()
            adapter.setSelectItem(selectItem)
            popupWindow.dismiss()
            tv.text = list[selectItem].name
            trainId?.let {
                getTrainInfo(it)
            }
        }
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.isOutsideTouchable = true
        popupWindow.setOnDismissListener { tv.setCompoundDrawables(null, null, zhankai, null) }
        popupWindow.showAsDropDown(tv)
    }

    private inner class MyTrainAdapter(mDatas: List<MyTrainMobileEntity>, private var selectItem: Int = -1) : BaseArrayRecyclerAdapter<MyTrainMobileEntity>(mDatas) {

        fun setSelectItem(selectItem: Int) {
            this.selectItem = selectItem
            notifyDataSetChanged()
        }

        override fun bindView(viewtype: Int): Int {
            return R.layout.popupwindow_dictionary_item
        }

        override fun onBindHoder(holder: RecyclerHolder, entity: MyTrainMobileEntity, position: Int) {
            val tvDict = holder.obtainView<TextView>(R.id.tvDict)
            tvDict.text = entity.name
            val select = ContextCompat.getDrawable(context, R.drawable.train_item_selected)
            select?.setBounds(0, 0, select.minimumWidth, select.minimumHeight)
            if (selectItem == position) {
                tvDict.isPressed = true
                tvDict.setCompoundDrawables(null, null, select, null)
                tvDict.compoundDrawablePadding = PixelFormat.formatPxToDip(context, 10)
            } else {
                tvDict.isPressed = false
                tvDict.setCompoundDrawables(null, null, null, null)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 ->
                /* 处理二维码扫描结果*/
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringExtra(CodeUtils.RESULT_STRING)
                    parseCaptureResult(result)
                }
            2 -> data?.let {
                val avatar = it.getStringExtra("avatar")
                menu.menu?.findViewById<ImageView>(R.id.iv_userIco)?.let {
                    GlideImgManager.loadCircleImage(context.applicationContext, avatar, R.drawable.user_default, R.drawable.user_default, it)
                }
            }
        }
    }

    private fun parseCaptureResult(result: String) {
        if (result.contains("qtId") && result.contains("service")) {   //扫一扫登录
            try {
                val gson = Gson()
                val mCaptureResult = gson.fromJson(result, CaptureResult::class.java)
                val qtId = mCaptureResult.qtId
                val service = mCaptureResult.service
                val url = Constants.LOGIN_URL
                login(url, qtId, service)
            } catch (e: Exception) {
                toast(context, "解析二维码失败")
            }

        } else if ((result.startsWith("http") || result.startsWith("https")) && result.contains(Constants.REFERER)) {
            signedOn(result)
        } else {
            showMaterialDialog("提示", "非本应用规定的二维码")
        }
    }

    private fun login(url: String, qtId: String, service: String) {
        showLoadingDialog("登录验证")
        Flowable.fromCallable { OkHttpClientManager.getInstance().scanLogin(context, url, qtId, service) }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ isSuccessful ->
                    hideLoadingDialog()
                    if (isSuccessful) {
                        toast(context, "验证成功")
                    } else {
                        toast(context, "验证失败")
                    }
                }) {
                    hideLoadingDialog()
                    toast(context, "验证失败")
                }
    }

    private fun signedOn(url: String) {
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                hideTipDialog()
                if (response?.getResponseCode() != null && response.getResponseCode() == "00") {
                    toast(context, "签到成功")
                } else {
                    if (response?.getResponseMsg() != null)
                        toast(context, response.getResponseMsg())
                    else
                        toast(context, "签到失败")
                }
            }
        }))
    }

    private var mExitTime: Long = 0

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && menu.isMenuShowing) {
            menu.toggle(true)
            return false
        } else if (keyCode == KeyEvent.KEYCODE_BACK && !menu.isMenuShowing) {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                toast(context, "再按一次退出" + resources.getString(R.string.app_name))
                mExitTime = System.currentTimeMillis()
            } else {
                LegoApplication.exit()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun getVersion() {
        addSubscription(OkHttpClientManager.getAsyn(context, Constants.updateUrl, object : OkHttpClientManager.ResultCallback<VersionEntity>() {
            override fun onError(request: Request, e: Exception) {

            }

            override fun onResponse(entity: VersionEntity) {
                if (entity.versionCode > Common.getVersionCode(context)) {
                    updateTips(entity)
                }
            }
        }))
    }

    private fun updateTips(entity: VersionEntity) {
        val dialog = MaterialDialog(context)
        dialog.setMessage(entity.updateLog)
        dialog.setTitle("发现新版本")
        dialog.setNegativeButton("稍后下载", null)
        dialog.setPositiveButton("立即下载") { _, _ -> startService(entity) }
        dialog.show()
    }

    private fun startService(entity: VersionEntity) {
        val intent = Intent(context, VersionUpdateService::class.java)
        intent.putExtra("url", entity.downurl)
        intent.putExtra("versionName", entity.versionName)
        startService(intent)
        if (!Common.isNotificationEnabled(context)) {
            openTips()
        }
    }

    private fun openTips() {
        val dialog = MaterialDialog(context)
        dialog.setTitle("提示")
        dialog.setMessage("通知已关闭，是否允许应用推送通知？")
        dialog.setPositiveButton("开启") { _, _ -> Common.openSettings(context) }
        dialog.setNegativeButton("取消") { _, _ -> toast(context, "已进入后台更新") }
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(context, VersionUpdateService::class.java))
    }
}