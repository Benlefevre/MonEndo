package com.benlefevre.monendo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.benlefevre.monendo.data.models.Pain
import java.util.*

@Dao
interface PainDao {

    @Query("SELECT * FROM Pain")
    fun getAllPains(): LiveData<List<Pain>>

    @Query("SELECT * FROM Pain WHERE date BETWEEN :dateBegin AND :dateEnd")
    fun getPainByPeriod(dateBegin : Date, dateEnd : Date) : LiveData<List<Pain>>

    @Insert
    fun insertPain(pain: Pain)

    @Query("DELETE FROM Pain")
    fun deleteAllPain()
}