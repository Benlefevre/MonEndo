package com.benlefevre.monendo.dashboard.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import java.util.*

@Entity(foreignKeys = [ForeignKey(entity = Pain::class,parentColumns = ["id"],childColumns = ["painId"],onDelete = CASCADE)])
data class UserActivities(
    @ColumnInfo(name = "painId", index = true) var painId : Long,
    @ColumnInfo(name = "name") var name : String,
    @ColumnInfo(name = "duration") var duration : Int,
    @ColumnInfo(name = "intensity") var intensity : Int,
    @ColumnInfo(name = "painValue") var painValue : Int,
    @ColumnInfo(name = "date") var date : Date
) {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0
}