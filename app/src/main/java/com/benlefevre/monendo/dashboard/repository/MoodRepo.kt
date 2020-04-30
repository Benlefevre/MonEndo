package com.benlefevre.monendo.dashboard.repository

import androidx.lifecycle.LiveData
import com.benlefevre.monendo.dashboard.dao.MoodDao
import com.benlefevre.monendo.dashboard.models.Mood

class MoodRepo(private val moodDao: MoodDao) {

    val moods : LiveData<List<Mood>> = moodDao.getAllMoods()

    fun getPainMood(painId : Long) : LiveData<Mood> {
        return moodDao.getPainMood(painId)
    }

    suspend fun insertMood(mood: Mood) = moodDao.insert(mood)

    suspend fun deleteAllMoods() = moodDao.deleteAllMoods()
}