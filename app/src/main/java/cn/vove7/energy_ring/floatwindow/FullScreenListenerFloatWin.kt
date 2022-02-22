package cn.vove7.energy_ring.floatwindow

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import cn.vove7.energy_ring.App
import cn.vove7.energy_ring.BuildConfig
import cn.vove7.energy_ring.R
import cn.vove7.energy_ring.service.AccService
import cn.vove7.energy_ring.util.screenHeight
import cn.vove7.energy_ring.util.screenWidth

/**
 * # FullScreenListenerFloatWin
 *
 * @author Vove
 * 2020/5/9
 */
object FullScreenListenerFloatWin {

    const val TAG = "FullScreenListenerFloatWin"

    var isFullScreen = false
    var lastRotation = -1

    private val view by lazy {
        @SuppressLint("AppCompatCustomView")
        object : TextView(App.INS) {
            init {
                if (BuildConfig.DEBUG) {
                    setBackgroundColor(R.color.colorPrimary)
                    setTextColor(Color.WHITE)
                    text = FloatRingWindow.debugInfo
                }
                setOnSystemUiVisibilityChangeListener {
                    Log.d(TAG, "OnSystemUiVisibility: $it")
                    val _isFullScreen = it == 6 || it == 4
                    postDelayed({
//                        if (lastRotation != FloatRingWindow.currentRotation) {
                        isFullScreen = _isFullScreen
                        lastRotation = FloatRingWindow.currentRotation
                        update()
//                        }
                    }, 50)
                }
            }
        }
    }

    private val layoutParams: WindowManager.LayoutParams
        get() = WindowManager.LayoutParams(
            -2, -2,
            if (FloatRingWindow.currentRotation / 2 == 0) screenWidth / 2
            else screenHeight / 2, 0,
            when {
//                AccService.hasOpend -> WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else -> WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.RGBA_8888
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            gravity = Gravity.TOP or Gravity.START
        }

    var showing = false

    private fun update() {
        Log.d("FullScreenListenerFloatWin", "update: ")
        FloatRingWindow.reload()
        updateDebugInfo()
    }

    fun updateDebugInfo() {
        kotlin.runCatching {
            view.text = FloatRingWindow.debugInfo
//                wm.updateViewLayout(view, layoutParams)
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun start() {
        if (showing) {
            return
        }
        Log.d(TAG, "start: full show")
        showing = true
        wm.addView(view, layoutParams)
    }

    private val wm: WindowManager
        get() = AccService.wm ?: App.windowsManager

    fun reload() {
        try {
            showing = false
            wm.removeViewImmediate(view)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        start()
    }

}