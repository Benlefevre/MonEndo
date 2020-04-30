package com.benlefevre.monendo.dashboard.repository

import com.benlefevre.monendo.dashboard.dao.PainWithRelationsDao
import java.util.*

class PainWithRelationsRepo(private val painWithRelationsDao: PainWithRelationsDao) {

    fun getPainWithRelationByPeriod(dateBegin: Date, dateEnd: Date) =
        painWithRelationsDao.getAllPainsWithRelations(dateBegin, dateEnd)
}