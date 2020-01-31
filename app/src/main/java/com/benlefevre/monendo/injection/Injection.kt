package com.benlefevre.monendo.injection

import android.content.Context
import com.benlefevre.monendo.data.database.EndoDatabase
import com.benlefevre.monendo.data.repositories.*

abstract class Injection {

    companion object {

        fun providePainDataSource(context: Context): PainRepo {
            return PainRepo(EndoDatabase.getDatabase(context).painDao())
        }

        fun provideSymptomDataSource(context: Context): SymptomRepo {
            return SymptomRepo(EndoDatabase.getDatabase(context).symptomDao())
        }

        fun provideMoodDataSource(context: Context): MoodRepo {
            return MoodRepo(EndoDatabase.getDatabase(context).moodDao())
        }

        fun provideUserActivitiesDataSource(context: Context): UserActivitiesRepo {
            return UserActivitiesRepo(EndoDatabase.getDatabase(context).userActivitiesDao())
        }

        fun provideTemperatureDataSource(context: Context): TemperatureRepo {
            return TemperatureRepo(EndoDatabase.getDatabase(context).temperatureDao())
        }

        fun providerViewModelFactory(context: Context): ViewModelFactory {
            val firestoreRepo: FirestoreRepo = FirestoreRepo.getInstance()
            val painRepo = providePainDataSource(context)
            val symptomRepo = provideSymptomDataSource(context)
            val moodRepo = provideMoodDataSource(context)
            val userActivitiesRepo = provideUserActivitiesDataSource(context)
            val temperatureRepo = provideTemperatureDataSource(context)
            return ViewModelFactory(
                firestoreRepo,
                painRepo,
                symptomRepo,
                moodRepo,
                userActivitiesRepo,
                temperatureRepo
            )
        }
    }
}