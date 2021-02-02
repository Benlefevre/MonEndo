package com.benlefevre.monendo.doctor.ui

import android.content.Intent
import android.content.Intent.ACTION_DIAL
import android.content.Intent.ACTION_VIEW
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
import com.benlefevre.monendo.databinding.CustomDialogCommentBinding
import com.benlefevre.monendo.databinding.FragmentDoctorDetailBinding
import com.benlefevre.monendo.doctor.adapter.CommentaryAdapter
import com.benlefevre.monendo.doctor.models.Commentary
import com.benlefevre.monendo.doctor.models.Doctor
import com.benlefevre.monendo.doctor.viewmodel.DoctorViewModel
import com.benlefevre.monendo.login.User
import com.benlefevre.monendo.utils.NO_NAME
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import java.util.*

class DoctorDetailFragment : Fragment(R.layout.fragment_doctor_detail), OnMapReadyCallback {

    private var _binding : FragmentDoctorDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var doctor: Doctor
    private lateinit var map: GoogleMap
    private lateinit var markerOptions: MarkerOptions
    private lateinit var user: User
    private lateinit var adapter: CommentaryAdapter

    private val commentaries = mutableListOf<Commentary>()

    private val viewModel: DoctorViewModel by stateViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDoctorDetailBinding.bind(view)
        user = MainActivity.user
        initMap()
        MapsInitializer.initialize(context)
        configureRecyclerView()
        val args: DoctorDetailFragmentArgs by navArgs()
        doctor = args.selectedDoctor
        updateUi(doctor)
        configureBottomNav()
        initViewModel()
    }

    private fun configureRecyclerView() {
        adapter = CommentaryAdapter(commentaries)
        binding.recyclerview.apply {
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
        binding.detailName.text = doctor.name
        binding.detailSpec.text = doctor.spec
        binding.detailAddress.text = doctor.address
        binding.detailPhone.text =
            if (!doctor.phone.isNullOrBlank()) {
                "${doctor.phone}"
            } else {
                binding.detailBottomBar.menu[0].isVisible = false
                getString(R.string.no_phone)
            }
        doctor.typesActes?.let {
            binding.detailTypes.apply {
                binding.detailTypesLegend.visibility = View.VISIBLE
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
            .replace(R.id.detail_map, mapFragment)
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
        }

        map.setOnMarkerClickListener {
            false
        }
        map.setOnInfoWindowClickListener {
        }
    }

    private fun configureBottomNav() {
        binding.detailBottomBar.setOnNavigationItemSelectedListener {
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
                user.id,
                user.photoUrl,
                Date()
            )
        viewModel.createCommentaryInFirestore(commentary, user)
    }

    private fun alreadyComment(): Boolean {
        var alreadyComment = false
        commentaries.forEach {
            if (it.authorId == user.id) {
                alreadyComment = true
            }
        }
        return if (alreadyComment) {
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
                val dialogBinding = CustomDialogCommentBinding.inflate(LayoutInflater.from(context),null,false)
                val slider = dialogBinding.commentSlider
                val userInput = dialogBinding.commentText

                MaterialAlertDialogBuilder(requireContext())
                    .setView(dialogBinding.root)
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.cancel()
                    }
                    .setPositiveButton(getString(R.string.save)) { dialog, _ ->
                        if (!userInput.text.isNullOrBlank()) {
                            createCommentary(slider.value, userInput.text.toString())
                            viewModel.getCommentaryWithId(doctor.id)
                        }
                        dialog.cancel()
                    }
                    .show()
            } else {
                val snackbar =
                    Snackbar.make(
                        binding.detailBottomBar,
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
