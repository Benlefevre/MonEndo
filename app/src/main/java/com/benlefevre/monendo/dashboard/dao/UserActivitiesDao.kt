package com.benlefevre.monendo.dashboard.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.benlefevre.monendo.dashboard.models.UserActivities
import java.util.*

@Dao
interface UserActivitiesDao {

    @Query("SELECT * FROM UserActivities")
    fun getAllUserActivities() : LiveData<List<UserActivities>>

    @Query("SELECT * FROM UserActivities WHERE painId = :painId")
    fun getPainUserActivities(painId : Long) : LiveData<List<UserActivities>>

    @Query("SELECT * FROM UserActivities WHERE date BETWEEN :dateBegin AND :dateEnd")
    fun getUserActivitiesByPeriod(dateBegin : Date, dateEnd : Date) : LiveData<List<UserActivities>>

    @Insert
    suspend fun insertAll(userActivities : List<UserActivities>)

    @Query("DELETE FROM UserActivities WHERE NOT name = :name")
    suspend fun deleteAllUserActivities(name : String)

    @Query("DELETE FROM UserActivities WHERE name = :name")
    suspend fun deleteAllSleepDatas(name : String)
}