package com.benlefevre.monendo.utils

import java.text.SimpleDateFormat
import java.util.*

fun formatDate(date: Date): String {
    val dateFormat = SimpleDateFormat("dd/MM",Locale.getDefault())
    return dateFormat.format(date)
}