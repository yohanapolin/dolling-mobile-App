package com.dolling.ui.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.dolling.databinding.ActivitySplashScreenBinding
import com.dolling.ui.main.MainActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val activity: Class<out AppCompatActivity>

        val firebaseAuth = Firebase.auth.currentUser
        activity = if (firebaseAuth != null) MainActivity::class.java
        else defineNextActivity()

        Handler(Looper.getMainLooper()).postDelayed({
            Intent(this, activity).apply {
                this.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(this)
            }
        }, 3000)
    }

    private fun defineNextActivity(): Class<out AppCompatActivity> {
        var welcomeSliderAlreadyOpened: Boolean
        getSharedPreferences("dolling", Context.MODE_PRIVATE).apply {
            welcomeSliderAlreadyOpened = this.getBoolean("welcome_slider_already_opened", false)
        }
        return if (welcomeSliderAlreadyOpened) SignInActivity::class.java
        else WelcomeSliderActivity::class.java
    }
}