package com.benlefevre.monendo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.benlefevre.monendo.data.models.UserActivities
import java.util.*

@Dao
interface UserActivitiesDao {

    @Query("SELECT * FROM UserActivities")
    fun getAllUserActivities() : LiveData<List<UserActivities>>

    @Query("SELECT * FROM UserActivities WHERE painId = :painId")
    fun getPainUserActivities(painId : Long) : LiveData<List<UserActivities>>

    @Query("SELECT * FROM UserActivities WHERE date BETWEEN :dateBegin AND :dateEnd")
    fun getUserActivitiesByPeriod(dateBegin : Date, dateEnd : Date)

    @Insert
    fun insertAll(userActivities : List<UserActivities>)

    @Query("DELETE FROM UserActivities WHERE name = :name")
    fun deleteAllUserActivities(name : String)

    @Query("DELETE FROM UserActivities WHERE name = :name")
    fun deleteAllSleepDatas(name : String)
}