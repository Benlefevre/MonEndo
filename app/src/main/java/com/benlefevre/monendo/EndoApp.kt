package com.benlefevre.monendo

import android.app.Application
import com.facebook.stetho.Stetho

class EndoApp : Application() {

    companion object{
        lateinit var INSTANCE : EndoApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        Stetho.initializeWithDefaults(INSTANCE)
    }
}