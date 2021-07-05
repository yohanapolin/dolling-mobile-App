package com.dolling.ui.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dolling.R
import com.dolling.databinding.ActivityWelcomeSliderBinding
import com.dolling.listeners.FirestoreListener
import com.dolling.ui.main.MainActivity
import com.dolling.view_model.start.WelcomeSliderViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener

class WelcomeSliderActivity : AppCompatActivity() {
    private lateinit var carouselView: CarouselView

    private var sampleImages = intArrayOf(
        R.drawable.intro_carousel_1,
        R.drawable.intro_carousel_2,
        R.drawable.intro_carousel_3
    )

    private lateinit var binding: ActivityWelcomeSliderBinding
    private val viewModel: WelcomeSliderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeSliderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carouselView = binding.carouselViewIntro
        carouselView.pageCount = sampleImages.size

        carouselView.setImageListener(imageListener);

        binding.buttonCreateAccount.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.buttonSignInGoogle.setOnClickListener {
            signInWithGoogleAccount()
        }
    }

    private var imageListener =
        ImageListener { position, imageView -> imageView.setImageResource(sampleImages[position]) }

    private fun signInWithGoogleAccount() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val gsClient = GoogleSignIn.getClient(this, gso)
        gsClient.revokeAccess()

        val googleSignInIntent = gsClient.signInIntent
        startActivityForResult(googleSignInIntent, 123)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            setFirebaseAuth(account.idToken!!)
        } catch (exc: ApiException) {
            Toast.makeText(this, "Failed to sign in: $exc", Toast.LENGTH_LONG).show()
        }
    }

    private fun setFirebaseAuth(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    viewModel.checkIfAccountAlreadyAvailable(object : FirestoreListener {
                        override fun onSuccessListener(data: Any) {
                            val data1 = data as List<*>
                            if (data1.isEmpty()) {
                                viewModel.createNewAccountByEmail(object : FirestoreListener {
                                    override fun onSuccessListener(data: Any) {
                                        setSharedPreference()
                                        goToNextActivity()
                                    }

                                    override fun onFailureListener(errorMsg: String) {
                                        Toast.makeText(
                                            this@WelcomeSliderActivity,
                                            "Failed to sign in: $errorMsg",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                            } else {
                                setSharedPreference()
                                goToNextActivity()
                            }
                        }

                        override fun onFailureListener(errorMsg: String) {
                            Toast.makeText(
                                this@WelcomeSliderActivity,
                                "Failed to sign in: $errorMsg",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } else {
                    Toast.makeText(
                        this,
                        "Failed to sign in: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun setSharedPreference() {
        getSharedPreferences("dolling", Context.MODE_PRIVATE).apply {
            with(this.edit()) {
                putBoolean("welcome_slider_already_opened", true)
                putBoolean("is_logged_in_with_email", true)
                apply()
            }
        }
    }

    private fun goToNextActivity() {
        Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            Toast.makeText(
                this@WelcomeSliderActivity,
                "Welcome aboard ${Firebase.auth.currentUser!!.displayName}",
                Toast.LENGTH_LONG
            ).show()
            startActivity(this)
        }
    }
}