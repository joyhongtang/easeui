package com.hyphenate.chatuidemo.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDelegate
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.hyphenate.chatuidemo.Constant
import com.hyphenate.chatuidemo.conference.utils.ActivityPageManager
import com.hyphenate.chatuidemo.entity.MessageEvent
import com.hyphenate.chatuidemo.utils.ImageTool
import com.hyphenate.easeui.R
import com.hyphenate.easeui.ui.EaseBaseActivity
import com.superrtc.mediamanager.ScreenCaptureManager
import kotlinx.android.synthetic.main.activity_base.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream

@SuppressLint("Registered")
open abstract class BaseActivity : EaseBaseActivity(), View.OnClickListener {
    public var showTopBar = true
    private var isScreen = false
    private var screenImagePath = ""
    val ACTION = "action"

    //androidstudio 注释模板
    //截屏获取到的图片错误次数统计
    private var requestBitmapErrorCode = 0
    val HIDE_SUSPENDED_BALL = "HIDESuspendedBall"

    open fun initConfig() {}

    abstract fun initLayout(): Int

    abstract fun initData()

    abstract fun initListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initConfig()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(initLayout())
        initData()
        initListener()
        EventBus.getDefault().register(this)
        ActivityPageManager.getInstance().addActivity(this)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(R.layout.activity_base)
        if (Constant.isDualScreen){
            val layoutParams = cl_base.layoutParams as FrameLayout.LayoutParams
            layoutParams.marginEnd = 195
            cl_base.setPadding(0, 0, 18, 0)
        }
        LayoutInflater.from(this).inflate(layoutResID, cl_content_base, true)
        if (showTopBar) {
            cl_navigation_base.visibility = View.VISIBLE
            iv_back_base.setOnClickListener(this)
            iv_more_base.setOnClickListener(this)
            iv_check_base.setOnClickListener(this)
            iv_delete_base.setOnClickListener(this)
            iv_copy_base.visibility =  View.VISIBLE
            iv_copy_base.setOnClickListener(this)
        } else {
            cl_navigation_base.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
        ActivityPageManager.getInstance().removeActivity(this)
    }

    /**
     * @Title: CommunicationByPhoneActivity
     * @Description: 调用系统截屏功能进行屏幕截取
     * @author TJ
     * @date 2019/12/25
     * @version V1.0
     */
    open fun startScreenCapture() {
        val intent = Intent()
        intent.setClassName(packageName, "com.idwell.cloudframe.service.GlobalService")
        intent.putExtra(ACTION, HIDE_SUSPENDED_BALL)
        ActivityPageManager.getInstance().currentActivity()?.startService(intent)
        val dView = window.decorView
        dView.isDrawingCacheEnabled = true
        dView.buildDrawingCache()
        val bitmap: Bitmap = Bitmap.createBitmap(dView.drawingCache)
        if (bitmap != null) {
            try {
                // 获取内置SD卡路径
                val sdCardPath: String = Environment.getExternalStorageDirectory().getPath()
                // 图片文件路径
                val filePath = sdCardPath + File.separator + "screenshot.png"
                val file = File(filePath)
                val os = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.flush()
                os.close()
                screenImagePath = ImageTool.saveBitmap(
                    Constant.application,
                    bitmap
                )
                val allowSize = 10 * 1024
                val imageTemp = File(screenImagePath)
                val length = imageTemp.length()
                if (length < allowSize) {
                    imageTemp.delete()
//                    startScreenCapture()
                }
            } catch (e: Exception) {
            }
            if (bitmap != null) {
                startScreenShot(screenImagePath)
            }
        }
    }

    val DISPLAY_SUSPENDED_BALL = "displaySuspendedBall"

    /**
     * filepath
     */
    open fun startScreenShot(filePath: String?) {
        if (SPUtils.getInstance().getBoolean(DISPLAY_SUSPENDED_BALL, true)) {
            val intent = Intent()
            intent.setClassName(packageName, "com.idwell.cloudframe.service.GlobalService")
            intent.putExtra(ACTION, DISPLAY_SUSPENDED_BALL)
            ActivityPageManager.getInstance().currentActivity()?.startService(intent)
        }
        val i = Intent()
        i.setClassName("com.idwell.cloudframe", "com.idwell.cloudframe.ui.ScreenShootActivity")
        i.putExtra("filePath", filePath)
        Constant.application.startActivity(i)

    }


    /**
     * @Title: CommunicationByPhoneActivity
     * @Description: 调用系统截屏功能进行屏幕截取
     * @author TJ
     * @date 2019/12/25
     * @version V1.0
     */
    open fun startScreenCapture2() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requestBitmapErrorCode++
            if (requestBitmapErrorCode > 2) {
                requestBitmapErrorCode = 0
            }
            if (ScreenCaptureManager.getInstance().state == ScreenCaptureManager.State.IDLE) {
                ScreenCaptureManager.getInstance()
                    .init(ActivityPageManager.getInstance().currentActivity())
            }
            intent.setClassName(packageName, "com.idwell.cloudframe.service.GlobalService")
            intent.putExtra(ACTION, HIDE_SUSPENDED_BALL)
            ActivityPageManager.getInstance().currentActivity()?.startService(intent)

            screenImagePath = ""
            isScreen = false
            ScreenCaptureManager.getInstance()
                .setScreenCaptureCallback { bitmap ->
                    if (!isScreen) {
                        isScreen = true
                        screenImagePath = ImageTool.saveBitmap(
                            Constant.application,
                            bitmap
                        )
                        val imageTemp = File(screenImagePath)
                        val length = imageTemp.length()
                        //8*1024 = 1kb
                        val allowSize = 10 * 1024
                        if (length < allowSize) {
                            imageTemp.delete()
                            startScreenCapture2()
                        } else {
                            //pause camera when surface unavaliable
                            startScreenShot(screenImagePath)
                            requestBitmapErrorCode = 0
                        }
                    }
                }
            //            }
        } else {
            ToastUtils.showLong("系统版本过低，不能截屏")
        }
    }
    open fun backEvent(){
        finish()
    }
    override fun onClick(v: View?) {
        when (v) {
            iv_back_base ->
                backEvent()
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: MessageEvent) {
    }

}