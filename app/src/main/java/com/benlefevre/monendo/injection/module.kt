package com.benlefevre.monendo.injection

import androidx.room.Room
import com.benlefevre.monendo.api.CpamService
import com.benlefevre.monendo.api.DoctorRepository
import com.benlefevre.monendo.data.database.EndoDatabase
import com.benlefevre.monendo.data.repositories.*
import com.benlefevre.monendo.ui.viewmodels.*
import com.benlefevre.monendo.utils.API_URL
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
    single { PainRepo(get<EndoDatabase>().painDao()) }
    single { SymptomRepo(get<EndoDatabase>().symptomDao()) }
    single { UserActivitiesRepo(get<EndoDatabase>().userActivitiesDao()) }
    single { MoodRepo(get<EndoDatabase>().moodDao()) }
    single { TemperatureRepo(get<EndoDatabase>().temperatureDao()) }
    single { PainWithRelationsRepo(get<EndoDatabase>().painRelationDao()) }
    single { FirestoreRepo }
    viewModel { DashboardViewModel(get()) }
    viewModel { LoginActivityViewModel(get()) }
    viewModel { PainFragmentViewModel(get(), get(), get(), get()) }
    viewModel { FertilityViewModel(get()) }
}

val networkModule = module {

    factory { provideHttpClient() }
    factory { provideCpamApi(get()) }
    single { provideRetrofit(get()) }
    factory { DoctorRepository(get()) }
    viewModel { DoctorViewModel(get()) }
}

fun provideCpamApi(retrofit: Retrofit): CpamService = retrofit.create(CpamService::class.java)

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

