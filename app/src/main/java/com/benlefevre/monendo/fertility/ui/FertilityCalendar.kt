package com.benlefevre.monendo.fertility.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat.getColor
import com.benlefevre.monendo.R
import com.benlefevre.monendo.fertility.models.CalendarDay
import com.benlefevre.monendo.utils.formatDateToDayName
import timber.log.Timber
import java.util.*

class FertilityCalendar(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var dividedWidth = 0f
    private var dividedHeight = 0f
    private var textSize = 0f

    private var firstDayCalendar = 0
    private var maxDays = 0
    private var previousMaxDays = 0

    private var calendarDays: MutableList<CalendarDay> = mutableListOf()

    private lateinit var rectF: RectF
    private lateinit var dayRectF: RectF
    private var linePaint: Paint
    private var backPaint: Paint
    private var textPaint: TextPaint

    init {
        linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = getColor(context, R.color.colorPrimaryVariant)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        backPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = getColor(context, R.color.colorPrimary)
            style = Paint.Style.FILL_AND_STROKE
        }
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = getColor(context, R.color.colorOnPrimary)
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        initCalendar()
        createCalendarDays()
    }

    private fun initCalendar() {
        maxDays = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayMonth = with(Calendar.getInstance()) {
            set(Calendar.DAY_OF_MONTH, 1)
            time
        }
        setupFirstDay(formatDateToDayName(firstDayMonth))
        initPreviousMonth()
    }

    private fun setupFirstDay(firstDayMonth: String) {
        when (firstDayMonth) {
            "Mon" -> firstDayCalendar = 0
            "Tue" -> firstDayCalendar = 1
            "Wed" -> firstDayCalendar = 2
            "Thu" -> firstDayCalendar = 3
            "Fri" -> firstDayCalendar = 4
            "Sat" -> firstDayCalendar = 5
            "Sun" -> firstDayCalendar = 6
        }
    }

    private fun initPreviousMonth() {
        previousMaxDays = with(Calendar.getInstance()) {
            add(Calendar.MONTH, -1)
            getActualMaximum(Calendar.DAY_OF_MONTH)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectF = RectF(0f, 0f, oldw.toFloat(), oldh.toFloat())
        dividedHeight = h / 7f
        dividedWidth = w / 7f
        dayRectF = RectF(0f, 0f, w.toFloat(), dividedHeight)
        textSize = dividedHeight * 0.30f
        textPaint.textSize = textSize

        configureCalendarDays()
    }

    /**
     * Creates all days in the calendar and calls needed functions to set each CalendarDay's number
     * and text color according its position in the calendar
     */
    private fun createCalendarDays() {
        for (index in 1..6) {
            calendarDays.add(
                CalendarDay(
                    dividedWidth,
                    1,
                    dividedHeight,
                    index
                )
            )
            calendarDays.add(
                CalendarDay(
                    dividedWidth,
                    2,
                    dividedHeight,
                    index
                )
            )
            calendarDays.add(
                CalendarDay(
                    dividedWidth,
                    3,
                    dividedHeight,
                    index
                )
            )
            calendarDays.add(
                CalendarDay(
                    dividedWidth,
                    4,
                    dividedHeight,
                    index
                )
            )
            calendarDays.add(
                CalendarDay(
                    dividedWidth,
                    5,
                    dividedHeight,
                    index
                )
            )
            calendarDays.add(
                CalendarDay(
                    dividedWidth,
                    6,
                    dividedHeight,
                    index
                )
            )
            calendarDays.add(
                CalendarDay(
                    dividedWidth,
                    7,
                    dividedHeight,
                    index
                )
            )
        }
        configureCurrentMonth()
        configurePreviousMonth()
        configureNextMonth()
    }

    /**
     * Configures each CalendarDay with the right width and height
     */
    private fun configureCalendarDays() {
        calendarDays.forEach {
            it.width = dividedWidth
            it.height = dividedHeight
            it.setRectF()
        }
    }

    /**
     * Configures the CalendarDay's number and text color for the next month
     */
    private fun configureNextMonth() {
        var dayIndex = 1
        for (index in (firstDayCalendar + maxDays) until calendarDays.size) {
            calendarDays[index].apply {
                numberDay = dayIndex
                textColor = getColor(context, R.color.colorBackground)
                color = getColor(context, R.color.colorOnPrimary)
            }
            dayIndex++
        }
    }

    /**
     * Configures the CalendarDay's number and text color for the previous month
     */
    private fun configurePreviousMonth() {
        for (index in (firstDayCalendar - 1) downTo 0) {
            calendarDays[index].apply {
                numberDay = previousMaxDays
                textColor = getColor(context, R.color.colorBackground)
                color = getColor(context, R.color.colorOnPrimary)
            }
            previousMaxDays--
        }
    }

    /**
     * Configures the CalendarDay's number and text color for the current month
     */
    private fun configureCurrentMonth() {
        var dayIndex = 1
        var index = firstDayCalendar
        while (dayIndex <= maxDays) {
            calendarDays[index].apply {
                numberDay = dayIndex
                textColor = getColor(context, R.color.colorPrimary)
                color = getColor(context, R.color.colorOnPrimary)
            }
            dayIndex++
            index++
        }
    }

    /**
     * Resets all CalendarDays with the correct textColor
     */
    fun clearAllCalendarDays() {
        calendarDays.forEach {
            it.color = getColor(context, R.color.colorOnPrimary)
        }
        for (index in 0 until firstDayCalendar) {
            calendarDays[index].textColor = getColor(context, R.color.colorBackground)
        }
        for (index in firstDayCalendar until (firstDayCalendar + maxDays)) {
            calendarDays[index].textColor = getColor(context, R.color.colorPrimary)
        }
        for (index in (firstDayCalendar + maxDays) until calendarDays.size) {
            calendarDays[index].textColor = getColor(context, R.color.colorBackground)
        }
    }

    fun drawCycleInCalendar(firstDay: Int, durationCycle: Int) {
        Timber.i("$firstDay")
        val lastMens = firstDayCalendar + (firstDay - 1)
        val lastDayLastMens = lastMens + 5
        val previousMens = lastMens - durationCycle
        val previousLastDay = previousMens + 5
        val nextMens = lastMens + durationCycle
        val lastDayNextMens = nextMens + 5
        val ovulDay = nextMens - 14
        val previousOvulDay = lastMens - 14
        val fertiBegin = ovulDay - 3
        val fertiEnd = ovulDay + 1
        val previousFertiBegin = previousOvulDay - 3
        val previousFertiEnd = previousOvulDay + 1

        clearAllCalendarDays()

        drawAllMenstruationInCalendar(
            previousMens, previousLastDay, lastMens,
            lastDayLastMens, lastDayNextMens, nextMens
        )

        drawAllFertilizationInCalendar(previousFertiBegin, previousFertiEnd, fertiBegin, fertiEnd)

        drawOvulationInCalendar(previousOvulDay, ovulDay)

        invalidate()
    }

    private fun drawOvulationInCalendar(previousOvulDay: Int, ovulDay: Int) {
        //        draw previous ovulation period
        if (previousOvulDay >= 0 && previousOvulDay < calendarDays.size)
            drawOvulation(previousOvulDay)
        //        draw ovulation period
        if (ovulDay >= 0 && ovulDay < calendarDays.size)
            drawOvulation(ovulDay)
    }

    private fun drawAllFertilizationInCalendar(
        previousFertiBegin: Int, previousFertiEnd: Int,
        fertiBegin: Int, fertiEnd: Int
    ) {
        //        draw previous fertilization period
        drawFertilizationPeriod(previousFertiBegin, previousFertiEnd)
        //        draw current fertilization period
        drawFertilizationPeriod(fertiBegin, fertiEnd)
    }

    private fun drawFertilizationPeriod(
        fertiBegin: Int,
        fertiEnd: Int
    ) {
        if (fertiBegin < 0 && fertiEnd >= 0) {
            for (index in 0..fertiEnd)
                drawFertilizationDays(index)
        }
        if (fertiBegin < calendarDays.size && fertiEnd >= calendarDays.size) {
            for (index in fertiBegin until calendarDays.size)
                drawFertilizationDays(index)
        }
        if (fertiBegin >= 0 && fertiEnd < calendarDays.size) {
            for (index in fertiBegin..fertiEnd)
                drawFertilizationDays(index)
        }
    }

    private fun drawAllMenstruationInCalendar(
        previousMens: Int, previousLastDay: Int, lastMens: Int,
        lastDayLastMens: Int, lastDayNextMens: Int, nextMens: Int
    ) {
        //        draw previous month menstruation
        drawMensPeriod(previousMens, previousLastDay)
        //        draw last menstruation
        drawMensPeriod(lastMens,lastDayLastMens)
        //        draw next menstruation
        if (lastDayNextMens < calendarDays.size) {
            drawMensPeriod(nextMens,lastDayNextMens)
        } else {
            drawMensPeriod(nextMens,calendarDays.size)
        }
    }

    private fun drawMensPeriod(mens: Int, lastDay: Int) {
        if (mens >= 0) {
            for (index in mens until lastDay)
                drawMenstruationDays(index)
        } else {
            for (index in 0 until lastDay)
                drawMenstruationDays(index)
        }
    }

    private fun drawOvulation(dayIndex: Int) {
        calendarDays[dayIndex].apply {
            color = getColor(context, R.color.graph2)
            textColor = getColor(context, R.color.colorOnPrimary)
        }
    }

    private fun drawFertilizationDays(dayIndex: Int) {
        calendarDays[dayIndex].apply {
            color = getColor(context, R.color.graph1)
            textColor = getColor(context, R.color.colorOnPrimary)
        }
    }

    private fun drawMenstruationDays(dayIndex: Int) {
        calendarDays[dayIndex].apply {
            color = getColor(context, R.color.graph4)
            textColor = getColor(context, R.color.colorOnPrimary)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(rectF, linePaint)
        backPaint.color = getColor(context, R.color.colorPrimaryVariant)
        canvas.drawRect(dayRectF, backPaint)
        canvas.drawText(context.getString(R.string.monday), (dividedWidth / 2), (dividedHeight / 2) + (textSize / 2), textPaint)
        canvas.drawText(context.getString(R.string.tuesday), (dividedWidth / 2 * 3), (dividedHeight / 2) + (textSize / 2), textPaint)
        canvas.drawText(context.getString(R.string.wednesday), (dividedWidth / 2 * 5), (dividedHeight / 2) + (textSize / 2), textPaint)
        canvas.drawText(context.getString(R.string.thursday), (dividedWidth / 2 * 7), (dividedHeight / 2) + (textSize / 2), textPaint)
        canvas.drawText(context.getString(R.string.friday), (dividedWidth / 2 * 9), (dividedHeight / 2) + (textSize / 2), textPaint)
        canvas.drawText(context.getString(R.string.saturday), (dividedWidth / 2 * 11), (dividedHeight / 2) + (textSize / 2), textPaint)
        canvas.drawText(context.getString(R.string.sunday), (dividedWidth / 2 * 13), (dividedHeight / 2) + (textSize / 2), textPaint)

        calendarDays.forEach {
            backPaint.color = it.color
            textPaint.color = it.textColor
            canvas.drawRect(it.rectF, backPaint)
            canvas.drawText("${it.numberDay}", it.rectF.centerX(), it.rectF.centerY() + (textSize / 2), textPaint)
        }
    }
}
