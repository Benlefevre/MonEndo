package com.benlefevre.monendo.injection

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.benlefevre.monendo.dashboard.repository.MoodRepo
import com.benlefevre.monendo.dashboard.repository.PainWithRelationsRepo
import com.benlefevre.monendo.dashboard.repository.SymptomRepo
import com.benlefevre.monendo.dashboard.repository.UserActivitiesRepo
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.database.EndoDatabase
import com.benlefevre.monendo.doctor.api.AdresseService
import com.benlefevre.monendo.doctor.api.CpamService
import com.benlefevre.monendo.doctor.repository.AdresseRepository
import com.benlefevre.monendo.doctor.repository.CommentaryRepository
import com.benlefevre.monendo.doctor.repository.DoctorRepository
import com.benlefevre.monendo.doctor.viewmodel.DoctorViewModel
import com.benlefevre.monendo.fertility.FertilityViewModel
import com.benlefevre.monendo.fertility.temperature.TemperatureRepo
import com.benlefevre.monendo.login.LoginActivityViewModel
import com.benlefevre.monendo.login.UserRepo
import com.benlefevre.monendo.pain.PainFragmentViewModel
import com.benlefevre.monendo.pain.PainRepo
import com.benlefevre.monendo.settings.SettingViewModel
import com.benlefevre.monendo.utils.API_URL_ADRESSE
import com.benlefevre.monendo.utils.API_URL_CPAM
import com.benlefevre.monendo.utils.PREFERENCES
import com.facebook.stetho.okhttp3.StethoInterceptor
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
    single { provideSharedPreferences(androidApplication()) }
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
    viewModel { FertilityViewModel(get(), get()) }
    viewModel { SettingViewModel(get(), get(), get()) }
}

val networkModule = module {

    factory { provideHttpClient() }
    factory { provideAdresseApi(provideRetrofit(get(), API_URL_ADRESSE)) }
    factory { provideCpamApi(provideRetrofit(get(), API_URL_CPAM)) }
    single { AdresseRepository(get()) }
    single { CommentaryRepository(get()) }
    factory { DoctorRepository(get()) }
    viewModel { (handle: SavedStateHandle) -> DoctorViewModel(handle, get(), get(),get()) }
}

fun provideCpamApi(retrofit: Retrofit): CpamService = retrofit.create(
    CpamService::class.java
)

fun provideAdresseApi(retrofit: Retrofit) : AdresseService = retrofit.create(
    AdresseService::class.java
)

fun provideRetrofit(client: OkHttpClient, url: String): Retrofit {
    return Retrofit.Builder().baseUrl(url)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun provideHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addNetworkInterceptor(StethoInterceptor())
        .build()
}

fun provideSharedPreferences(app: Application): SharedPreferences {
    return app.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
}

