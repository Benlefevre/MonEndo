package com.benlefevre.monendo.doctor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.benlefevre.monendo.MainActivity
import com.benlefevre.monendo.R
import com.benlefevre.monendo.doctor.models.Doctor
import com.benlefevre.monendo.location.LocationData
import com.benlefevre.monendo.location.LocationLiveData
import com.benlefevre.monendo.utils.LOCATION_PERMISSIONS
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_doctor.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class DoctorFragment : Fragment(R.layout.fragment_doctor), OnMapReadyCallback,
    DoctorAdapter.DoctorListAdapterListener {

    private lateinit var map: GoogleMap
    private val viewModel: DoctorViewModel by viewModel()
    private val queryMap = mutableMapOf<String, String>()
    private val doctors = mutableListOf<Doctor>()
    private lateinit var locationLiveData: LocationLiveData
    private lateinit var lastLocation: Location

    private lateinit var doctorAdapter: DoctorAdapter

    private var searchLocation: String = ""
    private val markers = mutableListOf<Marker>()


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
        doctorAdapter = DoctorAdapter(doctors, this)
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
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.latitude,
                            it.longitude
                        ), 14f
                    )
                )
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
                updateUiState(it)
            })
        } else {
            Toast.makeText(
                requireContext(),
                "A network access is necessary to find doctors",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateUiState(state: DoctorUiState) {
        when (state) {
            DoctorUiState.Loading -> {
                fragment_doctor_progress_bar.show()
                doctors.clear()
                doctorAdapter.notifyDataSetChanged()
            }
            is DoctorUiState.DoctorReady -> {
                fragment_doctor_progress_bar.hide()
                doctors.clear()
                doctors.addAll(state.doctors)
                doctorAdapter.notifyDataSetChanged()
                map.clear()
                markers.clear()
                doctors.forEach {
                    val marker = addMarkersOnMap(it)
                    markers.add(marker)
                }
            }
            is DoctorUiState.Error -> Timber.e(state.errorMessage)
        }
    }

    private fun addMarkersOnMap(doctor: Doctor) : Marker {
        val marker = map.addMarker(
            MarkerOptions().title(doctor.name).snippet("${doctor.spec} at ${doctor.address}")
                .position(LatLng(doctor.coordonnees[0], doctor.coordonnees[1]))
        )

        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
//        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.doctor_icon))
        marker.tag = doctor
        Timber.i("${doctor.name} id : ${doctor.id} à ${doctor.address} et est ${doctor.spec} et vaut ${doctor.rating}")
        return marker
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
        map.setInfoWindowAdapter(
            EndoWindowAdapter(
                requireContext()
            )
        )
        map.setOnInfoWindowClickListener {

        }
        map.setOnMarkerClickListener {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        it.position.latitude + 0.0008,
                        it.position.longitude
                    ), 16f
                )
            )
            it.showInfoWindow()
            true
        }
    }

    private fun checkLocationPermissions() {
        if (!EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            EasyPermissions.requestPermissions(
                this, getString(R.string.need_locate_you),
                LOCATION_PERMISSIONS, Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            if (EasyPermissions.permissionPermanentlyDenied(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.location_permission))
                    .setMessage(getString(R.string.want_find_doctor))
                    .setPositiveButton(getString(R.string.go_settings)) { dialog, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:com.benlefevre.monendo")
                        startActivity(intent)
                        dialog.cancel()
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
                return
            } else {
                EasyPermissions.requestPermissions(
                    this, getString(R.string.need_locate_you),
                    LOCATION_PERMISSIONS, Manifest.permission.ACCESS_FINE_LOCATION
                )
                return
            }
        }
        when (requestCode) {
            LOCATION_PERMISSIONS -> locationLiveData.requestLastLocation()
        }
    }

    private fun setOnClickListeners() {
        fragment_doctor_search_txt.addTextChangedListener {
            when {
                it.toString().isNotEmpty() -> fragment_doctor_search_btn.text =
                    getString(R.string.search_btn)
                else -> fragment_doctor_search_btn.text = getString(R.string.around_me)
            }
            searchLocation = it.toString()
        }
        fragment_doctor_search_btn.setOnClickListener {
            getDoctor()
        }
    }

    override fun onDoctorSelected(doctor: Doctor) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    doctor.coordonnees[0] + 0.0008,
                    doctor.coordonnees[1]
                ), 16f
            )
        )
        val marker = markers.filter { it.tag == doctor }[0]
        if (!marker.isInfoWindowShown){
            marker.showInfoWindow()
        }
        else{
            Toast.makeText(requireContext(),"test",Toast.LENGTH_SHORT).show()
        }
    }
}
