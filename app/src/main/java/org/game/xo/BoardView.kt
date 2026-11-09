package org.game.xo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Square game board: transparent background, black inner grid lines only,
 * filled cells with a rounded lightGray background (inset by 2px) and a
 * black X/O mark, a red line over the winning row.
 */
class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var gridSize = 3
    var cells = IntArray(9)
    /** Cell indexes of the winning line, or null. */
    var winningLine: List<Int>? = null
    var onCellClick: ((row: Int, col: Int) -> Unit)? = null

    private val density = resources.displayMetrics.density
    private val cellFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GameColors.lightGray }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = GameColors.black
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    private val winLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = GameColors.red
        style = Paint.Style.STROKE
        strokeWidth = 4f * density
        strokeCap = Paint.Cap.ROUND
    }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = GameColors.black
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
        strokeCap = Paint.Cap.ROUND
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // square board sized to the available width
        val w = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(w, w)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width
        val h = height
        val cell = w / gridSize
        // only inner grid lines, no outer border
        for (i in 1 until gridSize) {
            canvas.drawLine(i * cell.toFloat(), 0f, i * cell.toFloat(), h.toFloat(), linePaint)
            canvas.drawLine(0f, i * cell.toFloat(), w.toFloat(), i * cell.toFloat(), linePaint)
        }

        // gray cell background: 2px smaller than the cell, rounded corners
        val inset = 16f
        val radius = 16f * density
        val fillRect = RectF()
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val mark = cells[r * gridSize + c]
                if (mark == GameEngine.EMPTY) continue
                val left = c * cell
                val top = r * cell
                fillRect.set(
                    left + inset, top + inset,
                    left + cell - inset, top + cell - inset
                )
                canvas.drawRoundRect(fillRect, radius, radius, cellFillPaint)
                val m = cell * 0.22f
                when (mark) {
                    GameEngine.X -> {
                        canvas.drawLine(left + m, top + m, left + cell - m, top + cell - m, iconPaint)
                        canvas.drawLine(left + cell - m, top + m, left + m, top + cell - m, iconPaint)
                    }
                    GameEngine.O -> canvas.drawCircle(left + cell / 2f, top + cell / 2f, cell / 2f - m, iconPaint)
                }
            }
        }

        // red line over the winning row
        winningLine?.let { line ->
            if (line.size >= 2) {
                val first = line.first()
                val last = line.last()
                canvas.drawLine(
                    (first % gridSize) * cell + cell / 2f,
                    (first / gridSize) * cell + cell / 2f,
                    (last % gridSize) * cell + cell / 2f,
                    (last / gridSize) * cell + cell / 2f,
                    winLinePaint
                )
            }
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // accept the gesture, otherwise ACTION_UP is never delivered to a plain View
        if (event.action == MotionEvent.ACTION_DOWN) return true
        if (event.action == MotionEvent.ACTION_UP) {
            val cell = width / gridSize
            if (cell > 0) {
                val col = event.x.toInt() / cell
                val row = event.y.toInt() / cell
                if (row in 0 until gridSize && col in 0 until gridSize) {
                    onCellClick?.invoke(row, col)
                    performClick()
                    return true
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
