package com.benlefevre.monendo.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.LiveData

class ConnectivityLivedata (private val context: Context) : LiveData<Boolean>(){

    private val networkReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent?) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo

            value = networkInfo != null && networkInfo.isConnected
        }

    }

    override fun onActive() {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(networkReceiver, filter)
    }

    override fun onInactive() {
        context.unregisterReceiver(networkReceiver)
    }
}

