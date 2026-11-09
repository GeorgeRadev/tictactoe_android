package org.game.xo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Two rows (X on top, O below). Each row shows: a turn indicator circle,
 * the row's player letter (X or O), the bold total win count, then the
 * results of the last games from that row's perspective: "+" win, "-"
 * loss, "=" draw (column count adapts to the screen width). The row of
 * the active player gets a lightGray background and a black circle.
 */
class ScoreboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var xTotal = 0
    var oTotal = 0
    /** Game results oldest first: 1 = X win, 2 = O win, 0 = draw. */
    var results: List<Int> = emptyList()
    /** 1 = X to move, 2 = O to move, 0 = none. */
    var activePlayer = 0

    private val density = resources.displayMetrics.density
    private val dp = { d: Int -> d * density }

    private val rowFillPaint = Paint().apply { color = GameColors.lightGray }
    private val circleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GameColors.black }
    private val circleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = GameColors.lightGray
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }
    private val totalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = GameColors.textBlack
        textSize = 18f * density
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = GameColors.textBlack
        textSize = 18f * density
        textAlign = Paint.Align.CENTER
    }
    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = GameColors.textBlack
        textSize = 16f * density
        textAlign = Paint.Align.CENTER
    }

    private var resultCols = 1

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val fixedW = dp(44) + dp(36) + dp(64)
        val minColW = dp(44).toInt()
        val avail = w - fixedW.toInt()
        resultCols = (avail / minColW).coerceAtLeast(1)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width
        val rowH = height / 2
        val indicatorW = dp(44).toInt()
        val playerW = dp(36).toInt()
        val totalW = dp(64).toInt()
        val totalStartX = indicatorW + playerW
        val colW = (w - totalStartX - totalW) / resultCols
        val offsetY = (cellPaint.fontMetrics.descent - cellPaint.fontMetrics.ascent) / 2f

        for (rowIx in 0..1) {
            val player = if (rowIx == 0) 1 else 2
            val top = rowIx * rowH
            val midY = top + rowH / 2f + offsetY

            if (player == activePlayer) {
                canvas.drawRect(0f, top.toFloat(), w.toFloat(), (top + rowH).toFloat(), rowFillPaint)
            }

            val cx = indicatorW / 2f
            val cy = top + rowH / 2f
            if (player == activePlayer) {
                canvas.drawCircle(cx, cy, dp(7).toFloat(), circleFillPaint)
            } else {
                canvas.drawCircle(cx, cy, dp(7).toFloat(), circleStrokePaint)
            }

            canvas.drawText(
                if (rowIx == 0) "X" else "O",
                indicatorW + playerW / 2f,
                midY,
                playerPaint
            )

            val total = if (rowIx == 0) xTotal else oTotal
            canvas.drawText(
                total.toString(),
                totalStartX + totalW / 2f,
                midY,
                totalPaint
            )

            val startIdx = (results.size - resultCols).coerceAtLeast(0)
            for (i in 0 until resultCols) {
                val idx = startIdx + i
                val mark = if (idx >= results.size) {
                    ""
                } else {
                    when (results[idx]) {
                        player -> "+"
                        0 -> "="
                        else -> "-"
                    }
                }
                canvas.drawText(
                    mark,
                    totalStartX + totalW + i * colW + colW / 2f,
                    midY,
                    cellPaint
                )
            }
        }
    }
}
