package com.benlefevre.monendo.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.benlefevre.monendo.data.repositories.PainWithRelationsRepo
import java.util.*

class DashboardViewModel(private val painWithRelationsRepo: PainWithRelationsRepo) : ViewModel() {


    val date7 = with(Calendar.getInstance()){
        add(Calendar.DAY_OF_YEAR,-7)
        time
    }

    val today = Date()

    fun getPainRelationsByPeriod() =
        painWithRelationsRepo.getPainWithRelationByPeriod(date7, today)


}