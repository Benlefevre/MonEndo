package com.benlefevre.monendo.injection

import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.benlefevre.monendo.dashboard.repository.MoodRepo
import com.benlefevre.monendo.dashboard.repository.PainWithRelationsRepo
import com.benlefevre.monendo.dashboard.repository.SymptomRepo
import com.benlefevre.monendo.dashboard.repository.UserActivitiesRepo
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.database.EndoDatabase
import com.benlefevre.monendo.doctor.api.CpamService
import com.benlefevre.monendo.doctor.api.DoctorRepository
import com.benlefevre.monendo.doctor.repository.CommentaryRepository
import com.benlefevre.monendo.doctor.viewmodel.DoctorViewModel
import com.benlefevre.monendo.fertility.FertilityViewModel
import com.benlefevre.monendo.fertility.temperature.TemperatureRepo
import com.benlefevre.monendo.login.LoginActivityViewModel
import com.benlefevre.monendo.login.UserRepo
import com.benlefevre.monendo.pain.PainFragmentViewModel
import com.benlefevre.monendo.pain.PainRepo
import com.benlefevre.monendo.settings.SettingViewModel
import com.benlefevre.monendo.utils.API_URL
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {

    single {
        Room.databaseBuilder(androidApplication(), EndoDatabase::class.java, "endo_db")
            .build()
    }
    single { Firebase.firestore }
    single { PainRepo(get<EndoDatabase>().painDao()) }
    single { SymptomRepo(get<EndoDatabase>().symptomDao()) }
    single { UserActivitiesRepo(get<EndoDatabase>().userActivitiesDao()) }
    single { MoodRepo(get<EndoDatabase>().moodDao()) }
    single { TemperatureRepo(get<EndoDatabase>().temperatureDao()) }
    single {
        PainWithRelationsRepo(
            get<EndoDatabase>().painRelationDao()
        )
    }
    single { UserRepo(get()) }
    viewModel {
        DashboardViewModel(
            get()
        )
    }
    viewModel { LoginActivityViewModel(get()) }
    viewModel {
        PainFragmentViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { FertilityViewModel(get()) }
    viewModel { SettingViewModel(get(), get(),get()) }
}

val networkModule = module {

    factory { provideHttpClient() }
    factory { provideCpamApi(get()) }
    single { provideRetrofit(get()) }
    single { CommentaryRepository(get()) }
    factory { DoctorRepository(get()) }
    viewModel { (handle: SavedStateHandle) -> DoctorViewModel(handle, get(), get()) }
}

fun provideCpamApi(retrofit: Retrofit): CpamService = retrofit.create(
    CpamService::class.java
)

fun provideRetrofit(client: OkHttpClient): Retrofit {
    return Retrofit.Builder().baseUrl(API_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun provideHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()
}

