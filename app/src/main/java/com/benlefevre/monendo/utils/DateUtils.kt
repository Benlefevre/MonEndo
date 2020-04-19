package com.benlefevre.monendo.utils

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

fun setRepeatHour(date: Date): String {
    val repeatHour = with(Calendar.getInstance()) {
        time = date
        add(Calendar.MINUTE, 60)
        time
    }
    return formatTime(repeatHour)
}