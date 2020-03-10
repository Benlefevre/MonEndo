package com.benlefevre.monendo.utils

import java.text.SimpleDateFormat
import java.util.*

fun formatDateWithoutYear(date: Date): String {
    val dateFormat = SimpleDateFormat("dd/MM",Locale.getDefault())
    return dateFormat.format(date)
}

fun formatDateWithYear(date: Date) : String{
    val dateFormat = SimpleDateFormat("dd/MM/yy",Locale.getDefault())
    return dateFormat.format(date)
}

fun parseStringInDate(string: String) : Date{
    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    return dateFormat.parse(string) ?: Date(-1L)
}