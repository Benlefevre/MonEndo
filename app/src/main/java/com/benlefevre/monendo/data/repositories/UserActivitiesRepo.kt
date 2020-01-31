package com.benlefevre.monendo.data.repositories

import androidx.lifecycle.LiveData
import com.benlefevre.monendo.data.dao.UserActivitiesDao
import com.benlefevre.monendo.data.models.UserActivities
import java.util.*

class UserActivitiesRepo(val userActivitiesDao: UserActivitiesDao) {

    val userActivities : LiveData<List<UserActivities>> = userActivitiesDao.getAllUserActivities()

    fun getPainUserActivities(painId:Long) : LiveData<List<UserActivities>>{
        return userActivitiesDao.getPainUserActivities(painId)
    }

    fun getUserActivitiesByPeriod(dateBegin : Date, dateEnd : Date) : LiveData<List<UserActivities>>{
        return userActivitiesDao.getUserActivitiesByPeriod(dateBegin, dateEnd)
    }

    fun insertAllUserActivities(userActivities : List<UserActivities>) = userActivitiesDao.insertAll(userActivities)

    fun deleteAllUserActivities(name : String) = userActivitiesDao.deleteAllUserActivities(name)

    fun deleteAllSleepDatas(name : String) = userActivitiesDao.deleteAllSleepDatas(name)
}