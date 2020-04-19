package com.benlefevre.monendo.ui.controllers.fragments

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.Doctor
import com.benlefevre.monendo.ui.viewmodels.DoctorViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.fragment_doctor.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class DoctorFragment : Fragment(R.layout.fragment_doctor),OnMapReadyCallback {

    private lateinit var mapView : MapView
    private lateinit var googleMap: GoogleMap
    private val viewModel : DoctorViewModel by viewModel()
    private val queryMap = mutableMapOf<String,String>()
    lateinit var doctors : MutableList<Doctor>
    lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = fragment_doctor_mapview
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Activity)
        fusedLocationClient.lastLocation.addOnSuccessListener {location ->
            Timber.i("Last location is $location")
        }
        getDoctor()
    }

    private fun getDoctor() {
        configureQueryMap()
        doctors = mutableListOf()
        viewModel.doctor.observe(viewLifecycleOwner, Observer {
            it?.let {
                doctors.addAll(it)
            }
            doctors.forEach {
                Timber.i("${it.name} à ${it.address} et est ${it.spec}")
            }
        })
    }

    private fun configureQueryMap() {
        queryMap["dataset"] = "annuaire-des-professionnels-de-sante"
        queryMap["rows"] = "50"
        queryMap["q"] = "Gynécologue obstétricien OR Gynécologue médical OR Gynécologue médical et obstétricien"
        queryMap["facet"] = "nom_com"
        queryMap["refine.nom_com"] = "Chaville"
        viewModel.getDoctors(queryMap)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

    }

    override fun onStart() {
        mapView.onStart()
        super.onStart()
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }


}
