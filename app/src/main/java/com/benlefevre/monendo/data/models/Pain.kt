package com.benlefevre.monendo.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Pain(
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "intensity") val intensity: Int,
    @ColumnInfo(name = "location") val location: String
) {
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
}