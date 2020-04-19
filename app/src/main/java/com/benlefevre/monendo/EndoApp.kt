package com.benlefevre.monendo

import android.app.Application
import com.benlefevre.monendo.injection.appModule
import com.benlefevre.monendo.injection.networkModule
import com.facebook.stetho.Stetho
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class EndoApp : Application() {

    companion object{
        lateinit var INSTANCE : EndoApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        Stetho.initializeWithDefaults(INSTANCE)
        startKoin{
            androidLogger()
            androidContext(this@EndoApp)
            modules(listOf(appModule, networkModule))
        }
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
    }
}