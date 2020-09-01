package com.benlefevre.monendo

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import com.benlefevre.monendo.injection.appModule
import com.benlefevre.monendo.injection.networkModule
import com.benlefevre.monendo.utils.PILL_CHANNEL
import com.benlefevre.monendo.utils.TREATMENT_CHANNEL
import com.facebook.stetho.Stetho
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class EndoApp : Application() {

    companion object {
        lateinit var INSTANCE: EndoApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        Stetho.initializeWithDefaults(INSTANCE)
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@EndoApp)
            modules(listOf(appModule, networkModule))
        }
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationPillChannel =
                NotificationChannel(PILL_CHANNEL, getString(R.string.contra_pill_notif_name), NotificationManager.IMPORTANCE_HIGH).apply {
                    enableLights(true)
                    lightColor = Color.GREEN
                    enableVibration(true)
                    lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                }
            val notificationTreatmentChannel =
                NotificationChannel(TREATMENT_CHANNEL, getString(R.string.treatment_channel_name), NotificationManager.IMPORTANCE_HIGH).apply {
                    enableLights(true)
                    lightColor = Color.GREEN
                    enableVibration(true)
                    lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                }
            manager.createNotificationChannel(notificationPillChannel)
            manager.createNotificationChannel(notificationTreatmentChannel)
        }
    }
}