package com.benlefevre.monendo.utils

import androidx.core.view.forEach
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Sets a checked listener on each chip to call the correct ViewModel's function
 * to fetch user's input in locale DB.
 */
fun setupChipDurationListener(chipGroup: ChipGroup, viewModel: DashboardViewModel) {
    chipGroup.forEach {
        it as Chip
        when (it.id) {
            R.id.chip_week -> {
                setupChipListenerWithViewModelFunction(it,viewModel::getPainsRelations7days)
            }
            R.id.chip_month -> {
                setupChipListenerWithViewModelFunction(it,viewModel::getPainsRelations30days)
            }
            R.id.chip_6months -> {
                setupChipListenerWithViewModelFunction(it,viewModel::getPainsRelations180days)
            }
            R.id.chip_year -> {
                setupChipListenerWithViewModelFunction(it,viewModel::getPainsRelations360days)
            }
        }
    }
}

fun setupChipListenerWithViewModelFunction(chip: Chip, func : () -> Unit){
    chip.setOnCheckedChangeListener { _, isChecked ->
        if(isChecked){
            func()
        }
    }
}