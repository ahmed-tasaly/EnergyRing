package cn.vove7.energy_ring.floatwindow

import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import android.widget.FrameLayout
import cn.vove7.energy_ring.App
import cn.vove7.energy_ring.BuildConfig
import cn.vove7.energy_ring.energystyle.DoubleRingStyle
import cn.vove7.energy_ring.energystyle.EnergyStyle
import cn.vove7.energy_ring.energystyle.PillStyle
import cn.vove7.energy_ring.energystyle.RingStyle
import cn.vove7.energy_ring.model.ShapeType
import cn.vove7.energy_ring.service.AccService
import cn.vove7.energy_ring.util.*
import java.lang.Thread.sleep
import kotlin.concurrent.thread


/**
 * # FloatRingWindow
 *
 * @author Vove
 * 2020/5/8
 */
object FloatRingWindow {
    val TAG = this::class.java.simpleName

    private val hasPermission
        get() = Settings.canDrawOverlays(App.INS)

    private val displayEnergyStyleDelegate = weakLazy {
        buildEnergyStyle()
    }

    fun buildEnergyStyle(): EnergyStyle<*> = when (Config.energyType) {
        ShapeType.RING -> RingStyle()
        ShapeType.DOUBLE_RING -> DoubleRingStyle()
        ShapeType.PILL -> PillStyle()
    }

    private val displayEnergyStyle by displayEnergyStyleDelegate

    private val wm: WindowManager
        get() = AccService.wm ?: App.windowsManager

    fun start() {
        if (hasPermission) {
            if (canShow()) {
                showInternal()
            }
        } else {
            openFloatPermission()
            thread {
                val s = System.currentTimeMillis()
                while (!hasPermission && System.currentTimeMillis() - s < 60000) {
                    Log.d("Debug :", "wait p...")
                    sleep(100)
                }
                Log.d("Debug :", "hasPermission")
                if (hasPermission) {
                    Handler(Looper.getMainLooper()).post {
                        showInternal()
                    }
                }
            }
        }
    }

    val currentRotation get() = wm.defaultDisplay.rotation

    fun getCenterPoint(): Point {
        val sh = screenHeight
        val sw = screenWidth
        Log.d(TAG, "screen h: $sh w: $sw")
        val xy = when (currentRotation) {
            Surface.ROTATION_90 -> Config.posY to screenWidth - Config.posX
            Surface.ROTATION_180 -> screenWidth - Config.posX to screenHeight - Config.posY
            Surface.ROTATION_270 -> screenHeight - Config.posY to Config.posX
            else -> Config.posX to Config.posY
        }
        return Point((xy.first - displayEnergyStyle.width().toFloat() / 2).toInt(),
            (xy.second - displayEnergyStyle.height().toFloat() / 2).toInt())
    }

    var isShowing = false
    private val layoutParams: WindowManager.LayoutParams
        get() = run {
            val p = getCenterPoint()
            Log.d(TAG, "pos: $p")
            WindowManager.LayoutParams(
                -2, -2,
                p.x, p.y,
                when {
                    AccService.hasOpend -> WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    else -> WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.RGBA_8888
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    this.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        }

    private val bodyView by lazy {
        FrameLayout(App.INS).apply {
            lastChange = SystemClock.elapsedRealtime()
            addView(displayEnergyStyle.displayView, -2, -2)
            transform()
        }
    }

    fun onShapeTypeChanged() {
        forceRefresh()
        show()
    }

    private fun View.transform() = apply {
        rotation = (currentRotation * 90).toFloat()
    }

    fun forceRefresh() {
        lastChange = SystemClock.elapsedRealtime()
        displayEnergyStyle.onRemove()
        displayEnergyStyleDelegate.clearWeakValue()
        bodyView.apply {
            removeAllViews()
            addView(displayEnergyStyle.displayView, -2, -2)
            transform()
        }
        displayEnergyStyle.update(batteryLevel)
        displayEnergyStyle.reloadAnimation()
    }

    private fun showInternal() {
        isShowing = true
        FullScreenListenerFloatWin.start()
        try {
            bodyView.visibility = View.VISIBLE
            displayEnergyStyle.update(batteryLevel)
            if (bodyView.tag != true) {
                wm.addView(bodyView, layoutParams)
                bodyView.tag = true
            }
            reloadAnimation()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun reload() {
        Log.d(TAG, "reload: ")
        if (isShowing) {
            if(canShow()) {
                update()
                return
            }
            close()
        }
        show()
    }

    @Synchronized
    fun close() {
        if(!isShowing) return
        try {
            bodyView.tag = false
            isShowing = false
            wm.removeViewImmediate(bodyView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun update(p: Int? = null) {
        if (!isShowing) {
            return
        }
        wm.updateViewLayout(bodyView, layoutParams)
        checkValid() ?: return
        displayEnergyStyle.update(p)
        bodyView.requestLayout()
        bodyView.transform()
    }

    private const val periodRefreshView = 5 * 60 * 1000

    private var lastChange = 0L

    fun checkValid(): Unit? {
        if (SystemClock.elapsedRealtime() - lastChange > periodRefreshView) {
            onShapeTypeChanged()
            return null
        }
        return Unit
    }

    fun onCharging() {
        reloadAnimation()
    }


    fun reloadAnimation() {
        displayEnergyStyle.reloadAnimation()
    }

    fun onDisCharging() {
        reloadAnimation()
    }

    @Synchronized
    fun hide() {
        if (!isShowing) {
            return
        }
        bodyView.visibility = View.INVISIBLE
        isShowing = false
        displayEnergyStyle.onHide()
    }

    fun pauseAnimator() {
        displayEnergyStyle.pauseAnimator()
    }

    fun resumeAnimator() {
        displayEnergyStyle.resumeAnimator()
    }

    var debugInfo: String = ""

    fun canShow(): Boolean {
        if (!hasPermission) {
            return false
        }

        Log.d(TAG, "currentRotation: $currentRotation")
        Log.d(TAG, "isPowerSaveMode: ${App.powerManager.isPowerSaveMode}")
        Log.d(TAG, "isFullScreen: ${FullScreenListenerFloatWin.isFullScreen}")

        val cond1 = !Config.autoHideRotate || currentRotation == 0
        val cond2 = !Config.autoHideFullscreen || !FullScreenListenerFloatWin.isFullScreen
        val cond3 = !Config.powerSaveHide || !App.powerManager.isPowerSaveMode

        if (BuildConfig.DEBUG) {
            debugInfo = """旋转: $cond1 全屏: $cond2 省电: $cond3
                |full:${FullScreenListenerFloatWin.isFullScreen}
            |pos: ${getCenterPoint()}
            |rotation: $currentRotation
        """.trimMargin()
            Log.d("Debug :", debugInfo)
        }

        return cond1 && cond2 && cond3
    }

    @Synchronized
    fun show() {
        if (!canShow()) {
            hide()
            return
        }
        showInternal()
    }

}