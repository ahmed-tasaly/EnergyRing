package cn.vove7.energy_ring.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import cn.vove7.energy_ring.R
import cn.vove7.energy_ring.databinding.AccurateSeekBarBinding
import com.google.android.material.slider.Slider

/**
 * # AccurateSeekBar
 *
 * @author Vove
 * 2020/5/9
 */
@Suppress("HasPlatformType", "PropertyName")
class AccurateSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var title: CharSequence? = null
        set(value) {
            field = value
            vb.titleView.text = value
        }

    val minVal: Float get() = vb.seekBarView.valueFrom

    val maxVal: Float get() = vb.seekBarView.valueTo

    var progress: Float
        set(value) {
            vb.seekBarView.value = value.coerceAtMost(vb.seekBarView.valueTo)
                .coerceAtLeast(vb.seekBarView.valueFrom)
        }
        get() = vb.seekBarView.value

    private val vb by lazy {
        AccurateSeekBarBinding.inflate(LayoutInflater.from(context), this, true)
    }

    init {
        val ats = context.obtainStyledAttributes(attrs, R.styleable.AccurateSeekBar)
        title = ats.getString(R.styleable.AccurateSeekBar_title)

        vb.seekBarView.valueTo = ats.getFloat(R.styleable.AccurateSeekBar_max, 100f)
        vb.seekBarView.valueFrom = ats.getFloat(R.styleable.AccurateSeekBar_min, 0f)

        ats.recycle()
        vb.plusView.setOnClickListener {
            val p = vb.seekBarView.value + 1
            if (p <= vb.seekBarView.valueTo) {
                vb.seekBarView.value = p
                onChangeAction?.invoke(p.toInt(), true)
            }
        }
        vb.minusView.setOnClickListener {
            val p = vb.seekBarView.value - 1
            if (p >= vb.seekBarView.valueFrom) {
                vb.seekBarView.value = p
                onChangeAction?.invoke(p.toInt(), true)
            }
        }
        vb.seekBarView.addOnChangeListener { _, value, user ->
            onChangeAction?.invoke(value.toInt(), user)
        }
        vb.seekBarView.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                onStartAction?.invoke()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                onStopAction?.invoke(slider.value.toInt())
            }
        })
    }

    fun onChange(lis: (progress: Int, fromUser: Boolean) -> Unit) {
        onChangeAction = lis
    }

    private var onStopAction: ((progress: Int) -> Unit)? = null
    private var onStartAction: (() -> Unit)? = null

    private var onChangeAction: ((progress: Int, fromUser: Boolean) -> Unit)? = null

    fun onStart(startAction: () -> Unit) {
        onStartAction = startAction
    }

    fun onStop(stopAction: ((progress: Int) -> Unit)) {
        onStopAction = stopAction
    }

}