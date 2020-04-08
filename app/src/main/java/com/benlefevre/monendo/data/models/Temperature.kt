package com.benlefevre.monendo.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Temperature(
    @ColumnInfo(name = "value") var value : Float,
    @ColumnInfo(name = "date") var date: Date
) {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0
}