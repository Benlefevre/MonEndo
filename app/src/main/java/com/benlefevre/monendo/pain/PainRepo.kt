package com.benlefevre.monendo.pain

import androidx.lifecycle.LiveData
import com.benlefevre.monendo.dashboard.dao.PainDao
import com.benlefevre.monendo.dashboard.models.Pain
import java.util.*

class PainRepo(private val painDao: PainDao) {

    val pains: LiveData<List<Pain>> = painDao.getAllPains()

    fun getPainsByPeriod(dateBegin: Date, dateEnd: Date): LiveData<List<Pain>> =
        painDao.getPainByPeriod(dateBegin, dateEnd)

    suspend fun insertPain(pain : Pain) : Long = painDao.insertPain(pain)

    suspend fun deleteAllPains() = painDao.deleteAllPain()
}