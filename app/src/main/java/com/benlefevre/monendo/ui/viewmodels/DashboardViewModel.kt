package com.benlefevre.monendo.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.benlefevre.monendo.data.repositories.PainWithRelationsRepo
import java.util.*

class DashboardViewModel(private val painWithRelationsRepo: PainWithRelationsRepo) : ViewModel() {

    fun getPainRelationsByPeriod(dateBegin: Date, dateEnd: Date) =
        painWithRelationsRepo.getPainWithRelationByPeriod(dateBegin, dateEnd)

}