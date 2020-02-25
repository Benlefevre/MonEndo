package com.benlefevre.monendo.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.benlefevre.monendo.data.repositories.PainWithRelationsRepo
import java.util.*

class DashboardViewModel(private val painWithRelationsRepo: PainWithRelationsRepo) : ViewModel() {


    val date7: Date = with(Calendar.getInstance()){
        add(Calendar.DAY_OF_YEAR,-7)
        time
    }

    val dateMonth: Date = with(Calendar.getInstance()){
        add(Calendar.DAY_OF_YEAR,-31)
        time
    }

    val date6Months: Date = with(Calendar.getInstance()){
        add(Calendar.DAY_OF_YEAR,-180)
        time
    }

    val dateYear: Date = with(Calendar.getInstance()){
        add(Calendar.DAY_OF_YEAR, -365)
        time
    }

    val today = Date()

    fun getPainRelationsBy7LastDays() =
        painWithRelationsRepo.getPainWithRelationByPeriod(date7, today)

    fun getPainRelationsByLastMonth() =
        painWithRelationsRepo.getPainWithRelationByPeriod(dateMonth,today)

    fun getPainRelationsByLast6Months() =
        painWithRelationsRepo.getPainWithRelationByPeriod(date6Months,today)

    fun getPainRelationsByLastYear() =
        painWithRelationsRepo.getPainWithRelationByPeriod(dateYear,today)
}