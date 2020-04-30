package com.benlefevre.monendo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.benlefevre.monendo.dashboard.dao.*
import com.benlefevre.monendo.dashboard.models.Mood
import com.benlefevre.monendo.dashboard.models.Pain
import com.benlefevre.monendo.dashboard.models.Symptom
import com.benlefevre.monendo.dashboard.models.UserActivities
import com.benlefevre.monendo.fertility.models.Temperature
import com.benlefevre.monendo.fertility.temperature.TemperatureDao
import com.benlefevre.monendo.utils.Converters

@Database(entities = [Pain::class, Symptom::class, UserActivities::class, Mood::class, Temperature::class],version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class EndoDatabase : RoomDatabase() {

    abstract fun painDao() : PainDao
    abstract fun symptomDao() : SymptomDao
    abstract fun moodDao() : MoodDao
    abstract fun userActivitiesDao() : UserActivitiesDao
    abstract fun temperatureDao() : TemperatureDao
    abstract fun painRelationDao() : PainWithRelationsDao

    companion object{
        @Volatile
        private var INSTANCE : EndoDatabase? = null

        fun getDatabase(context: Context) : EndoDatabase {
            val tempInstance =
                INSTANCE
            if (tempInstance != null)
                return tempInstance
            synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext,
                    EndoDatabase::class.java,
                    "endo_db")
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}