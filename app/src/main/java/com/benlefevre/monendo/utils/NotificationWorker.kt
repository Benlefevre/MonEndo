package com.benlefevre.monendo.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.benlefevre.monendo.R

class NotificationWorker(private var context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    override fun doWork(): Result {
        val data = inputData
        return when{
            !data.getString(TREATMENT).isNullOrBlank() && data.getString(TREATMENT) == PILL_TAG -> {
                sendPillNotification()
                Result.success()
            }
            else -> Result.failure()
        }
    }

    fun sendPillNotification() {
        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.treatmentFragment)
            .createPendingIntent()

        val channelId = context.getString(R.string.contra_pill_notif_id)
        val channelName = context.getString(R.string.contra_pill_notif_name)
        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(channelId,channelName,NotificationManager.IMPORTANCE_HIGH)
            notifManager.createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(context,channelId).apply {
            setContentTitle(context.getString(R.string.dont_forget_pill))
            setContentText(context.getString(R.string.check_pill))
            setSmallIcon(R.drawable.ic_launcher_background)
            setAutoCancel(false)
            setContentIntent(pendingIntent)
        }
        notifManager.notify(1,notification.build())
    }
}