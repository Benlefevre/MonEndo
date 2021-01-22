package com.benlefevre.monendo.utils

import android.app.DatePickerDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

fun formatDateWithoutYear(date: Date): String {
    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    return dateFormat.format(date)
}

fun formatDateWithYear(date: Date): String {
    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    return dateFormat.format(date)
}

fun formatDateToDayName(date: Date): String {
    val dateFormat = SimpleDateFormat("E", Locale.US)
    return dateFormat.format(date)
}

fun parseStringInDate(string: String): Date {
    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    return dateFormat.parse(string) ?: Date(-1L)
}

fun formatTime(date: Date): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(date)
}

fun parseStringInTime(string: String) : Date{
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.parse(string)?: Date(-1L)
}

fun setRepeatHour(date: Date): String {
    val repeatHour = with(Calendar.getInstance()) {
        time = date
        add(Calendar.MINUTE, 60)
        time
    }
    return formatTime(repeatHour)
}

/**
 * Sets a OnDateSetListener with a call to the function passed in parameter and shows a DatePickerDialog
 */
fun openDatePicker(context: Context?, calendar : Calendar, func: () -> Unit) {
    val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        calendar.apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        func()
    }
    context?.let {
        DatePickerDialog(
            it, dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = Calendar.getInstance().timeInMillis
            show()
        }
    }
}