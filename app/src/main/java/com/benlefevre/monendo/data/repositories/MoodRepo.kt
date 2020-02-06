package com.benlefevre.monendo.data.repositories

import androidx.lifecycle.LiveData
import com.benlefevre.monendo.data.dao.MoodDao
import com.benlefevre.monendo.data.models.Mood

class MoodRepo(private val moodDao: MoodDao) {

    val moods : LiveData<List<Mood>> = moodDao.getAllMoods()

    fun getPainMood(painId : Long) : LiveData<Mood> {
        return moodDao.getPainMood(painId)
    }

    suspend fun insertMood(mood: Mood) = moodDao.insert(mood)

    suspend fun deleteAllMoods() = moodDao.deleteAllMoods()
}