package com.benlefevre.monendo.fertility

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benlefevre.monendo.fertility.models.Temperature
import com.benlefevre.monendo.fertility.temperature.TemperatureRepo
import com.benlefevre.monendo.utils.CURRENT_MENS
import com.benlefevre.monendo.utils.DURATION
import com.benlefevre.monendo.utils.NEXT_MENS
import com.benlefevre.monendo.utils.parseStringInDate
import kotlinx.coroutines.launch
import java.util.*

class FertilityViewModel(private val temperatureRepo: TemperatureRepo, private val preferences: SharedPreferences) : ViewModel() {

    fun createTemp(temperature: Temperature) = viewModelScope.launch {
        temperatureRepo.insertTemperature(temperature)
    }

    fun getAllTemperatures() = temperatureRepo.temperatures

    fun getTemperaturesByPeriod(begin: Date, end: Date) = temperatureRepo.getTemperaturesByPeriod(begin, end)

    fun deleteAllTemperatures() = viewModelScope.launch {
        temperatureRepo.deleteAllTemperatures()
    }

    fun getCorrectUserInput(): List<String> {
        val nextMonthValue = preferences.getString(NEXT_MENS, null)
        val nextLabelDate = nextMonthValue?.let { parseStringInDate(it) }
        val nextMonth = Calendar.getInstance().apply {
            if (nextLabelDate != null && nextLabelDate != Date(-1L)) {
                time = nextLabelDate
            }
        }
        var mensDay = ""
        var duration = ""
        if (Calendar.getInstance().after(nextMonth)) {
            nextMonthValue?.let {
                mensDay = it
            }
            preferences.edit().putString(CURRENT_MENS, nextMonthValue).apply()
        } else {
            preferences.getString(CURRENT_MENS, null)?.let {
                mensDay = it
            }
        }

        preferences.getString(DURATION, null)?.let {
            duration = it
        }
        return listOf(mensDay, duration)
    }
}