package com.benlefevre.monendo.ui.controllers.activities

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.User
import com.benlefevre.monendo.mappers.convertFirebaseUserIntoUser
import com.benlefevre.monendo.utils.NO_MAIL
import com.benlefevre.monendo.utils.NO_NAME
import com.benlefevre.monendo.utils.NO_PHOTO_URL
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val navController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            setContentView(R.layout.activity_main)
            FirebaseAuth.getInstance().currentUser?.let {
                user = convertFirebaseUserIntoUser(it)
                updateDrawerHeader(user)
            }
            setupNavigation()
        }
    }

    private fun updateDrawerHeader(user: User) {
        val header = main_nav_view.getHeaderView(0)
        val headerName = header.findViewById<MaterialTextView>(R.id.drawer_user_name)
        val headerMail = header.findViewById<MaterialTextView>(R.id.drawer_user_mail)
        val headerPhoto = header.findViewById<AppCompatImageView>(R.id.drawer_user_photo)
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
            Glide.with(this).load(R.drawable.ic_girl).into(headerPhoto)
    }

    private fun setupNavigation() {
        val toolbar = main_toolbar
        val bottomNav = main_bottom_bar
        setSupportActionBar(toolbar)

        NavigationUI.setupActionBarWithNavController(this, navController, main_drawer)
        NavigationUI.setupWithNavController(bottomNav, navController)
        NavigationUI.setupWithNavController(main_nav_view, navController)
        NavigationUI.setupWithNavController(toolbar, navController, main_drawer)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.dashboardFragment -> bottomNav.visibility = VISIBLE
                R.id.painFragment -> bottomNav.visibility = GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onBackPressed() {
        if (main_drawer.isDrawerOpen(GravityCompat.START))
            main_drawer.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }
}
