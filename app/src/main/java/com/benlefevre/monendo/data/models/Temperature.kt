package com.benlefevre.monendo.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Temperature(
    @ColumnInfo(name = "value") val value : Double,
    @ColumnInfo(name = "date") val date: Date
) {
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0
}