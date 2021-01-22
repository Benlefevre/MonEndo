package com.benlefevre.monendo.treatment.models

import android.graphics.RectF

class Pill(width: Float, var multiWidth: Int, height: Float, var multiHeight: Int, var radius: Float, var color: Int) {

    var width = width
        set(value) {
            field = value
            x = value * multiWidth
        }
    var height = height
        set(value) {
            field = value
            y = value * multiHeight
        }

    var x = 0f
    var y = 0f
    var isChecked: Boolean = false
    var isClickable: Boolean = false
    var shadowRectF: RectF = RectF()

    init {
        x = width * multiWidth
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

    fun setChecked(color: Int) {
        if (!isChecked) {
            this.color = color
            isChecked = true
        } else
            isChecked = false
    }
}