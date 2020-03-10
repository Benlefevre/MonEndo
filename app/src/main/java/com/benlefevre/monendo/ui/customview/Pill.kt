package com.benlefevre.monendo.ui.customview

import android.graphics.RectF

data class Pill(var width: Float, var multiWith: Int, var heigth: Float, var multiHeight: Int, var radius: Float, var color: Int) {

    var x = 0f
    var y = 0f
    var isChecked: Boolean = false
    var isClickable: Boolean = true
    var shadowRectF: RectF = RectF()

    init {
        x = width * multiWith
        y = heigth * multiHeight
        shadowRectF =
            RectF(x - radius / 1.2f, y - radius / 1.2f, x + radius / 1.2f, y + radius / 1.2f)
    }

    fun checkTouchInCircle(touchX: Float, touchY: Float): Boolean {
        return (touchX < x + radius
                && touchX > x - radius
                && touchY < y + radius
                && touchY > y - radius
                && isClickable)
    }

    fun setShadow() {
        shadowRectF =
            RectF(x - radius / 1.2f, y - radius / 1.2f, x + radius / 1.2f, y + radius / 1.2f)
    }
}