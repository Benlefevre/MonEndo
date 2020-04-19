package com.benlefevre.monendo.ui.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat.getColor
import com.benlefevre.monendo.R
import com.benlefevre.monendo.utils.CURRENT_CHECKED
import com.benlefevre.monendo.utils.NEED_CLEAR
import com.benlefevre.monendo.utils.PREFERENCES
import timber.log.Timber

class ContraceptiveTablet(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var dividedWidth = 0f
    private var dividedHeight = 0f
    private var day = -1
    private var needClear = false

    private lateinit var rectF: RectF
    private var triangle: Path = Path()
    private var backPaint: Paint
    private var circlePaint: Paint
    private var linePaint: Paint
    private var shadowPaint: Paint
    var pills: MutableList<Pill> = mutableListOf()

    init {
        needClear = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getBoolean(NEED_CLEAR, false)
        backPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = getColor(context, R.color.colorPrimary)
            style = Paint.Style.FILL
        }
        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = getColor(context, R.color.colorOnPrimary)
            style = Paint.Style.FILL
        }
        linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = getColor(context, R.color.colorOnPrimary)
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 10f
        }
        shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = getColor(context, R.color.colorBackground)
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        createPills()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectF = RectF(0f, 0f, w.toFloat(), h.toFloat())
        dividedWidth = w / 9f
        dividedHeight = h / 5f

        if (pills.isEmpty())
            createPills()
        else
            pills.forEach {
                it.apply {
                    width = dividedWidth
                    height = dividedHeight
                    setShadow()
                }
            }
    }

    private fun createPills() {
        for (index in 1..4) {
            pills.add(Pill(dividedWidth, 2, dividedHeight, index, 40f, getColor(context, R.color.colorOnPrimary)))
            pills.add(Pill(dividedWidth, 3, dividedHeight, index, 40f, getColor(context, R.color.colorOnPrimary)))
            pills.add(Pill(dividedWidth, 4, dividedHeight, index, 40f, getColor(context, R.color.colorOnPrimary)))
            pills.add(Pill(dividedWidth, 5, dividedHeight, index, 40f, getColor(context, R.color.colorOnPrimary)))
            pills.add(Pill(dividedWidth, 6, dividedHeight, index, 40f, getColor(context, R.color.colorOnPrimary)))
            pills.add(Pill(dividedWidth, 7, dividedHeight, index, 40f, getColor(context, R.color.colorOnPrimary)))
            pills.add(Pill(dividedWidth, 8, dividedHeight, index, 40f, getColor(context, R.color.colorOnPrimary)))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRoundRect(rectF, 100f, 100f, backPaint)

        for (index in 1..4) {
            triangle.apply {
                moveTo(dividedWidth - 25, (dividedHeight * index) - 25)
                lineTo(dividedWidth, dividedHeight * index)
                lineTo(dividedWidth - 25, (dividedHeight * index) + 25)
                close()
            }
            canvas.drawPath(triangle, linePaint)
        }

        for (index in 1..3) {
            canvas.apply {
                drawLine(dividedWidth * 8, (dividedHeight * index) + 40, dividedWidth * 8, (dividedHeight * index) + (dividedHeight / 2), linePaint)
                drawLine((dividedWidth * 8) + 5, (dividedHeight * index) + (dividedHeight / 2), dividedWidth - 50, (dividedHeight * index) + (dividedHeight / 2), linePaint)
                drawLine(dividedWidth - 45, (dividedHeight * index) + (dividedHeight / 2), dividedWidth - 45, dividedHeight * (index + 1), linePaint)
                drawLine(dividedWidth - 50, dividedHeight * (index + 1), dividedWidth - 25, dividedHeight * (index + 1), linePaint)
            }
        }

        pills.forEach {
            circlePaint.color = it.color
            canvas.drawCircle(it.x, it.y, it.radius, circlePaint)
            if (!it.isChecked) {
                circlePaint.color = getColor(context, R.color.graph4)
                canvas.drawCircle(it.x, it.y, it.radius / 2, circlePaint)
                canvas.drawArc(it.shadowRectF, 0f, 70f, false, shadowPaint)
            } else
                canvas.drawArc(it.shadowRectF, -180f, 70f, false, shadowPaint)
        }
    }

    fun setupTablet(elapsedDays: Int) {
        day = elapsedDays
        if (day >= pills.size) day -= pills.size
        if (day == 0 && needClear) clearTablet()
        setMissingPills(day)
        setCurrentPill(day)
        setPillNotClickable(day)
        invalidate()
    }

    private fun setMissingPills(elapsedDays: Int) {
        for (index in 0..elapsedDays) {
            if (!pills[index].isChecked) {
                pills[index].apply {
                    color = getColor(context, R.color.graph5)
                    isClickable = true
                }
            }
        }
    }

    private fun setPillNotClickable(elapsedDays: Int) {
        for (index in elapsedDays + 1 until pills.size) {
            pills[index].apply {
                isClickable = false
                isChecked = false
                color = getColor(context, R.color.colorOnPrimary)
            }
        }
    }

    private fun setCurrentPill(elapsedDays: Int) {
        if (!pills[elapsedDays].isChecked) {
            pills[elapsedDays].apply {
                color = getColor(context, R.color.graph1)
                isClickable = true
            }
        }
        if (elapsedDays == 27) needClear = true
        isCurrentChecked()
    }

    private fun clearTablet() {
        pills.forEach {
            it.isChecked = false
            it.isClickable = true
        }
        needClear = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var isTouchPill: Boolean
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pills.forEach {
                    isTouchPill = it.checkTouchInCircle(event.x, event.y)
                    if (isTouchPill) {
                        isCurrentChecked()
                        if (!it.isChecked) {
                            it.color = getColor(context, R.color.colorOnSecondary)
                            it.isChecked = true
                        } else
                            it.isChecked = false
                    }
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (day != -1)
                    setupTablet(day)
                return true
            }
        }
        return false
    }

    private fun isCurrentChecked() {
        Timber.i("${pills[day].isChecked}")
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(CURRENT_CHECKED, pills[day].isChecked)
            .apply()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(NEED_CLEAR, needClear)
            .apply()
    }
}