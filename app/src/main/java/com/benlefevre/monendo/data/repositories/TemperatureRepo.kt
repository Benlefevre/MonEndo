package com.benlefevre.monendo.data.repositories

import androidx.lifecycle.LiveData
import com.benlefevre.monendo.data.dao.TemperatureDao
import com.benlefevre.monendo.data.models.Temperature
import java.util.*

class TemperatureRepo(private val temperatureDao: TemperatureDao) {

    val temperatures : LiveData<List<Temperature>> = temperatureDao.getAllTemperatures()

    fun getTemperaturesByPeriod(dateBegin : Date, dateEnd : Date) : LiveData<List<Temperature>>{
        return temperatureDao.getTemperaturesByPeriod(dateBegin, dateEnd)
    }

    fun insertTemperature(temperature: Temperature) = temperatureDao.insert(temperature)

    fun deleteAllTemperatures() = temperatureDao.deleteAllTemperatures()
}