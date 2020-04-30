package com.benlefevre.monendo.fertility.temperature

import androidx.lifecycle.LiveData
import com.benlefevre.monendo.fertility.models.Temperature
import java.util.*

class TemperatureRepo(private val temperatureDao: TemperatureDao) {

    val temperatures : LiveData<List<Temperature>> = temperatureDao.getAllTemperatures()

    fun getTemperaturesByPeriod(dateBegin : Date, dateEnd : Date) : LiveData<List<Temperature>>{
        return temperatureDao.getTemperaturesByPeriod(dateBegin, dateEnd)
    }

    suspend fun insertTemperature(temperature: Temperature) = temperatureDao.insert(temperature)

    suspend fun deleteAllTemperatures() = temperatureDao.deleteAllTemperatures()
}