package com.kelvinht.moneyapp.feature.reports.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.kelvinht.moneyapp.R

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 34f
        strokeCap = Paint.Cap.BUTT
    }
    private val bounds = RectF()
    private var segments: List<Int> = listOf(40, 20, 10)
    private var colors: List<Int> = listOf(
        ContextCompat.getColor(context, R.color.home_primary),
        ContextCompat.getColor(context, R.color.report_purple),
        ContextCompat.getColor(context, R.color.home_card_green)
    )
    private val trackColor = ContextCompat.getColor(context, R.color.report_track)

    fun setSegments(values: List<Int>) {
        segments = values
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val padding = paint.strokeWidth / 2f + 4f
        bounds.set(padding, padding, width - padding, height - padding)
        paint.color = trackColor
        canvas.drawArc(bounds, 0f, 360f, false, paint)

        var startAngle = -90f
        segments.forEachIndexed { index, percentage ->
            paint.color = colors.getOrElse(index) { trackColor }
            val sweep = 360f * percentage / 100f
            canvas.drawArc(bounds, startAngle, sweep, false, paint)
            startAngle += sweep
        }
    }
}
