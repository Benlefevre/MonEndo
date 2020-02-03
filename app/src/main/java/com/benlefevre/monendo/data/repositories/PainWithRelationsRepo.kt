package com.benlefevre.monendo.data.repositories

import com.benlefevre.monendo.data.dao.PainWithRelationsDao
import java.util.*

class PainWithRelationsRepo(private val painWithRelationsDao: PainWithRelationsDao) {

    fun getPainWithRelationByPeriod(dateBegin: Date, dateEnd: Date) =
        painWithRelationsDao.getAllPainsWithRelations(dateBegin, dateEnd)
}