package cn.vove7.energy_ring.energystyle

import android.view.View

interface EnergyStyle<T : View> {
    val displayView: T

    fun reloadAnimation()

    fun resumeAnimator()

    fun update(progress: Int? = null)

    fun width(): Int
    fun height(): Int

    fun onRemove()

    fun onHide()

    fun pauseAnimator()

    fun setColor(color: Int) {}
}