package com.benlefevre.monendo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.benlefevre.monendo.data.models.Temperature
import java.util.*

@Dao
interface TemperatureDao {

    @Query("SELECT * FROM Temperature")
    fun getAllTemperatures() : LiveData<List<Temperature>>

    @Query("SELECT * FROM Temperature WHERE date BETWEEN :dateBegin AND :dateEnd")
    fun getTemperaturesByPeriod(dateBegin : Date, dateEnd : Date) : LiveData<List<Temperature>>

    @Insert
    suspend fun insert(temperature: Temperature)

    @Query("DELETE FROM Temperature")
    suspend fun deleteAllTemperatures()
}