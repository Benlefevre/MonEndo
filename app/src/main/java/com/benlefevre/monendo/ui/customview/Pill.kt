package com.benlefevre.monendo.ui.customview

import android.graphics.RectF

class Pill(width: Float, var multiWith: Int, height: Float, var multiHeight: Int, var radius: Float, var color: Int) {

    var width = width
        set(value) {
            field = value
            x = value * multiWith
        }
    var height = height
        set(value) {
            field = value
            y = value * multiHeight
        }

    var x = 0f
    var y = 0f
    var isChecked: Boolean = false
    var isClickable: Boolean = true
    var shadowRectF: RectF = RectF()

    init {
        x = width * multiWith
        y = height * multiHeight
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