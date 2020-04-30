package com.benlefevre.monendo.dashboard.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = Pain::class, parentColumns = ["id"], childColumns = ["painId"],onDelete = CASCADE)])
data class Mood(
    @ColumnInfo(name = "painId",index = true) var painId : Long = 0,
    @ColumnInfo(name = "value") var value : String
) {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0
}