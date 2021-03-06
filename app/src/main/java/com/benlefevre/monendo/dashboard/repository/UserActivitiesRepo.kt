package com.benlefevre.monendo.dashboard.repository

import androidx.lifecycle.LiveData
import com.benlefevre.monendo.dashboard.dao.UserActivitiesDao
import com.benlefevre.monendo.dashboard.models.UserActivities
import java.util.*

class UserActivitiesRepo(val userActivitiesDao: UserActivitiesDao) {

    val userActivities : LiveData<List<UserActivities>> = userActivitiesDao.getAllUserActivities()

    fun getPainUserActivities(painId:Long) : LiveData<List<UserActivities>>{
        return userActivitiesDao.getPainUserActivities(painId)
    }

    fun getUserActivitiesByPeriod(dateBegin : Date, dateEnd : Date) : LiveData<List<UserActivities>>{
        return userActivitiesDao.getUserActivitiesByPeriod(dateBegin, dateEnd)
    }

    suspend fun insertAllUserActivities(userActivities : List<UserActivities>) = userActivitiesDao.insertAll(userActivities)

    suspend fun deleteAllUserActivities(name : String) = userActivitiesDao.deleteAllUserActivities(name)

    suspend fun deleteAllSleepDatas(name : String) = userActivitiesDao.deleteAllSleepDatas(name)
}