package com.benlefevre.monendo.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import java.util.*

@Entity(foreignKeys = [ForeignKey(entity = Pain::class,parentColumns = ["id"], childColumns = ["painId"], onDelete = CASCADE)])
data class Symptom (
    @ColumnInfo(name = "painId") val painId : Long,
    @ColumnInfo(name = "name") val name : String,
    @ColumnInfo(name = "date") val date : Date
) {
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0
}