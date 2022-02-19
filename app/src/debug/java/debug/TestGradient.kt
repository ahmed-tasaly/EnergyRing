package debug

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.min

/**
 * # debug.TestGradient
 *
 * @author Vove
 * @date 2021/9/8
 */
class TestGradient : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fl = FrameLayout(this)

        fl.addView(CV(this), FrameLayout.LayoutParams(500, 500).also {
            it.gravity = Gravity.CENTER
        })
        setContentView(fl)
    }

    class CV @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

        val progressf = 0.8f

        var strokeWidthF = 8f
        private val paint by lazy {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLUE
            }
        }

        private val rectF = RectF()
        var mainColor = Color.GREEN

        private fun initPaint() {
            paint.reset()
            paint.isAntiAlias = true
        }

        val radialGradient: RadialGradient by lazy {
            val size = height
            RadialGradient(size / 2f,
                size / 2f, size / 2f,
                mainColor,
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP)
        }

        override fun onDraw(canvas: Canvas?) {
            canvas ?: return
            val size = min((width / 2).toFloat(), (height / 2).toFloat())
            canvas.translate(size, size)
            canvas.rotate(-90f)
            val strokeWidth = size * (strokeWidthF / 100f)
            val r = size - strokeWidth / 2
            initPaint()

            //圆环外接矩形
            rectF.set(-r, -r, r, r)

            //背景
            paint.color = Color.WHITE
            paint.strokeWidth = strokeWidth
            paint.style = Paint.Style.STROKE
            canvas.drawArc(rectF, 0f, 360f, true, paint)

            initPaint()
            //圆环
            paint.strokeWidth = strokeWidth
            paint.style = Paint.Style.STROKE

            paint.color = mainColor
            paint.shader = radialGradient

            canvas.drawArc(rectF, 0f, 360f * progressf, false, paint)
        }
    }
}