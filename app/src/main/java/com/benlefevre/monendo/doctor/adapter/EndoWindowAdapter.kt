package com.benlefevre.monendo.doctor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.benlefevre.monendo.databinding.InfoWindowEndoBinding
import com.benlefevre.monendo.doctor.models.Doctor
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import java.util.*

class EndoWindowAdapter(context: Context) : GoogleMap.InfoWindowAdapter {

    private val binding = InfoWindowEndoBinding.inflate(LayoutInflater.from(context),null,false)

    override fun getInfoContents(marker : Marker): View {
        val doctor = marker.tag as Doctor

        with(binding){
            infoDoctorName.text = doctor.name
            infoDoctorSpec.text = doctor.spec.toUpperCase(Locale.getDefault())
            infoDoctorAddress.text = doctor.address
        }
        return binding.root
    }

    override fun getInfoWindow(p0: Marker?): View? = null
}