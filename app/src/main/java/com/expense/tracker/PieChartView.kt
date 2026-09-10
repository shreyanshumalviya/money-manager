package com.expense.tracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

data class Slice(val name: String, val amount: Double, val color: Int)

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val slices = mutableListOf<Slice>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    private val defaultColors = intArrayOf(
        Color.parseColor("#10B981"), // Green (Food)
        Color.parseColor("#38BDF8"), // Blue (Rent)
        Color.parseColor("#F59E0B"), // Amber (Travel)
        Color.parseColor("#8B5CF6"), // Purple (Subscriptions)
        Color.parseColor("#EC4899"), // Pink (Luxury)
        Color.parseColor("#64748B")  // Slate
    )

    fun setData(categories: List<Pair<String, Double>>) {
        slices.clear()
        val nonZero = categories.filter { it.second > 0 }
        nonZero.forEachIndexed { index, pair ->
            val color = defaultColors[index % defaultColors.size]
            slices.add(Slice(pair.first, pair.second, color))
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (slices.isEmpty()) return

        val total = slices.sumOf { it.amount }
        if (total <= 0) return

        val diameter = min(width, height).toFloat() - 32f
        val left = (width - diameter) / 2f
        val top = (height - diameter) / 2f
        rectF.set(left, top, left + diameter, top + diameter)

        var startAngle = -90f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = diameter * 0.22f

        for (slice in slices) {
            val sweepAngle = ((slice.amount / total) * 360f).toFloat()
            paint.color = slice.color
            canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)
            startAngle += sweepAngle
        }
    }
}