package com.haoyu.app.activity

import android.graphics.Bitmap
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.gson.Gson
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.compressor.Compressor
import com.haoyu.app.entity.DiscussResult
import com.haoyu.app.entity.MobileUser
import com.haoyu.app.entity.MultiItemEntity
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.student.R
import com.haoyu.app.pickerlib.*
import com.haoyu.app.rxBus.MessageEvent
import com.haoyu.app.rxBus.RxBus
import com.haoyu.app.utils.Action
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.utils.PixelFormat
import com.haoyu.app.view.AppToolBar
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Request
import java.io.File
import java.util.*

/**
 * 创建日期：2018/1/19.
 * 描述:创建研说
 * 作者:xiaoma
 */
class CmtsSaysEditActivity : BaseActivity() {
    private val context = this
    private lateinit var toolBar: AppToolBar
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var recyclerView: RecyclerView
    private val icoItems = ArrayList<MultiItemEntity>()
    private lateinit var adapter: GridAdapter
    private lateinit var cameraItem: CameraItem

    private class CameraItem : MultiItemEntity {
        override fun getItemType(): Int {
            return 2
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.activity_cmts_createss
    }

    override fun initView() {
        toolBar = findViewById(R.id.toolBar)
        etTitle = findViewById(R.id.et_title)
        etContent = findViewById(R.id.et_content)
        recyclerView = findViewById(R.id.recyclerView)
        val layoutManager = GridLayoutManager(this, 3)
        layoutManager.orientation = GridLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(GridSpacingItemDecoration(3, PixelFormat.dp2px(context, 2f), false))
        cameraItem = CameraItem()
        icoItems.add(cameraItem)
        adapter = GridAdapter(icoItems)
        recyclerView.adapter = adapter
    }

    override fun setListener() {
        toolBar.setOnTitleClickListener(object : AppToolBar.TitleOnClickListener {
            override fun onLeftClick(view: View) {
                finish()
            }

            override fun onRightClick(view: View) {
                val title = etTitle.text.toString().trim()
                val content = etContent.text.toString().trim()
                if (title.isEmpty()) {
                    showMaterialDialog("提示", "请输入标题")
                } else if (content.isEmpty()) {
                    showMaterialDialog("提示", "请输入描述内容")
                } else {
                    upload(title, content)
                }
            }
        })
        val ivArrow = findViewById<AppCompatImageView>(R.id.ivArrow)
        ivArrow.setOnClickListener({
            etTitle.setSelection(etTitle.text.length)
        })
    }

