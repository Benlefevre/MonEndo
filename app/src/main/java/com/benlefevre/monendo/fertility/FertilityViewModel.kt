package com.benlefevre.monendo.fertility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benlefevre.monendo.fertility.models.Temperature
import com.benlefevre.monendo.fertility.temperature.TemperatureRepo
import kotlinx.coroutines.launch
import java.util.*

class FertilityViewModel(private val temperatureRepo: TemperatureRepo) : ViewModel() {

    fun createTemp(temperature: Temperature) = viewModelScope.launch {
        temperatureRepo.insertTemperature(temperature)
    }

    fun getAllTemperatures() = temperatureRepo.temperatures

    fun getTemperaturesByPeriod(begin: Date, end: Date) = temperatureRepo.getTemperaturesByPeriod(begin, end)

    fun deleteAllTemperatures() = viewModelScope.launch {
        temperatureRepo.deleteAllTemperatures()
    }
}