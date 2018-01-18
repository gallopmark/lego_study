package com.haoyu.app.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.github.barteksc.pdfviewer.PDFView
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.entity.AppActivityViewResult
import com.haoyu.app.fragment.CoursewareEditorFragment
import com.haoyu.app.fragment.CoursewareLinkFragment
import com.haoyu.app.lego.student.R
import com.haoyu.app.utils.Common
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.MediaFile
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.AppToolBar
import com.haoyu.app.view.RoundRectProgressBar
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.util.FileDownloadUtils
import okhttp3.Request
import java.io.File
import java.util.*

/**
 * 创建日期：2018/1/18.
 * 描述:课程学习教学课件预览
 * 作者:xiaoma
 */
class CoursewareViewerActivity : BaseActivity() {
    private lateinit var context: CoursewareViewerActivity
    private lateinit var toolBar: AppToolBar
    private lateinit var llTips: LinearLayout
    private lateinit var tvTopTips: TextView
    private lateinit var tvClose: TextView
    private lateinit var container: FrameLayout
    private lateinit var tvNonsupport: TextView   //无法预览课件，提示到网站参与
    /*当课件类型是pdf文件时*/
    private lateinit var llFileInfo: LinearLayout
    private lateinit var tvFileSize: TextView   //文件大小
    private lateinit var btDownload: Button  //下载、继续下载、打开文件按钮
    private lateinit var llDownloadInfo: LinearLayout    //显示下载信息
    private lateinit var tvDownloadInfo: TextView   //显示文件下载大小和总大小
    private lateinit var downloadBar: RoundRectProgressBar  //显示文件下载进度
    private val fileRoot = Constants.coursewareDir
    /** */
    private var running: Boolean = false
    private var viewNum: Int = 0
    private var needViewNum: Int = 0
    private var interval: Int = 0    //已观看次数，要求观看次数，延时访问时间
    private var isSupport = false
    private var isRead = false

    override fun setLayoutResID(): Int {
        return R.layout.activity_courseware_viewer
    }

