package com.benlefevre.monendo.dashboard.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Pain(
    @ColumnInfo(name = "date") var date: Date,
    @ColumnInfo(name = "intensity") var intensity: Int,
    @ColumnInfo(name = "location") var location: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}