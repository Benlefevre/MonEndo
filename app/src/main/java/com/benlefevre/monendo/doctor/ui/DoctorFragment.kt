package com.benlefevre.monendo.doctor.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.benlefevre.monendo.MainActivity
import com.benlefevre.monendo.R
import com.benlefevre.monendo.databinding.FragmentDoctorBinding
import com.benlefevre.monendo.doctor.adapter.DoctorAdapter
import com.benlefevre.monendo.doctor.adapter.EndoWindowAdapter
import com.benlefevre.monendo.doctor.models.Doctor
import com.benlefevre.monendo.doctor.viewmodel.DoctorUiState
import com.benlefevre.monendo.doctor.viewmodel.DoctorViewModel
import com.benlefevre.monendo.location.LocationData
import com.benlefevre.monendo.location.LocationLiveData
import com.benlefevre.monendo.utils.DOC
import com.benlefevre.monendo.utils.GYNE
import com.benlefevre.monendo.utils.LOCATION_PERMISSIONS
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import pub.devrel.easypermissions.EasyPermissions

class DoctorFragment : Fragment(R.layout.fragment_doctor), OnMapReadyCallback,
    DoctorAdapter.DoctorListAdapterListener {

    private var _binding: FragmentDoctorBinding? = null
    private val binding get() = _binding!!
    private var isSearchVisible = false
    private lateinit var menuItem: MenuItem
    private lateinit var navController: NavController
    private lateinit var map: GoogleMap
    private val viewModel: DoctorViewModel by stateViewModel()
    private val doctors = mutableListOf<Doctor>()
    private lateinit var locationLiveData: LocationLiveData
    private lateinit var lastLocation: Location

    private lateinit var doctorAdapter: DoctorAdapter

    private var searchLocation: String = ""
    private val markers = mutableListOf<Marker>()
    private val cities = mutableListOf<String>()
    private val postCodes = mutableListOf<String>()
    private var isFirstLocation = true


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDoctorBinding.bind(view)
        setHasOptionsMenu(true)
        navController = findNavController()
        setOnClickListeners()
        initMap()
        configureViewModelObservers()
        configureRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.doctor_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        menuItem = item
        return when (item.itemId) {
            R.id.doctor_search -> {
                if (!isSearchVisible) {
                    openSearch()
                } else {
                    closeSearch()
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun configureViewModelObservers() {
        locationLiveData = LocationLiveData(requireContext())
        locationLiveData.observe(viewLifecycleOwner, {
            handleLocationData(it)
        })
        viewModel.cities.observe(viewLifecycleOwner, {
            handleCitiesData(it)
        })
        viewModel.doctor.observe(viewLifecycleOwner, {
            updateUiState(it)
        })
    }

    private fun handleCitiesData(it: List<Pair<String, String>>) {
        cities.clear()
        postCodes.clear()
        it.forEachIndexed { index, pair ->
            cities.add(index, "${pair.first}   ${pair.second}")
            postCodes.add(index, pair.second)
        }
        val citiesAdapter =
            ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                cities
            )

        with(binding.searchTxt) {
            setAdapter(citiesAdapter)
            if (searchLocation == postCodes[0]) {
                dismissDropDown()
            } else {
                showDropDown()
            }
        }
    }

    private fun configureRecyclerView() {
        doctorAdapter =
            DoctorAdapter(doctors, this)
        binding.docRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = doctorAdapter
        }
    }

    /**
     * It is the function called when LocationLiveData emit a value. It verifies if an exception is
     * thrown and if it is not the case it updates the position of the GoogleMap's camera.
     */
    @SuppressLint("MissingPermission")
    private fun handleLocationData(locationData: LocationData) {
        if (handleLocationException(locationData.exception)) {
            return
        }
        locationData.location?.let {
            lastLocation = it
            if (::map.isInitialized) {
                map.isMyLocationEnabled = true
                if (isFirstLocation) {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                it.latitude,
                                it.longitude
                            ), 12f
                        )
                    )
                    viewModel.setGeoLocationQuery(lastLocation)
                    isFirstLocation = false
                }
                getDoctor()
            }
        }
    }

    /**
     * Checks if an exception is thrown and calls the function that requested the needed permissions.
     */
    private fun handleLocationException(exception: Exception?): Boolean {
        exception ?: return false
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
            .replace(R.id.mapview, mapFragment)
            .commit()
    }

    /**
     * Checks is the device has a network access, calls the query's configuration function and
     * sets an observer on the viewModel's doctor LiveData.
     */
    private fun getDoctor() {
        if (MainActivity.isConnected && ::lastLocation.isInitialized) {
            viewModel.getDoctorsWithUserInput()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.network_location),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Handles the value returned by the DoctorLiveData and updates the Ui according to the result.
     */
    private fun updateUiState(state: DoctorUiState) {
        when (state) {
            is DoctorUiState.Loading -> {
                binding.progressBar.show()
                doctors.clear()
                doctorAdapter.notifyDataSetChanged()
                setChipUnClickableWhenLoading()
            }
            is DoctorUiState.DoctorReady -> {
                setChipClickableWhenReady()
                doctors.clear()
                doctors.addAll(state.doctors)
                doctorAdapter.notifyDataSetChanged()
                map.clear()
                markers.clear()
                doctors.forEach {
                    val marker = addMarkersOnMap(it)
                    markers.add(marker)
                }
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            doctors[0].coordonnees[0],
                            doctors[0].coordonnees[1]
                        ), 12f
                    )
                )
                binding.progressBar.hide()
            }
            is DoctorUiState.Error -> {
                Snackbar.make(
                    binding.docRecyclerView,
                    state.errorMessage,
                    Snackbar.LENGTH_SHORT
                ).show()
                binding.progressBar.hide()
            }
        }
    }

    /**
     * Adds a marker on the map with the doctor's values and return the added marker with a tag
     * that corresponding to the doctor passed in parameter.
     */
    private fun addMarkersOnMap(doctor: Doctor): Marker {
        val marker = map.addMarker(
            MarkerOptions().title(doctor.name).snippet("${doctor.spec} at ${doctor.address}")
                .position(LatLng(doctor.coordonnees[0], doctor.coordonnees[1]))
        )

        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        marker.tag = doctor
        return marker
    }

    /**
     * Configures all the map's behaviors when user click on an item
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setInfoWindowAdapter(
            EndoWindowAdapter(
                requireContext()
            )
        )

        map.setOnInfoWindowClickListener {
            val doctorDest =
                DoctorFragmentDirections.actionDoctorFragmentToDoctorDetailFragment(it.tag as Doctor)
            navController.navigate(doctorDest)
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
            binding.docRecyclerView.scrollToPosition(doctors.indexOf(it.tag))
            doctorAdapter.index = doctors.indexOf(it.tag)
            doctorAdapter.notifyDataSetChanged()
            true
        }

        map.setOnMyLocationButtonClickListener {
            viewModel.setGeoLocationQuery(lastLocation)
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        lastLocation.latitude,
                        lastLocation.longitude
                    ), 12f
                )
            )
            getDoctor()
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
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                showDialogToSettings()
                return
            } else {
                EasyPermissions.requestPermissions(
                    requireActivity(), getString(R.string.need_locate_you),
                    LOCATION_PERMISSIONS, Manifest.permission.ACCESS_FINE_LOCATION
                )
                return
            }
        }
        when (requestCode) {
            LOCATION_PERMISSIONS -> locationLiveData.requestLastLocation()
        }
    }

    private fun showDialogToSettings() {
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
    }

    private fun setOnClickListeners() {
        var selectedCity = ""
        binding.searchTxt.setOnItemClickListener { _, _, position, _ ->
            selectedCity = cities[position]
            binding.searchTxt.setText(cities[position])
            searchLocation = postCodes[position]
            viewModel.setPostalCodeQuery(searchLocation)
        }

        binding.searchTxt.addTextChangedListener { input ->
            if (input.toString() == selectedCity) {
                binding.searchTxt.dismissDropDown()
            } else if (input.toString().length >= 2) {
                viewModel.getAdresse(input.toString())
                searchLocation = ""
            }
        }

        binding.searchBtn.setOnClickListener {
            closeSearch()
            getDoctor()
            closeKeyboard()
        }

        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.gyneChip.id -> {
                    binding.gyneChip2.isChecked = true
                    viewModel.setSpecialitySearchCriteria(GYNE)
                }
                binding.docChip.id -> {
                    binding.docChip2.isChecked = true
                    viewModel.setSpecialitySearchCriteria(DOC)
                }
            }
            if (!isSearchVisible) {
                getDoctor()
            }
        }

        binding.docChip2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.docChip.isChecked = true
                viewModel.setSpecialitySearchCriteria(DOC)
            }
        }

        binding.gyneChip2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.gyneChip.isChecked = true
                viewModel.setSpecialitySearchCriteria(GYNE)
            }
        }
    }

    private fun setChipUnClickableWhenLoading() {
        binding.docChip.isCheckable = false
        binding.docChip2.isCheckable = false
        binding.gyneChip.isCheckable = false
        binding.gyneChip2.isCheckable = false
    }

    private fun setChipClickableWhenReady() {
        binding.docChip.isCheckable = true
        binding.docChip2.isCheckable = true
        binding.gyneChip.isCheckable = true
        binding.gyneChip2.isCheckable = true
    }

    private fun closeKeyboard() {
        val imm: InputMethodManager =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view: IBinder? = requireView().rootView.windowToken
        imm.hideSoftInputFromWindow(view, 0)
    }

    private fun openSearch() {
        isSearchVisible = true
        val view = view?.findViewById<MotionLayout>(R.id.doctor_motion_layout)
        view?.transitionToEnd()
        menuItem.setIcon(R.drawable.search_off)
    }

    private fun closeSearch() {
        isSearchVisible = false
        val view = view?.findViewById<MotionLayout>(R.id.doctor_motion_layout)
        view?.transitionToStart()
        menuItem.setIcon(R.drawable.search)
        binding.searchTxt.setText("")
        closeKeyboard()
    }

    /**
     * Defines the map's behavior and the navigation when user clicks on a recycler view's item
     */
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
        if (!marker.isInfoWindowShown) {
            marker.showInfoWindow()
        } else {
            val doctorDest =
                DoctorFragmentDirections.actionDoctorFragmentToDoctorDetailFragment(doctor)
            navController.navigate(doctorDest)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.docRecyclerView.adapter = null
        _binding = null
    }
}
