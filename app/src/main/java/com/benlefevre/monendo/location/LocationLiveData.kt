package com.benlefevre.monendo.location

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.location.LocationServices

data class LocationData(val location: Location? = null, val exception: Exception? = null)

class LocationLiveData(context: Context) : LiveData<LocationData>() {

    private val appContext = context.applicationContext
    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(appContext)

    override fun onActive() {
        super.onActive()
        requestLastLocation()
    }

    fun requestLastLocation() {
        try{
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                value = LocationData(location = it)
            }
            fusedLocationProviderClient.lastLocation.addOnFailureListener {
                value = LocationData(exception = it)
            }
        }catch (e : SecurityException){
            value = LocationData(exception = e)
        }
    }
}