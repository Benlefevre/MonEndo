package com.benlefevre.monendo.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = Pain::class, parentColumns = ["id"], childColumns = ["painId"],onDelete = CASCADE)])
data class Mood(
    @ColumnInfo(name = "painId") val painId : Long,
    @ColumnInfo(name = "value") val value : String
) {
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0
}