package com.benlefevre.monendo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.benlefevre.monendo.data.models.Symptom
import java.util.*

@Dao
interface SymptomDao {

    @Query("SELECT * FROM Symptom")
    fun getAllSymptoms(): LiveData<List<Symptom>>

    @Query("SELECT * FROM Symptom WHERE painId = :painId")
    fun getPainSymptoms(painId : Long) : LiveData<List<Symptom>>

    @Query("SELECT * FROM Symptom WHERE date BETWEEN :dateBegin AND :dateEnd")
    fun getSymptomsByPeriod(dateBegin : Date, dateEnd : Date) : LiveData<List<Symptom>>

    @Insert
    suspend fun insertAll(symptoms : List<Symptom>)

    @Query("DELETE FROM Symptom")
    suspend fun deleteAllSymptoms()
}