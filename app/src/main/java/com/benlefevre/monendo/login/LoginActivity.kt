package com.benlefevre.monendo.login

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.benlefevre.monendo.MainActivity
import com.benlefevre.monendo.R
import com.benlefevre.monendo.databinding.ActivityLoginBinding
import com.benlefevre.monendo.utils.RC_SIGN_IN
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    private val loginViewModel : LoginActivityViewModel by viewModel()
    private var _binding : ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private val sharedPreferences : SharedPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(LayoutInflater.from(this),null,false)
        if (FirebaseAuth.getInstance().currentUser != null) {
            AuthUI.getInstance().signOut(this)
            removeUserInPreferences()
        }
        createSignInIntent()
    }

    private fun removeUserInPreferences() {
        sharedPreferences.edit().apply {
            remove("id")
            remove("name")
            remove("mail")
            remove("url")
            putBoolean("isLogged", false)
        }.apply()
    }

    private fun saveUserInPreferences(user: User) {
        sharedPreferences.edit().apply {
            putString("id", user.id)
            putString("name", user.name)
            putString("mail", user.mail)
            putString("url", user.photoUrl)
            putBoolean("isLogged", true)
        }.apply()
    }

    private fun createSignInIntent() {
        val providers = setupProviders()

        val authMethodPickerLayout = createAuthMethodPickerLayout()

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .enableAnonymousUsersAutoUpgrade()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setTheme(R.style.LoginActivityTheme)
                .setIsSmartLockEnabled(false)
                .build(), RC_SIGN_IN
        )
    }

    //    Configure the list of authorized providers
    private fun setupProviders(): List<AuthUI.IdpConfig> {
        return listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.AnonymousBuilder().build()
        )
    }

    //    Creating AuthMethodPickerLayout to customise the Login Ui
    private fun createAuthMethodPickerLayout(): AuthMethodPickerLayout {
        return AuthMethodPickerLayout.Builder(R.layout.activity_login)
            .setEmailButtonId(R.id.login_mail_btn)
            .setGoogleButtonId(R.id.login_google_btn)
            .setFacebookButtonId(R.id.login_facebook_btn)
            .setAnonymousButtonId(R.id.login_anonymous)
            .build()
    }

    private fun saveUserInFirestore() {
        val user : User
        FirebaseAuth.getInstance().currentUser?.let {
            user = convertFirebaseUserIntoUser(it)
            if (!it.isAnonymous) {
                loginViewModel.createUserInFirestore(user)
            }
            saveUserInPreferences(user)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleResponseAfterSignIn(requestCode, resultCode, data)
    }

    //    According to the return of the Sign In intent, creation of a user on Firestore
//    and opening of the main activity
    private fun handleResponseAfterSignIn(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val idpResponse = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK && idpResponse?.providerType != null) {
                saveUserInFirestore()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                if (idpResponse == null)
                    Snackbar.make(
                        binding.loginRoot,
                        getString(R.string.cancel_login),
                        Snackbar.LENGTH_SHORT
                    ).show()
                else {
                    when (idpResponse.error?.errorCode) {
                        ErrorCodes.NO_NETWORK -> Snackbar.make(
                            binding.loginRoot,
                            getString(R.string.network_to_login),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        ErrorCodes.UNKNOWN_ERROR -> Snackbar.make(
                            binding.loginRoot,
                            getString(R.string.unknown_error_login),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        else -> Snackbar.make(
                            binding.loginRoot,
                            getString(R.string.unknown_error_login),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
