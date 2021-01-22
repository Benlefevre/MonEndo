package com.benlefevre.monendo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.benlefevre.monendo.databinding.ActivityMainBinding
import com.benlefevre.monendo.login.LoginActivity
import com.benlefevre.monendo.login.User
import com.benlefevre.monendo.utils.ConnectivityLiveData
import com.benlefevre.monendo.utils.NO_MAIL
import com.benlefevre.monendo.utils.NO_NAME
import com.benlefevre.monendo.utils.NO_PHOTO_URL
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textview.MaterialTextView
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {

    private val navController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var connectivityLiveData: ConnectivityLiveData
    private val sharedPreferences : SharedPreferences by inject()

    companion object {
        var isConnected = false
        lateinit var user: User
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this),null,false)
//        if (FirebaseAuth.getInstance().currentUser == null) {
        if (!sharedPreferences.getBoolean("isLogged",false)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            setContentView(binding.root)
            verifyConnectivity()
            user = User(
                sharedPreferences.getString("id","0")!!,
                sharedPreferences.getString("name", NO_NAME)!!,
                sharedPreferences.getString("mail", NO_MAIL)!!,
                sharedPreferences.getString("url", NO_PHOTO_URL)!!
            )
            updateDrawerHeader(user)
//            FirebaseAuth.getInstance().currentUser?.let {
//                user =
//                    convertFirebaseUserIntoUser(it)
//                updateDrawerHeader(user)
//            }
            setupNavigation()
        }
    }

    /**
     * Observes the device's connectivity and store it into a boolean
     */
    private fun verifyConnectivity() {
        connectivityLiveData = ConnectivityLiveData(this)
        connectivityLiveData.observe(this, {
            isConnected = it
        })
    }

    /**
     * Binds user information into the nav header views
     */
    private fun updateDrawerHeader(user: User) {
        val header = binding.mainNavView.getHeaderView(0)
        val headerName = header.findViewById<MaterialTextView>(R.id.user_name)
        val headerMail = header.findViewById<MaterialTextView>(R.id.user_mail)
        val headerPhoto = header.findViewById<AppCompatImageView>(R.id.user_photo)
        if (user.name.isNotEmpty() && user.name != NO_NAME)
            headerName.text = user.name
        else headerName.text = getString(
            R.string.anonymous
        )
        if (user.mail.isNotEmpty() && user.mail != NO_MAIL)
            headerMail.text = user.mail
        if (user.photoUrl.isNotEmpty() && user.photoUrl != NO_PHOTO_URL)
            Glide.with(this).load(user.photoUrl).apply(RequestOptions.circleCropTransform())
                .into(headerPhoto)
        else
            Glide.with(this).load(R.drawable.ic_girl_white)
                .apply(RequestOptions.circleCropTransform()).into(headerPhoto)
    }

    /**
     * Configures the NavigationDrawer, the Toolbar and the BottomNavigation with the NavController
     *  that navigate to destinations defined into the nav_graph.xml
     */
    private fun setupNavigation() {
        val toolbar = binding.mainToolbar
        val bottomNav = binding.mainBottomBar
        setSupportActionBar(toolbar)

        NavigationUI.setupActionBarWithNavController(this, navController, binding.mainDrawer)
        NavigationUI.setupWithNavController(bottomNav, navController)
        NavigationUI.setupWithNavController(binding.mainNavView, navController)
        NavigationUI.setupWithNavController(toolbar, navController, binding.mainDrawer)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.dashboardFragment -> bottomNav.visibility = VISIBLE
                R.id.painFragment -> bottomNav.visibility = GONE
                R.id.painDetailFragment -> bottomNav.visibility = GONE
                R.id.symptomDetailFragment -> bottomNav.visibility = GONE
                R.id.activitiesDetailFragment -> bottomNav.visibility = GONE
                R.id.sleepDetailFragment -> bottomNav.visibility = GONE
                R.id.moodDetailFragment -> bottomNav.visibility = GONE
                R.id.doctorDetailFragment -> bottomNav.visibility = GONE
                R.id.doctorFragment -> bottomNav.visibility = VISIBLE
                R.id.settingsFragment -> bottomNav.visibility = GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onBackPressed() {
        if (binding.mainDrawer.isDrawerOpen(GravityCompat.START))
            binding.mainDrawer.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
