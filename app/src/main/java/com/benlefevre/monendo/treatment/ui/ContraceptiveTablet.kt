package com.benlefevre.monendo.treatment.ui

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
import com.benlefevre.monendo.treatment.models.Pill
import com.benlefevre.monendo.utils.CURRENT_CHECKED
import com.benlefevre.monendo.utils.NEED_CLEAR
import com.benlefevre.monendo.utils.NUMBER_OF_PILLS
import com.benlefevre.monendo.utils.PREFERENCES
import timber.log.Timber

class ContraceptiveTablet(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var dividedWidth7Pills = 0f
    private var dividedWidth6Pills = 0f
    private var dividedWidth5Pills = 0f

    private var dividedHeight2Lines = 0f
    private var dividedHeight4Lines = 0f
    private var dividedHeight3Lines = 0f

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
            .getString(NUMBER_OF_PILLS, "28")!!.toInt()
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

        dividedWidth7Pills = w / 9f
        dividedWidth6Pills = w / 8f
        dividedWidth5Pills = w / 7f

        dividedHeight4Lines = h / 5f
        dividedHeight3Lines = h / 4f
        dividedHeight2Lines = h / 3f


        if (pills.isEmpty())
            createPills()
        else
            pills.forEach {
                it.apply {
                    when (nbPills) {
                        10 -> {
                            width = dividedWidth5Pills
                            height = dividedHeight2Lines
                        }
                        12 -> {
                            width = dividedWidth6Pills
                            height = dividedHeight2Lines
                        }
                        14 -> {
                            width = dividedWidth7Pills
                            height = dividedHeight2Lines
                        }
                        21 -> {
                            width = dividedWidth7Pills
                            height = dividedHeight3Lines
                        }
                        28 -> {
                            width = dividedWidth7Pills
                            height = dividedHeight4Lines
                        }
                        29 -> {
                            width = dividedWidth7Pills
                            height = dividedHeight4Lines
                        }
                    }
                    setShadow()
                }
            }
    }

    private fun createPills() {
        Timber.i("createPills")
        val colorOnPrimary = getColor(context, R.color.colorOnPrimary)
        pills.clear()

        var width = 0.0f
        var height = 0.0f
        var rangeLine: IntRange = 0..1
        var rangePills: IntRange = 0..1
        when (nbPills) {
            29 -> {
                width = dividedWidth7Pills
                height = dividedHeight4Lines
                rangeLine = 1..4
                rangePills = 2..8
            }
            28 -> {
                width = dividedWidth7Pills
                height = dividedHeight4Lines
                rangeLine = 1..4
                rangePills = 2..8
            }
            21 -> {
                width = dividedWidth7Pills
                height = dividedHeight3Lines
                rangeLine = 1..3
                rangePills = 2..8
            }
            14 -> {
                width = dividedWidth7Pills
                height = dividedHeight2Lines
                rangeLine = 1..2
                rangePills = 2..8
            }
            12 -> {
                width = dividedWidth6Pills
                height = dividedHeight2Lines
                rangeLine = 1..2
                rangePills = 2..7
            }
            10 -> {
                width = dividedWidth5Pills
                height = dividedHeight2Lines
                rangeLine = 1..2
                rangePills = 2..6
            }
        }
        for (index in rangeLine) {
            for (indexWidth in rangePills) {
                pills.add(
                    Pill(
                        width,
                        indexWidth,
                        height,
                        index,
                        40f,
                        colorOnPrimary
                    )
                )
            }
        }
    }

    fun setNumberOfPills(numberPills: Int) {
        Timber.i("setNumberOfPills")
        nbPills = numberPills
        createPills()
        if (day != -1)
            setupTablet(day)
        else
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
            29 -> {
                nbTriangles = 4
                height = dividedHeight4Lines
                width = dividedWidth7Pills
                nbLines = 3
                lineLength = 8
            }
            28 -> {
                nbTriangles = 4
                height = dividedHeight4Lines
                width = dividedWidth7Pills
                nbLines = 3
                lineLength = 8
            }
            21 -> {
                nbTriangles = 3
                height = dividedHeight3Lines
                width = dividedWidth7Pills
                nbLines = 2
                lineLength = 8
            }
            14 -> {
                nbTriangles = 2
                height = dividedHeight2Lines
                width = dividedWidth7Pills
                nbLines = 1
                lineLength = 8
            }
            12 -> {
                nbTriangles = 2
                height = dividedHeight2Lines
                width = dividedWidth6Pills
                nbLines = 1
                lineLength = 7
            }
            10 -> {
                nbTriangles = 2
                height = dividedHeight2Lines
                width = dividedWidth5Pills
                nbLines = 1
                lineLength = 6
            }
            else -> {
                nbTriangles = 4
                height = dividedHeight4Lines
                width = dividedHeight4Lines
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
                canvas.drawArc(it.shadowRectF, 0f, 70f, false, shadowPaint.apply {
                    color = getColor(context, R.color.colorBackground)
                })
            } else
                canvas.drawArc(it.shadowRectF, -180f, 70f, false, shadowPaint.apply {
                    color = getColor(context, R.color.colorOnPrimary)
                })
        }
        if (nbPills == 29) {
            circlePaint.color = getColor(context, R.color.graph3)
            for (index in 21..27) {
                with(pills[index]) {
                    if (!isChecked) {
                        canvas.drawCircle(x, y, radius / 2, circlePaint)
                    }
                }
            }
        }
    }

    fun setupTablet(elapsedDays: Int) {
        Timber.i("setupTablet + days = $elapsedDays / ${pills.size}")
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
        when (nbPills) {
            10 -> if (elapsedDays == 9) needClear = true
            12 -> if (elapsedDays == 11) needClear = true
            14 -> if (elapsedDays == 13) needClear = true
            21 -> if (elapsedDays == 20) needClear = true
            28 -> if (elapsedDays == 27) needClear = true
            29 -> if (elapsedDays == 27) needClear = true
        }
        isCurrentChecked()
    }

    private fun isCurrentChecked() {
        Timber.i("isCurrentChecked")
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit().apply {
                putBoolean(NEED_CLEAR, needClear)
                putString(NUMBER_OF_PILLS, nbPills.toString())
                putBoolean(CURRENT_CHECKED, pills[day].isChecked)
            }
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
                pills.forEach { pill ->
                    isTouchPill = pill.checkTouchInCircle(event.x, event.y)
                    if (isTouchPill) {
                        if (day != -1) {
                            isCurrentChecked()
                        }
                        if (!pill.isChecked) {
                            pill.color = getColor(context, R.color.colorBackground)
                            pill.isChecked = true
                        } else
                            pill.isChecked = false
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
}