package com.kelvinht.moneylife.presentation.summary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.kelvinht.moneylife.R
import com.kelvinht.moneylife.core.format.RupiahFormatter
import java.time.YearMonth

class MonthlyDailySpendingChartView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : View(context, attrs) {
        private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.cardBorder)
            strokeWidth = resources.displayMetrics.density
        }
        private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.colorPrimary)
        }
        private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.textSecondary)
            textSize = 10f * resources.displayMetrics.scaledDensity
        }
        private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.textSecondary)
            textSize = 13f * resources.displayMetrics.scaledDensity
            textAlign = Paint.Align.CENTER
        }

        private var selectedMonth: YearMonth = YearMonth.now()
        private var dailyTotals: Map<String, Long> = emptyMap()

        fun setMonthlyData(
            month: YearMonth,
            totals: Map<String, Long>,
        ) {
            selectedMonth = month
            dailyTotals = totals
            contentDescription = context.getString(R.string.monthly_chart_accessibility, month)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val density = resources.displayMetrics.density
            val daysInMonth = selectedMonth.lengthOfMonth()
            val left = 58f * density
            val right = width - 8f * density
            val top = 14f * density
            val bottom = height - 28f * density
            val plotHeight = bottom - top
            val plotWidth = right - left
            if (plotWidth <= 0f || plotHeight <= 0f) return

            val values = (1..daysInMonth).map { day ->
                val dateKey = "$selectedMonth-${day.toString().padStart(2, '0')}"
                dailyTotals[dateKey] ?: 0L
            }
            val maxValue = values.maxOrNull() ?: 0L
            if (maxValue <= 0L) {
                canvas.drawText(
                    context.getString(R.string.monthly_chart_empty),
                    width / 2f,
                    (top + bottom) / 2f,
                    emptyPaint,
                )
                drawDateLabels(canvas, left, right, bottom, density, daysInMonth)
                return
            }

            val gridValues = listOf(maxValue, maxValue / 2L, 0L)
            gridValues.forEachIndexed { index, value ->
                val y = top + plotHeight * index / (gridValues.size - 1)
                canvas.drawLine(left, y, right, y, gridPaint)
                canvas.drawText(
                    RupiahFormatter.formatCompact(value),
                    4f * density,
                    y + labelPaint.textSize / 3f,
                    labelPaint,
                )
            }

            val slotWidth = plotWidth / daysInMonth
            val barWidth = minOf(12f * density, slotWidth * 0.65f).coerceAtLeast(2f * density)
            values.forEachIndexed { index, amount ->
                if (amount > 0L) {
                    val centerX = left + slotWidth * (index + 0.5f)
                    val barHeight = plotHeight * amount.toFloat() / maxValue.toFloat()
                    canvas.drawRoundRect(
                        centerX - barWidth / 2f,
                        bottom - barHeight,
                        centerX + barWidth / 2f,
                        bottom,
                        3f * density,
                        3f * density,
                        barPaint,
                    )
                }
            }
            drawDateLabels(canvas, left, right, bottom, density, daysInMonth)
        }

        private fun drawDateLabels(
            canvas: Canvas,
            left: Float,
            right: Float,
            bottom: Float,
            density: Float,
            daysInMonth: Int,
        ) {
            val slotWidth = (right - left) / daysInMonth
            for (day in 1..daysInMonth) {
                if (day == 1 || day % 5 == 0 || day == daysInMonth) {
                    val label = day.toString()
                    val x = left + slotWidth * (day - 0.5f)
                    canvas.drawText(label, x - labelPaint.measureText(label) / 2f, bottom + 18f * density, labelPaint)
                }
            }
        }
    }
