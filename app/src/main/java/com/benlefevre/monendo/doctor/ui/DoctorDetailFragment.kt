package com.benlefevre.monendo.doctor.ui

import android.content.Intent
import android.content.Intent.ACTION_DIAL
import android.content.Intent.ACTION_VIEW
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.benlefevre.monendo.MainActivity
import com.benlefevre.monendo.R
import com.benlefevre.monendo.doctor.CommentaryAdapter
import com.benlefevre.monendo.doctor.models.Commentary
import com.benlefevre.monendo.doctor.models.Doctor
import com.benlefevre.monendo.doctor.viewmodel.DoctorViewModel
import com.benlefevre.monendo.login.User
import com.benlefevre.monendo.utils.COMMENT_ID
import com.benlefevre.monendo.utils.NO_NAME
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.custom_dialog_comment.view.*
import kotlinx.android.synthetic.main.fragment_doctor_detail.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import java.util.*

class DoctorDetailFragment : Fragment(R.layout.fragment_doctor_detail), OnMapReadyCallback {

    private lateinit var doctor: Doctor
    private lateinit var map: GoogleMap
    private lateinit var markerOptions: MarkerOptions
    private lateinit var user: User
    private lateinit var adapter: CommentaryAdapter
    private val sharedPreferences: SharedPreferences by inject()
    private lateinit var commentIdList: MutableList<String>
    private val gson = Gson()
    private val commentaries = mutableListOf<Commentary>()

    private val viewModel: DoctorViewModel by stateViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = MainActivity.user
        initMap()
        configureRecyclerView()
        val args: DoctorDetailFragmentArgs by navArgs()
        doctor = args.selectedDoctor
        updateUi(doctor)
        configureBottomNav()
        initViewModel()
    }

    private fun configureRecyclerView() {
        adapter = CommentaryAdapter(commentaries)
        fragment_doctor_detail_recyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DoctorDetailFragment.adapter
        }
    }

    private fun initViewModel() {
        viewModel.getCommentaryWithId(doctor.id)
        viewModel.commentaries.observe(viewLifecycleOwner, {
            commentaries.clear()
            commentaries.addAll(it)
            adapter.notifyDataSetChanged()
        })
    }

    private fun updateUi(doctor: Doctor) {
        fragment_doctor_detail_name.text = doctor.name
        fragment_doctor_detail_spec.text = doctor.spec
        fragment_doctor_detail_address.text = doctor.address
        fragment_doctor_detail_phone.text =
            if (!doctor.phone.isNullOrBlank()) {
                "${doctor.phone}"
            } else {
                fragment_doctor_detail_bottom_bar.menu[0].isVisible = false
                getString(R.string.no_phone)
            }
        doctor.typesActes?.let {
            fragment_doctor_detail_types.apply {
                fragment_doctor_detail_types_legend.visibility = View.VISIBLE
                text = doctor.typesActes.replace(",", "\n")
                visibility = View.VISIBLE
                movementMethod = ScrollingMovementMethod()
            }
        }

        markerOptions = MarkerOptions().title(doctor.name)
            .position(LatLng(doctor.coordonnees[0], doctor.coordonnees[1]))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
    }

    /**
     * Sets a map in the according fragment with SupportMapFragment.newInstance()
     */
    private fun initMap() {
        val mapOptions = GoogleMapOptions()
            .mapType(GoogleMap.MAP_TYPE_NORMAL)
            .liteMode(true)
            .zoomGesturesEnabled(false)
            .mapToolbarEnabled(false)

        val mapFragment = SupportMapFragment.newInstance(mapOptions)
        mapFragment.getMapAsync(this)
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_doctor_detail_map, mapFragment)
            .commit()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val marker = map.addMarker(markerOptions)
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    marker.position.latitude,
                    marker.position.longitude
                ), 16f
            )
        )
        map.setOnMapClickListener {
//            marker.showInfoWindow()
        }
//        marker.showInfoWindow()
        map.setOnMarkerClickListener {
            false
        }
        map.setOnInfoWindowClickListener {
        }
    }

    private fun configureBottomNav() {
        fragment_doctor_detail_bottom_bar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.doctor_menu_call -> {
                    startCall()
                    true
                }
                R.id.doctor_menu_web -> {
                    startWebSearch()
                    true
                }
                R.id.doctor_menu_comment -> {
                    openCommentaryDialog()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Open a browser Activity with an URL that contained a search with the doctor name. The doctor's
     * name is split to match the http request format
     */
    private fun startWebSearch() {
        var name = ""
        doctor.name.split(" ").forEach { subString ->
            name += "$subString+"
        }
        val intent = Intent(ACTION_VIEW)
        intent.data = Uri.parse("https://www.google.com/search?q=$name")
        startActivity(intent)
    }

    private fun startCall() {
        val intent = Intent(ACTION_DIAL)
        intent.data = Uri.parse("tel: ${doctor.phone}")
        startActivity(intent)
    }

    private fun createCommentary(rating: Float, text: String) {
        val commentary =
            Commentary(
                doctor.id,
                doctor.name,
                rating.toDouble(),
                text,
                user.name,
                user.photoUrl,
                Date()
            )
        viewModel.createCommentaryInFirestore(commentary, user)
    }

    private fun alreadyComment(): Boolean {
        commentIdList = mutableListOf()
        gson.fromJson<List<String>>(
            sharedPreferences.getString(COMMENT_ID, ""),
            object : TypeToken<List<String>>() {}.type
        )?.let {
            commentIdList.addAll(it)
        }
        return if (commentIdList.contains("${doctor.name}-${user.id}")) {
            Toast.makeText(
                requireContext(),
                getString(R.string.already_comment),
                Toast.LENGTH_SHORT
            )
                .show()
            true
        } else {
            false
        }
    }

    private fun openCommentaryDialog() {
        if (alreadyComment()) {
            return
        } else {
            if (user.name != NO_NAME) {
                val customDialog =
                    LayoutInflater.from(requireContext())
                        .inflate(R.layout.custom_dialog_comment, null)
                val slider = customDialog.custom_dialog_comment_slider
                val userInput = customDialog.custom_dialog_comment_text

                MaterialAlertDialogBuilder(requireContext())
                    .setView(customDialog)
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.cancel()
                    }
                    .setPositiveButton(getString(R.string.save)) { dialog, _ ->
                        if (!userInput.text.isNullOrBlank()) {
                            createCommentary(slider.value, userInput.text.toString())
                            viewModel.getCommentaryWithId(doctor.id)
                            commentIdList.add("${doctor.name}-${user.id}")
                            sharedPreferences.edit()
                                .putString(COMMENT_ID, gson.toJson(commentIdList))
                                .apply()
                        }
                        dialog.cancel()
                    }
                    .show()
            } else {
                val snackbar =
                    Snackbar.make(
                        fragment_doctor_detail_bottom_bar,
                        "You have to sign in to leave a commentary",
                        Snackbar.LENGTH_LONG
                    )
                snackbar.setAction("Log out and sign in") {
                    findNavController().navigate(R.id.logOut)
                }
                snackbar.show()
            }
        }
    }
}
