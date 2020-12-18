package com.benlefevre.monendo.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import timber.log.Timber

class ConnectivityLiveData(private val context: Context) : LiveData<Boolean>(),
    ConnectivityListener {

    private var networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    private val callback = ConnectivityCallback(this)

    private fun isConnected() {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, callback)
    }

    override fun onActive() {
        isConnected()
    }

    override fun onInactive() {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(callback)
    }

    override fun onStateChange(isConnected: Boolean) {
        postValue(isConnected)
        Timber.i("isConnected : $isConnected")
    }
}

class ConnectivityCallback(private val listener: ConnectivityListener) :
    ConnectivityManager.NetworkCallback() {

    var nbConnection = 0

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        nbConnection++
        listener.onStateChange(true)
    }

    override fun onUnavailable() {
        super.onUnavailable()
        nbConnection = (nbConnection - 1).coerceAtLeast(0)
        listener.onStateChange(nbConnection >= 1)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        nbConnection = (nbConnection - 1).coerceAtLeast(0)
        listener.onStateChange(nbConnection >= 1)
    }
}

interface ConnectivityListener {
    fun onStateChange(isConnected: Boolean)
}

