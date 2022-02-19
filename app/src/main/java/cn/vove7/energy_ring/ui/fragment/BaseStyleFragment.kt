package cn.vove7.energy_ring.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import cn.vove7.energy_ring.R
import cn.vove7.energy_ring.floatwindow.FloatRingWindow
import cn.vove7.energy_ring.listener.PowerEventReceiver
import cn.vove7.energy_ring.ui.adapter.ColorsAdapter
import cn.vove7.energy_ring.ui.view.AccurateSeekBar
import cn.vove7.energy_ring.util.Config
import cn.vove7.energy_ring.util.antiColor
import cn.vove7.energy_ring.util.pickColor

/**
 * # BaseStyleFragment
 *
 * @author Vove
 * 2020/5/14
 */
@Suppress("PrivatePropertyName")
abstract class BaseStyleFragment : Fragment() {
    abstract val layoutRes: Int

    private val color_list by lazy {
        requireView().findViewById<RecyclerView>(R.id.color_list)
    }

    private val bg_color_view by lazy {
        requireView().findViewById<TextView>(R.id.bg_color_view)
    }

    private val strokeWidth_seek_bar by lazy {
        requireView().findViewById<AccurateSeekBar>(R.id.strokeWidth_seek_bar)
    }

    private val posx_seek_bar by lazy {
        requireView().findViewById<AccurateSeekBar>(R.id.posx_seek_bar)
    }

    private val posy_seek_bar by lazy {
        requireView().findViewById<AccurateSeekBar>(R.id.posy_seek_bar)
    }

    private val size_seek_bar by lazy {
        requireView().findViewById<AccurateSeekBar>(R.id.size_seek_bar)
    }

    private val charging_rotateDuration_seek_bar by lazy {
        requireView().findViewById<AccurateSeekBar>(R.id.charging_rotateDuration_seek_bar)
    }
    private val default_rotateDuration_seek_bar by lazy {
        requireView().findViewById<AccurateSeekBar>(R.id.default_rotateDuration_seek_bar)
    }

    private val spacing_seek_bar by lazy {
        requireView().findViewById<AccurateSeekBar>(R.id.spacing_seek_bar)
    }
    private val color_list_charging by lazy {
        requireView().findViewById<RecyclerView>(R.id.color_list_charging)
    }

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutRes, container, false)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        color_list.adapter = ColorsAdapter({ Config.colorsDischarging }, { Config.colorsDischarging = it })
        color_list_charging.adapter = ColorsAdapter({ Config.colorsCharging }, { Config.colorsCharging = it })
        refreshData()
        listenSeekBar(view)
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    @SuppressLint("NotifyDataSetChanged")
    @CallSuper
    open fun refreshData() = view.run {
        bg_color_view?.setBackgroundColor(Config.ringBgColor)
        bg_color_view?.setTextColor(Config.ringBgColor.antiColor)
        color_list?.adapter?.notifyDataSetChanged()
        color_list_charging?.adapter?.notifyDataSetChanged()
        strokeWidth_seek_bar?.progress = Config.strokeWidthF

        posx_seek_bar?.progress = Config.posXf.toFloat()
        posy_seek_bar?.progress = Config.posYf.toFloat()
        size_seek_bar?.progress = Config.size.toFloat()

        charging_rotateDuration_seek_bar?.progress =
            (charging_rotateDuration_seek_bar.maxVal + 1
                - Config.chargingRotateDuration / 1000)
        default_rotateDuration_seek_bar?.progress = (default_rotateDuration_seek_bar.maxVal + default_rotateDuration_seek_bar.minVal -
            (Config.defaultRotateDuration) / 1000)

        spacing_seek_bar?.progress = Config.spacingWidthF.toFloat()
    }

    @CallSuper
    open fun listenSeekBar(view: View): Unit = view.run {

        bg_color_view.setOnClickListener {
            pickColor(requireContext(), initColor = Config.ringBgColor) { c ->
                bg_color_view.setBackgroundColor(c)
                bg_color_view.setTextColor(c.antiColor)
                Config.ringBgColor = c
                FloatRingWindow.onShapeTypeChanged()
            }
        }
        charging_rotateDuration_seek_bar?.onStop { progress ->
            Config.chargingRotateDuration = (charging_rotateDuration_seek_bar.maxVal.toInt() +
                1 - progress) * 1000
            if (PowerEventReceiver.isCharging) {
                FloatRingWindow.reloadAnimation()
            }
        }
        default_rotateDuration_seek_bar?.onStop { progress -> //[60,180]
            Config.defaultRotateDuration = (default_rotateDuration_seek_bar.maxVal.toInt() -
                (progress - default_rotateDuration_seek_bar.minVal.toInt())) * 1000
            Log.d("Debug :", "listenSeekBar  ---->$progress ${Config.defaultRotateDuration}")
            if (!PowerEventReceiver.isCharging) {
                FloatRingWindow.reloadAnimation()
            }
        }
        strokeWidth_seek_bar?.onChange { progress, user ->
            if (!user) return@onChange
            Config.strokeWidthF = progress.toFloat()
            FloatRingWindow.update()
        }
        strokeWidth_seek_bar?.onStart {
            FloatRingWindow.forceRefresh()
        }
        posx_seek_bar?.onChange { progress, user ->
            if (!user) return@onChange
            Config.posXf = progress
            FloatRingWindow.update()
        }
        posy_seek_bar?.onChange { progress, user ->
            if (!user) return@onChange
            Config.posYf = progress
            FloatRingWindow.update()
        }
        size_seek_bar?.onStart {
            FloatRingWindow.forceRefresh()
        }
        size_seek_bar?.onChange { progress, user ->
            if (!user) return@onChange
            Config.size = progress
            FloatRingWindow.update()
        }
        spacing_seek_bar?.onStart {
            FloatRingWindow.forceRefresh()
        }
        spacing_seek_bar?.onChange { progress, user ->
            if (!user) return@onChange
            Config.spacingWidthF = progress
            FloatRingWindow.update()
        }
    } ?: Unit

}