package com.benlefevre.monendo.injection

import androidx.room.Room
import com.benlefevre.monendo.data.database.EndoDatabase
import com.benlefevre.monendo.data.repositories.*
import com.benlefevre.monendo.ui.viewmodels.DashboardViewModel
import com.benlefevre.monendo.ui.viewmodels.LoginActivityViewModel
import com.benlefevre.monendo.ui.viewmodels.PainFragmentViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { Room.databaseBuilder(androidApplication(), EndoDatabase::class.java, "endo_db").build() }
    single { PainRepo(get<EndoDatabase>().painDao()) }
    single { SymptomRepo(get<EndoDatabase>().symptomDao() )}
    single { UserActivitiesRepo(get<EndoDatabase>().userActivitiesDao()) }
    single { MoodRepo(get<EndoDatabase>().moodDao()) }
    single { TemperatureRepo(get<EndoDatabase>().temperatureDao()) }
    single { PainWithRelationsRepo(get<EndoDatabase>().painRelationDao()) }
    single { FirestoreRepo }
    viewModel { DashboardViewModel(get()) }
    viewModel { LoginActivityViewModel(get()) }
    viewModel { PainFragmentViewModel(get(),get(),get(),get()) }
}