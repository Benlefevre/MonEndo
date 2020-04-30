package com.benlefevre.monendo.dashboard.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.benlefevre.monendo.dashboard.models.Pain
import java.util.*

@Dao
interface PainDao {

    @Query("SELECT * FROM Pain")
    fun getAllPains(): LiveData<List<Pain>>

    @Query("SELECT * FROM Pain WHERE date BETWEEN :dateBegin AND :dateEnd")
    fun getPainByPeriod(dateBegin : Date, dateEnd : Date) : LiveData<List<Pain>>

    @Insert
    suspend fun insertPain(pain: Pain) : Long

    @Query("DELETE FROM Pain")
    suspend fun deleteAllPain()
}