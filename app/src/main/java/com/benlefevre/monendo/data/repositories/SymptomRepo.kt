package com.benlefevre.monendo.data.repositories

import androidx.lifecycle.LiveData
import com.benlefevre.monendo.data.dao.SymptomDao
import com.benlefevre.monendo.data.models.Symptom
import java.util.*

class SymptomRepo(val symptomDao: SymptomDao) {

    val symptoms : LiveData<List<Symptom>> = symptomDao.getAllSymptoms()

    fun getSymptomsByPeriod(dateBegin : Date,dateEnd : Date) : LiveData<List<Symptom>>{
        return symptomDao.getSymptomsByPeriod(dateBegin, dateEnd)
    }

    fun getPainSymptoms(painId : Long) : LiveData<List<Symptom>>{
        return symptomDao.getPainSymptoms(painId)
    }

    suspend fun insertAllSymptoms(symptoms : List<Symptom>) = symptomDao.insertAll(symptoms)

    suspend fun deleteAllSymptoms() = symptomDao.deleteAllSymptoms()
}