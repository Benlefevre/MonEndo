package com.benlefevre.monendo.dashboard.repository

import androidx.lifecycle.LiveData
import com.benlefevre.monendo.dashboard.dao.SymptomDao
import com.benlefevre.monendo.dashboard.models.Symptom
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