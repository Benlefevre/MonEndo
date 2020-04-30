package com.benlefevre.monendo.ui.controllers.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.Doctor
import com.benlefevre.monendo.location.LocationData
import com.benlefevre.monendo.location.LocationLiveData
import com.benlefevre.monendo.ui.adapters.DoctorAdapter
import com.benlefevre.monendo.ui.adapters.EndoWindowAdapter
import com.benlefevre.monendo.ui.controllers.activities.MainActivity
import com.benlefevre.monendo.ui.viewmodels.DoctorViewModel
import com.benlefevre.monendo.utils.LOCATION_PERMISSIONS
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_doctor.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class DoctorFragment : Fragment(R.layout.fragment_doctor), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val viewModel: DoctorViewModel by viewModel()
    private val queryMap = mutableMapOf<String, String>()
    private val doctors =  mutableListOf<Doctor>()
    private lateinit var locationLiveData: LocationLiveData
    private lateinit var lastLocation: Location

    private lateinit var doctorAdapter: DoctorAdapter

    private var searchLocation: String =""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        initMap()
        locationLiveData = LocationLiveData(requireContext())
        locationLiveData.observe(viewLifecycleOwner, Observer {
            handleLocationData(it)
        })
        configureRecyclerView()
    }

    private fun configureRecyclerView() {
        doctorAdapter = DoctorAdapter(doctors)
        fragment_doctor_recycler_view.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = doctorAdapter
        }
    }

    private fun handleLocationData(locationData: LocationData) {
        if (handleLocationException(locationData.exception)) {
            return
        }
        locationData.location?.let {
            lastLocation = it
            if (::map.isInitialized) {
                map.isMyLocationEnabled = true
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 14f))
            }
            getDoctor()
        }
    }

    private fun handleLocationException(exception: Exception?): Boolean {
        exception ?: return false
        Timber.e(exception)
        when (exception) {
            is SecurityException -> checkLocationPermissions()
        }
        return true
    }

    /**
     * Sets a map in the according fragment with SupportMapFragment.newInstance()
     */
    private fun initMap() {
        val mapOptions = GoogleMapOptions()
            .mapType(GoogleMap.MAP_TYPE_NORMAL)
            .zoomControlsEnabled(true)
            .zoomGesturesEnabled(true)
            .mapToolbarEnabled(false)

        val mapFragment = SupportMapFragment.newInstance(mapOptions)
        mapFragment.getMapAsync(this)
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_doctor_mapview, mapFragment)
            .commit()
    }

    private fun getDoctor() {
        if (MainActivity.isConnected) {
            configureQueryMap()
            viewModel.doctor.observe(viewLifecycleOwner, Observer {
                it?.let {
                    doctors.clear()
                    doctors.addAll(it)
                    doctorAdapter.notifyDataSetChanged()
                    map.clear()
                    it.forEach {
                        addMarkersOnMap(it)
                    }
                }
            })
        }
    }

    private fun addMarkersOnMap(doctor: Doctor){
        val marker = map.addMarker(MarkerOptions().title(doctor.name).snippet("${doctor.spec} at ${doctor.address}")
            .position(LatLng(doctor.coordonnees[0] , doctor.coordonnees[1])))

        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
//        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.doctor_icon))
        marker.tag = doctor
        Timber.i("${doctor.name} id : ${doctor.id} à ${doctor.address} et est ${doctor.spec} et vaut ${doctor.rating}")
    }

    private fun configureQueryMap() {
        queryMap.clear()
        queryMap["dataset"] = "annuaire-des-professionnels-de-sante"
        queryMap["rows"] = "50"
        if (fragment_doctor_gyne_chip.isChecked) {
            queryMap["q"] =
                "Gynécologue obstétricien OR Gynécologue médical OR Gynécologue médical et obstétricien"
        }
        if (fragment_doctor_doc_chip.isChecked) {
            queryMap["q"] = "Médecin généraliste"
        }
        if (searchLocation.isEmpty()) {
            queryMap["geofilter.distance"] =
                "${lastLocation.latitude},${lastLocation.longitude},5000"
        } else {
            queryMap["facet"] = "nom_com"
            queryMap["refine.nom_com"] = searchLocation.capitalize()
        }
        Timber.i("${queryMap["q"]}")
        viewModel.getDoctors(queryMap)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setInfoWindowAdapter(EndoWindowAdapter(requireContext()))
        map.setOnInfoWindowClickListener {

        }
        map.setOnMarkerClickListener {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.position.latitude + 0.0008,it.position.longitude),16f))
            it.showInfoWindow()
            true
        }
    }

    private fun setOnClickListeners() {
        fragment_doctor_search_txt.addTextChangedListener {
            when{
                it.toString().isNotEmpty() -> fragment_doctor_search_btn.text = getString(R.string.search_btn)
                else -> fragment_doctor_search_btn.text = getString(R.string.around_me)
            }
            searchLocation = it.toString()
        }
        fragment_doctor_search_btn.setOnClickListener {
            getDoctor()
        }
    }

    private fun checkLocationPermissions() {
        if (!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            EasyPermissions.requestPermissions(
                this, "We need this permission to locate you on a map",
                LOCATION_PERMISSIONS, Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            if (EasyPermissions.permissionPermanentlyDenied(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Location permissions")
                    .setMessage("If you want find a doctor around you or do a search, we need to locate you. Please give us the needed permission")
                    .setPositiveButton("Go to settings") { dialog, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:com.benlefevre.monendo")
                        startActivity(intent)
                        dialog.cancel()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
                return
            } else {
                EasyPermissions.requestPermissions(
                    this, "We need this permission to locate you on a map",
                    LOCATION_PERMISSIONS, Manifest.permission.ACCESS_FINE_LOCATION
                )
                return
            }
        }
        when (requestCode) {
            LOCATION_PERMISSIONS -> locationLiveData.requestLastLocation()
        }
    }
}