    @Suppress("DEPRECATION")
    override fun initView() {
        context = this
        running = intent.getBooleanExtra("running", false)
        val title = intent.getStringExtra("title")
        viewNum = intent.getIntExtra("viewNum", 0)
        needViewNum = intent.getIntExtra("needViewNum", 0)
        interval = intent.getIntExtra("interval", 12)
        val type = intent.getStringExtra("type")
        findViews()
        if (title != null) {
            val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(title)
            }
            toolBar.setTitle_text(spanned)
        } else {
            toolBar.setTitle_text("教学课件")
        }
        showTips()
        if (type != null) {
            if (type == "file" && intent.getStringExtra("url") != null) {//pdf文件
                isSupport = true
                val url = intent.getStringExtra("url")
                val fileName = Common.getFileName(url)
                FileDownloader.setup(context)
                if (!isFileExists(fileName)) {
                    beginDownload(url, fileName)
                }
            } else if (type == "link" && intent.getStringExtra("url") != null) { //外链
                isSupport = true
                val url = intent.getStringExtra("url")
                setLinkFragment(url)
            } else if (type == "editor") { //文本编辑器
                isSupport = true
                val editor = intent.getStringExtra("editor")
                setEditorF(editor)
            } else {
                isSupport = false
                tvNonsupport.visibility = View.VISIBLE
            }
        } else {
            isSupport = false
            tvNonsupport.visibility = View.VISIBLE
        }
    }

    private fun findViews() {
        toolBar = findViewById(R.id.toolBar)
        llTips = findViewById(R.id.ll_tips)
        tvTopTips = findViewById(R.id.tv_topTips)
        tvClose = findViewById(R.id.tv_close)
        container = findViewById(R.id.container)
        tvNonsupport = findViewById(R.id.tv_nonsupport)
        llFileInfo = findViewById(R.id.ll_fileInfo)
        tvFileSize = findViewById(R.id.tv_fileSize)
        btDownload = findViewById(R.id.bt_download)
        llDownloadInfo = findViewById(R.id.ll_downloadInfo)
        tvDownloadInfo = findViewById(R.id.tv_downloadInfo)
        downloadBar = findViewById(R.id.downloadBar)
    }

    private fun showTips() {
        toolBar.setShow_right_button(false)
        val message = "观看文档即可完成活动，要求观看文档 $needViewNum 次/您已观看 $viewNum 次。"
        val ssb = SpannableString(message)
        var start = message.lastIndexOf("档") + 1
        var end = message.indexOf("次")
        ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        start = message.lastIndexOf("看") + 1
        end = message.lastIndexOf("次")
        ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvTopTips.text = ssb
    }

    private fun isFileExists(fileName: String): Boolean {
        val filePath = FileDownloadUtils.getTargetFilePath(fileRoot, true, fileName)
        if (filePath != null && File(filePath).exists()) {
            generateFile(filePath)
            return true
        }
        return false
    }

    private fun generateFile(filePath: String) {
        if (MediaFile.isPdfFileType(filePath)) {
            openPdfFile(filePath)
        } else {
            llFileInfo.visibility = View.GONE
            tvNonsupport.visibility = View.VISIBLE
        }
    }

    /*打开pdf文件*/
    private fun openPdfFile(filePath: String) {
        container.removeAllViews()
        val view = LayoutInflater.from(context).inflate(R.layout.layout_pdfviewer, container, false)
        val pdfView = view.findViewById<PDFView>(R.id.pdfView)
        val tvPage = view.findViewById<TextView>(R.id.tv_page)
        pdfView.fromFile(File(filePath))
                .swipeHorizontal(true)
                .defaultPage(0)
                .enableDoubletap(true)
                .enableSwipe(true)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .scrollHandle(null)
                .linkHandler(null)
                .enableAntialiasing(true) //  .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .onLoad {
                    if (!isRead && pdfView.windowToken != null) {  //如果activity已经销毁则不显示指示框
                        showGestureDialog()
                    }
                }
                .onPageChange { page, pageCount -> tvPage.text = (page + 1).toString() + "/" + pageCount }
                .load()
        container.addView(view)
    }

    private fun showGestureDialog() {
        val dialog = AlertDialog.Builder(context, R.style.GestureDialog).create()
        dialog.show()
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_gesture_tips, FrameLayout(context), false)
        val tvTips = view.findViewById<TextView>(R.id.tv_tips)
        val ivCenter = view.findViewById<ImageView>(R.id.iv_center)
        tvTips.text = "手势可放大缩小"
        ivCenter.setImageResource(R.drawable.gesture_big)
        view.findViewById<View>(R.id.bt_know).setOnClickListener {
            isRead = true
            dialog.dismiss()
        }
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.setOnDismissListener { isRead = true }
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        dialog.setContentView(view, params)
    }

    private fun beginDownload(url: String, fileName: String) {
        showFileContent(url, fileName)
        val path = fileRoot + File.separator + fileName
        FileDownloader.getImpl().create(url)
                .addHeader("Referer", Constants.REFERER)
                .setPath(path, false)
                .setListener(downloadListener).start()
    }

    private fun showFileContent(url: String, fileName: String) {
        llFileInfo.visibility = View.VISIBLE
        val ivType = findViewById<ImageView>(R.id.iv_type)
        val tvFileName = findViewById<TextView>(R.id.tv_fileName)
        val ivPause = findViewById<ImageView>(R.id.iv_pause)
        Common.setFileType(url, ivType)
        tvFileName.text = Common.getFileName(url)
        btDownload.setOnClickListener {
            btDownload.visibility = View.GONE
            llDownloadInfo.visibility = View.VISIBLE
            beginDownload(url, fileName)
        }
        ivPause.setOnClickListener({ FileDownloader.getImpl().pause(downloadListener) })
    }

    private val downloadListener = object : FileDownloadListener() {
        override fun pending(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
        }

        override fun progress(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
            llDownloadInfo.visibility = View.VISIBLE
            tvFileSize.text = Common.FormetFileSize(totalBytes.toLong())
            val downloadSize = Common.FormetFileSize(soFarBytes.toLong())
            val fileSize = Common.FormetFileSize(totalBytes.toLong())
            tvDownloadInfo.text = "下载中...($downloadSize/$fileSize)"
            downloadBar.max = totalBytes
            downloadBar.progress = soFarBytes
        }

        override fun completed(task: BaseDownloadTask) {
            if (task.targetFilePath != null && File(task.targetFilePath).exists()) {
                generateFile(task.targetFilePath)
            }
        }

        override fun paused(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
            btDownload.visibility = View.VISIBLE
            btDownload.text = "继续下载"
            llDownloadInfo.visibility = View.GONE
        }

        override fun error(task: BaseDownloadTask, e: Throwable) {
            toastFullScreen("文件下载出错", false)
            btDownload.visibility = View.VISIBLE
            btDownload.text = "继续下载"
            llDownloadInfo.visibility = View.GONE
        }

        override fun warn(task: BaseDownloadTask) {

        }
    }

    private fun setLinkFragment(url: String) {
        val fragment = CoursewareLinkFragment()
        val bundle = Bundle()
        bundle.putString("url", url)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    private fun setEditorF(editor: String) {
        val fragment = CoursewareEditorFragment()
        val bundle = Bundle()
        bundle.putString("editor", editor)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    override fun setListener() {
        toolBar.setOnTitleClickListener(object : AppToolBar.TitleOnClickListener {
            override fun onLeftClick(view: View) {
                finish()
            }

            override fun onRightClick(view: View) {
                showTopView()
            }
        })
        tvClose.setOnClickListener({
            tvClose.isEnabled = false
            hideTopView()
        })
    }

    private fun showTopView() {
        val animation = AnimationUtils.loadAnimation(context, R.anim.scale_show)
        animation?.let {
            llTips.startAnimation(it)
            it.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    llTips.visibility = View.VISIBLE
                    toolBar.setShow_right_button(false)
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        }
    }

    private fun hideTopView() {
        val animation = AnimationUtils.loadAnimation(context, R.anim.scale_hide)
        animation?.let {
            llTips.startAnimation(it)
            it.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    llTips.visibility = View.GONE
                    toolBar.setShow_right_button(true)
                    tvClose.isEnabled = true
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        }
    }

    override fun initData() {
        if (running && isSupport) {
            Handler().apply {
                postDelayed({ updateAttempt() }, (interval * 1000).toLong())
            }
        }
    }

    /*** 更新课件观看次数*/
    private fun updateAttempt() {
        val activityId = intent.getStringExtra("activityId")
        val mTextInfoUserId = intent.getStringExtra("mTextInfoUserId")
        val url = Constants.OUTRT_NET + "/" + activityId + "/study/m/textInfo/user/updateAttempt"
        val map = HashMap<String, String>()
        map["_method"] = "put"
        map["id"] = mTextInfoUserId
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onError(request: Request?, e: Exception?) {
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                response?.responseCode?.equals("00").let {
                    getActivityInfo(activityId)
                }
            }
        }, map))
    }

    private fun getActivityInfo(activityId: String) {
        val url = Constants.OUTRT_NET + "/" + activityId + "/study/m/activity/ncts/" + activityId + "/view"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<AppActivityViewResult>() {
            override fun onError(request: Request?, e: java.lang.Exception?) {}

            override fun onResponse(response: AppActivityViewResult?) {
                response?.responseData?.let {
                    it.getmTextInfoUser()?.let {
                        viewNum = it.viewNum
                    }
                    showTips()
                    showTopView()
                }
                response?.responseData?.getmActivityResult()?.getmActivity()?.let {
                    val activity = it
                    val intent = Intent()
                    intent.putExtra("activity", activity)
                    setResult(Activity.RESULT_OK, intent)
                }
            }
        }))
    }

    override fun onDestroy() {
        super.onDestroy()
        FileDownloader.getImpl().pause(downloadListener)
    }
}