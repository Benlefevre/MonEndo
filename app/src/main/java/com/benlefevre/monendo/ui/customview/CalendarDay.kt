package com.benlefevre.monendo.ui.customview

import android.graphics.RectF
import com.benlefevre.monendo.R

data class CalendarDay(var width: Float, var multiWidth: Int, var height: Float, var multiHeight: Int, var numberDay: Int = 0) {

    var textColor: Int = R.color.colorPrimary
    var color: Int = R.color.colorOnPrimary
    var rectF: RectF =
        RectF(width * (multiWidth - 1), height * multiHeight, width * multiWidth, height * (multiHeight + 1))
}