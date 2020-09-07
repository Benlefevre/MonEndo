package com.benlefevre.monendo.dashboard.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.repository.PainWithRelationsRepo
import kotlinx.coroutines.launch
import java.util.*

class DashboardViewModel(private val painWithRelationsRepo: PainWithRelationsRepo) : ViewModel() {

    private val _pains = MutableLiveData<List<PainWithRelations>>()

    val pains: LiveData<List<PainWithRelations>>
        get() = _pains


    val date7: Date = with(Calendar.getInstance()) {
        add(Calendar.DAY_OF_YEAR, -7)
        time
    }

    val dateMonth: Date = with(Calendar.getInstance()) {
        add(Calendar.DAY_OF_YEAR, -31)
        time
    }

    val date6Months: Date = with(Calendar.getInstance()) {
        add(Calendar.DAY_OF_YEAR, -180)
        time
    }

    val dateYear: Date = with(Calendar.getInstance()) {
        add(Calendar.DAY_OF_YEAR, -365)
        time
    }

    val today = Date()

    fun getPainRelationsBy7LastDays() =
        painWithRelationsRepo.getPainWithRelationByPeriod(date7, today)

    fun getPainRelationsByLastMonth() =
        painWithRelationsRepo.getPainWithRelationByPeriod(dateMonth, today)

    fun getPainRelationsByLast6Months() =
        painWithRelationsRepo.getPainWithRelationByPeriod(date6Months, today)

    fun getPainRelationsByLastYear() =
        painWithRelationsRepo.getPainWithRelationByPeriod(dateYear, today)

    fun getPainsRelations7days() {
        viewModelScope.launch {
            _pains.value = painWithRelationsRepo.getPainRelationByPeriod(date7, today)
        }
    }

    fun getPainsRelations30days() {
        viewModelScope.launch {
            _pains.value = painWithRelationsRepo.getPainRelationByPeriod(dateMonth, today)
        }
    }

    fun getPainsRelations180days() {
        viewModelScope.launch {
            _pains.value = painWithRelationsRepo.getPainRelationByPeriod(date6Months, today)
        }
    }

    fun getPainsRelations360days() {
        viewModelScope.launch {
            _pains.value = painWithRelationsRepo.getPainRelationByPeriod(dateYear, today)
        }
    }
}