    private fun pickerPicture() {
        if (icoItems.size >= 10)
            toast(context, "您最多可选9张图")
        else {
            val limit = 10 - icoItems.size
            val option = MediaOption.Builder()
                    .setSelectType(MediaOption.TYPE_IMAGE)
                    .isMultiMode(true)
                    .setShowCamera(false)
                    .setSelectLimit(limit)
                    .setStyle(CropImageView.Style.CIRCLE)
                    .build()
            MediaPicker.getInstance().init(option).selectMedia(context, object : MediaPicker.onSelectMediaCallBack() {
                override fun onSelected(mSelects: List<MediaItem>) {
                    showTipDialog()
                    Flowable.fromArray(mSelects).map { _ ->
                        val items = ArrayList<MediaItem>()
                        for (i in mSelects.indices) {
                            val item = mSelects[i]
                            val file = File(item.path)
                            val target = Compressor(context).setMaxWidth(400).setMaxHeight(320).setQuality(100)
                                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                    .setDestinationDirectoryPath(Constants.compressor)
                                    .compressToFile(file)
                            item.path = target.absolutePath
                            items.add(item)
                        }
                        items
                    }.subscribe { mediaItems ->
                        hideTipDialog()
                        icoItems.remove(cameraItem)
                        icoItems.addAll(mediaItems)
                        if (icoItems.size < 9) {
                            icoItems.add(cameraItem)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            })
        }
    }

    private fun upload(title: String, content: String) {
        if (icoItems.size > 2) {
            showTipDialog()
            addSubscription(Flowable.fromCallable {
                val list = ArrayList<String>()
                for (i in 0 until icoItems.size) {
                    try {
                        if (icoItems[i].itemType == 1) {
                            val path = (icoItems[i] as MediaItem).path
                            uploadSrc(path)?.responseData?.let {
                                list.add(it.toString())
                            }
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }
                list
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ list ->
                hideTipDialog()
                val sb = StringBuilder()
                sb.append("<p>$content</p>")
                for (src in list) {
                    sb.append(src)
                }
                commit(title, sb.toString())
            }, { hideTipDialog() }))
        } else {
            commit(title, content)
        }
    }

    @Throws(Exception::class)
    private fun uploadSrc(path: String): BaseResponseResult<*>? {
        val url = "${Constants.OUTRT_NET}/m/file/uploadFileUeditor"
        val file = File(path)
        val json = OkHttpClientManager.post(context, url, file, file.name) { _, _, _, _ -> }
        return Gson().fromJson(json, BaseResponseResult::class.java)
    }

    /*创建研说*/
    private fun commit(title: String, content: String) {
        val url = "${Constants.OUTRT_NET}/m/discussion/cmts"
        val map = HashMap<String, String>()
        map["discussionRelations[0].relation.id"] = "cmts"
        map["discussionRelations[0].relation.type"] = "discussion"
        map["title"] = title
        map["content"] = content
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<DiscussResult>() {
            override fun onBefore(request: Request?) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: DiscussResult?) {
                hideTipDialog()
                response?.responseData?.let {
                    if (it.creator == null) {
                        it.creator = MobileUser().apply {
                            id = context.userId
                            avatar = context.avatar
                            realName = context.realName
                        }
                    } else {
                        it.creator.let {
                            if (it.id == null || it.id.toLowerCase() == "null") it.id = userId
                            if (it.avatar == null || it.avatar.toLowerCase() == "null") it.avatar = avatar
                            if (it.realName == null || it.realName.toLowerCase() == "null") it.realName = realName
                        }
                    }
                    val event = MessageEvent()
                    event.action = Action.CREATE_STUDY_SAYS
                    event.obj = it
                    RxBus.getDefault().post(event)
                    toastFullScreen("发表成功", true)
                    finish()
                }
            }
        }, map))
    }

    private inner class GridAdapter(mDatas: List<MultiItemEntity>) : BaseArrayRecyclerAdapter<MultiItemEntity>(mDatas) {
        private var imageSize = 0

        init {
            val screenWidth = resources.displayMetrics.widthPixels
            val densityDpi = resources.displayMetrics.densityDpi
            var cols = screenWidth / densityDpi
            cols = if (cols < 3) 3 else cols
            val columnSpace = 4 * context.resources.displayMetrics.density
            imageSize = ((screenWidth - columnSpace * (cols - 1)) / cols).toInt()
        }

        override fun getItemViewType(position: Int): Int {
            return mDatas[position].itemType
        }

        override fun bindView(viewtype: Int): Int {
            return if (viewtype == 1) R.layout.grid_image_item else R.layout.grid_image_add
        }

        override fun onBindHoder(holder: RecyclerHolder, entity: MultiItemEntity, position: Int) {
            if (holder.itemViewType == 1) {
                holder.itemView.layoutParams = FrameLayout.LayoutParams(imageSize, imageSize)
                val item = entity as MediaItem
                val path = item.path
                val ivImage = holder.obtainView<ImageView>(R.id.ivImage)
                val ivCancel = holder.obtainView<ImageView>(R.id.ivCancel)
                GlideImgManager.loadImage(context, path, R.drawable.app_default, R.drawable.app_default, ivImage)
                ivCancel.setOnClickListener({
                    mDatas.removeAt(position)
                    if (mDatas.size <= 9) {
                        mDatas.add(cameraItem)
                    }
                    notifyDataSetChanged()
                })
            } else {
                holder.itemView.layoutParams = LinearLayout.LayoutParams(imageSize, imageSize)
                holder.itemView.setOnClickListener({ pickerPicture() })
            }
        }

    }

    override fun onDestroy() {
        val file = File(Constants.compressor)
        if (file.exists())
            file.delete()
        super.onDestroy()
    }
}