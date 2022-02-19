package cn.vove7.energy_ring.energystyle

import android.widget.FrameLayout
import cn.vove7.energy_ring.App
import cn.vove7.energy_ring.ui.view.RingView
import cn.vove7.energy_ring.util.Config
import cn.vove7.energy_ring.util.getColorByRange
import cn.vove7.energy_ring.util.weakLazy

/**
 * # RingStyle
 *
 * @author Vove
 * 2020/5/11
 */
class RingStyle : RotateAnimatorSupporter<RingView>() {

    private val ringViewDelegate = weakLazy {
        RingView(App.INS).apply {
            layoutParams = FrameLayout.LayoutParams(Config.size, Config.size)
        }
    }

    override val displayView: RingView by ringViewDelegate

    override fun onAnimatorUpdate(rotateValue: Float) {
        displayView.rotation = rotateValue
    }

    override fun setColor(color: Int) {
        displayView.apply {
            mainColor = color
        }
    }

    override fun height(): Int = Config.size
    override fun width(): Int = Config.size

    override fun update(progress: Int?) {
        displayView.apply {
            strokeWidthF = Config.strokeWidthF
            if (progress != null) {
                this.progress = progress
            }
            if (Config.colorMode == 2) {
                doughnutColors = Config.colorsDischarging
            } else {
                mainColor = getColorByRange(this.progressf, Config.colorsDischarging, Config.colorsCharging)
            }
            bgColor = Config.ringBgColor
            reSize(Config.size)
            requestLayout()
        }
    }

    override fun onRemove() {
        super.onRemove()
        ringViewDelegate.clearWeakValue()
    }
}