package com.benlefevre.monendo.doctor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.benlefevre.monendo.R
import com.benlefevre.monendo.doctor.models.Doctor
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.info_window_endo.view.*
import java.util.*

class EndoWindowAdapter(context: Context) : GoogleMap.InfoWindowAdapter {

    private val contents : View = LayoutInflater.from(context).inflate(R.layout.info_window_endo,null)

    override fun getInfoContents(marker : Marker): View {
        val doctor = marker.tag as Doctor

        with(contents){
            info_doctor_name.text = doctor.name
            info_doctor_spec.text = doctor.spec.toUpperCase(Locale.getDefault())
            info_doctor_address.text = doctor.address
        }
        return contents
    }

    override fun getInfoWindow(p0: Marker?): View? = null
}