package com.benlefevre.monendo.dashboard.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.benlefevre.monendo.dashboard.models.Mood

@Dao
interface MoodDao {

    @Query("SELECT * FROM Mood")
    fun getAllMoods() : LiveData<List<Mood>>

    @Query("SELECT * FROM Mood WHERE painId = :painId")
    fun getPainMood(painId : Long) : LiveData<Mood>

    @Insert
    suspend fun insert(mood: Mood)

    @Query("DELETE FROM Mood")
    suspend fun deleteAllMoods()
}