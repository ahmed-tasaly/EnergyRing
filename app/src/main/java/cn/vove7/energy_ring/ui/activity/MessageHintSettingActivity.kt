package cn.vove7.energy_ring.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import cn.vove7.energy_ring.R
import cn.vove7.energy_ring.databinding.ActivityMessageHintSettingBinding
import cn.vove7.energy_ring.floatwindow.FloatRingWindow
import cn.vove7.energy_ring.listener.NotificationListener
import cn.vove7.energy_ring.ui.view.slider.ReversibleRangeSlider
import cn.vove7.energy_ring.util.Config
import cn.vove7.energy_ring.util.openNotificationService
import cn.vove7.smartkey.get
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.customview.customView
import kotlin.math.ceil

/**
 * # MessageHintSettingActivity
 *
 * @author Vove
 * 2020/5/14
 */
@Suppress("PrivatePropertyName")
class MessageHintSettingActivity : BaseActivity() {

    private var checkOpen = false

    private val vb by lazy {
        ActivityMessageHintSettingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(vb.root)

        vb.serviceStatusButton.setOnClickListener {
            if (vb.serviceStatusButton.isSelected) {
                NotificationListener.stop()
                refreshStatusButton()
            } else if (!NotificationListener.isConnect) {
                checkOpen = true
                openNotificationService()
            } else {
                NotificationListener.resume()
                refreshStatusButton()
            }
        }
        vb.previewButton.setOnClickListener {
            FloatRingWindow.hide()
            startActivityForResult(Intent(this, MessageHintActivity::class.java), 10)
        }
        showTips()
        refreshDoNotDisturbRange()
    }

    private fun refreshDoNotDisturbRange() {
        val r = Config.doNotDisturbRange
        vb.doNotDisturbTimeView.text = getString(
            R.string.do_not_disturb_time_s,
            "${r.first}:00-${r.second}:00"
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10) {
            FloatRingWindow.show()
        }
    }

    private fun showTips() {
        if (!Config["tips_of_notification_hint", true, false]) {
            return
        }
        MaterialDialog(this).show {
            title(R.string.prompt)
            cancelable(false)
            cancelOnTouchOutside(false)
            message(R.string.screenoff_reminder_hint)

            noAutoDismiss()
            positiveButton(text = "10s")
            getActionButton(WhichButton.POSITIVE).isEnabled = false
            object : CountDownTimer(10000, 1000) {
                override fun onFinish() {
                    getActionButton(WhichButton.POSITIVE).isEnabled = true
                    positiveButton(R.string.i_know) {
                        Config["tips_of_notification_hint"] = false
                        dismiss()
                    }
                }

                override fun onTick(millis: Long) {
                    positiveButton(text = "${ceil(millis / 1000.0).toInt()}s")
                }
            }.start()
        }

    }

    override fun onResume() {
        super.onResume()
        if (checkOpen) {
            if (NotificationListener.isConnect) {
                NotificationListener.resume()
                checkOpen = false
            }
        }
        refreshStatusButton()
    }

    private fun refreshStatusButton() {
        vb.serviceStatusButton.isSelected = NotificationListener.isOpen
        vb.serviceStatusButton.text = if (vb.serviceStatusButton.isSelected) "停止服务" else "开启服务"
    }

    /**
     * 选取勿扰时间段
     * todo: 支持 23:00-5:00
     * @param view View
     */
    @SuppressLint("SetTextI18n")
    @Suppress("UNUSED_PARAMETER")
    fun pickTimeRange(view: View) {
        MaterialDialog(this).show {
            title(R.string.do_not_disturb_time)
            val v = LayoutInflater.from(this@MessageHintSettingActivity)
                .inflate(R.layout.range_picker, null)
            this.customView(view = v)
            var r = Config.doNotDisturbRange
            v.findViewById<ReversibleRangeSlider>(R.id.range_slider).apply {
                stepSize = 1f
                valueFrom = 0f
                valueTo = 23f

                addOnChangeListener { slider, value, _ ->
                    Log.d("Debug :", "pickTimeRange  ----> $value ${slider.values}")
                    r = slider.values.let { it[0].toInt() to it[1].toInt() }
                    v.findViewById<TextView>(R.id.range_text).text = "${r.first}:00-${r.second}:00"
                }
                setSupportReverse(true)
                values = listOf(r.first.toFloat(), r.second.toFloat())
            }

            positiveButton {
                Config.doNotDisturbRange = r
                refreshDoNotDisturbRange()
            }
            negativeButton()
        }
    }

}