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
import com.benlefevre.monendo.utils.NUMBER_OF_PILLS
import com.benlefevre.monendo.utils.PREFERENCES
import timber.log.Timber

class ContraceptiveTablet(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var dividedWidth28 = 0f
    private var dividedHeight28 = 0f
    private var dividedWidth10 = 0f
    private var dividedHeight10 = 0f
    private var day = -1
    private var needClear = false
    private var nbPills = 28

    private lateinit var rectF: RectF
    private var triangle: Path = Path()
    private var backPaint: Paint
    private var circlePaint: Paint
    private var linePaint: Paint
    private var shadowPaint: Paint
    var pills: MutableList<Pill> = mutableListOf()

    init {
        Timber.i("Pill init")
        needClear = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getBoolean(NEED_CLEAR, false)
        nbPills = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getInt(NUMBER_OF_PILLS, 28)
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
        Timber.i("onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Timber.i("onSizeChanged")
        super.onSizeChanged(w, h, oldw, oldh)
        rectF = RectF(0f, 0f, w.toFloat(), h.toFloat())

        dividedWidth28 = w / 9f
        dividedHeight28 = h / 5f

        dividedWidth10 = w / 7f
        dividedHeight10 = h / 3f

        if (pills.isEmpty())
            createPills()
        else
            pills.forEach {
                it.apply {
                    width = if (nbPills == 10) dividedWidth10 else dividedWidth28
                    height = if (nbPills == 10) dividedHeight10 else dividedHeight28
                    setShadow()
                }
            }
    }

    private fun createPills() {
        Timber.i("createPills")
        val colorOnPrimary = getColor(context,R.color.colorOnPrimary)
        pills.clear()
        when (nbPills) {
            28 -> {
                for (index in 1..4) {
                    pills.add(Pill(dividedWidth28, 2, dividedHeight28, index, 40f, colorOnPrimary))
                    pills.add(Pill(dividedWidth28, 3, dividedHeight28, index, 40f, colorOnPrimary))
                    pills.add(Pill(dividedWidth28, 4, dividedHeight28, index, 40f, colorOnPrimary))
                    pills.add(Pill(dividedWidth28, 5, dividedHeight28, index, 40f, colorOnPrimary))
                    pills.add(Pill(dividedWidth28, 6, dividedHeight28, index, 40f, colorOnPrimary))
                    pills.add(Pill(dividedWidth28, 7, dividedHeight28, index, 40f, colorOnPrimary))
                    pills.add(Pill(dividedWidth28, 8, dividedHeight28, index, 40f, colorOnPrimary))
                }
            }
            10 -> {
                for (index in 1..2) {
                    pills.add(Pill(dividedWidth10, 2, dividedHeight10, index, 40f, colorOnPrimary))
                    pills.add(Pill(dividedWidth10, 3, dividedHeight10, index, 40f, colorOnPrimary))
                    pills.add(Pill(dividedWidth10, 4, dividedHeight10, index, 40f, colorOnPrimary))
                    pills.add(Pill(dividedWidth10, 5, dividedHeight10, index, 40f, colorOnPrimary))
                    pills.add(Pill(dividedWidth10, 6, dividedHeight10, index, 40f, colorOnPrimary))
                }
            }
        }
    }

    fun setNumberOfPills(numberPills: Int) {
        Timber.i("setNumberOfPills")
        nbPills = numberPills
        createPills()
        if (day != -1)
            setupTablet(day)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        Timber.i("onDraw")
        super.onDraw(canvas)
        triangle.reset()
        canvas.drawRoundRect(rectF, 100f, 100f, backPaint)

        val nbTriangles: Int
        val height: Float
        val width: Float
        val lineLength: Int
        val nbLines: Int
        when (nbPills) {
            28 -> {
                nbTriangles = 4
                height = dividedHeight28
                width = dividedWidth28
                nbLines = 3
                lineLength = 8
            }
            10 -> {
                nbTriangles = 2
                height = dividedHeight10
                width = dividedWidth10
                nbLines = 1
                lineLength = 6
            }
            else -> {
                nbTriangles = 4
                height = dividedHeight28
                width = dividedHeight28
                nbLines = 3
                lineLength = 8
            }
        }

        for (index in 1..nbTriangles) {
            triangle.apply {
                moveTo(width - 25, (height * index) - 25)
                lineTo(width, height * index)
                lineTo(width - 25, (height * index) + 25)
                close()
            }
            canvas.drawPath(triangle, linePaint)
        }

        for (index in 1..nbLines) {
            canvas.apply {
                drawLine(width * lineLength, (height * index) + 40, width * lineLength, (height * index) + (height / 2), linePaint)
                drawLine((width * lineLength) + 5, (height * index) + (height / 2), width - 50, (height * index) + (height / 2), linePaint)
                drawLine(width - 45, (height * index) + (height / 2), width - 45, height * (index + 1), linePaint)
                drawLine(width - 50, height * (index + 1), width - 25, height * (index + 1), linePaint)
            }
        }

        pills.forEach {
            circlePaint.color = it.color
            canvas.drawCircle(it.x, it.y, it.radius, circlePaint)
            if (!it.isChecked) {
                circlePaint.color = getColor(context, R.color.graph4)
                canvas.drawCircle(it.x, it.y, it.radius / 2, circlePaint)
                canvas.drawArc(it.shadowRectF, 0f, 70f, false, shadowPaint.apply { color = getColor(context,R.color.colorBackground ) })
            } else
                canvas.drawArc(it.shadowRectF, -180f, 70f, false, shadowPaint.apply { color = getColor(context,R.color.colorOnPrimary ) })
        }
    }

    fun setupTablet(elapsedDays: Int) {
        Timber.i("setupTablet")
        day = elapsedDays
        if (day >= pills.size) day -= pills.size
        if (day == 0 && needClear) clearTablet()
        setMissingPills(day)
        setCurrentPill(day)
        setPillNotClickable(day)
        invalidate()
    }

    private fun setMissingPills(elapsedDays: Int) {
        Timber.i("setMissingPills")
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
        Timber.i("setPillNotClickable")
        for (index in elapsedDays + 1 until pills.size) {
            pills[index].apply {
                isClickable = false
                isChecked = false
                color = getColor(context, R.color.colorOnPrimary)
            }
        }
    }

    private fun setCurrentPill(elapsedDays: Int) {
        Timber.i("setCurrentPill")
        if (!pills[elapsedDays].isChecked) {
            pills[elapsedDays].apply {
                color = getColor(context, R.color.graph1)
                isClickable = true
            }
        }
        when(nbPills){
            10 -> if (elapsedDays == 9) needClear = true
            28 -> if (elapsedDays == 27) needClear = true
        }
        isCurrentChecked()
    }

    private fun isCurrentChecked() {
        Timber.i("isCurrentChecked")
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(CURRENT_CHECKED, pills[day].isChecked)
            .apply()
    }

    private fun clearTablet() {
        Timber.i("clearTablet")
        pills.forEach {
            it.isChecked = false
            it.isClickable = true
        }
        needClear = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Timber.i("onTouchEvent")
        var isTouchPill: Boolean
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pills.forEach {
                    isTouchPill = it.checkTouchInCircle(event.x, event.y)
                    if (isTouchPill) {
                        if (day != -1) {
                            isCurrentChecked()
                        }
                        if (!it.isChecked) {
                            it.color = getColor(context, R.color.colorBackground)
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(NEED_CLEAR, needClear)
            .apply()
    }
}