package com.benlefevre.monendo.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import java.util.*

@Entity(foreignKeys = [ForeignKey(entity = Pain::class,parentColumns = ["id"], childColumns = ["painId"], onDelete = CASCADE)])
data class Symptom (
    @ColumnInfo(name = "painId", index = true) var painId : Long = 0,
    @ColumnInfo(name = "name") var name : String,
    @ColumnInfo(name = "date") var date : Date
) {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0
}