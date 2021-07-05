package com.dolling.ui.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dolling.databinding.ActivityOtpVerificationBinding
import com.dolling.listeners.FirestoreListener
import com.dolling.ui.main.MainActivity
import com.dolling.view_model.start.OtpVerificationViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class OtpVerificationActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_NAME = "extra_name"
    }

    private lateinit var binding: ActivityOtpVerificationBinding
    private val viewModel: OtpVerificationViewModel by viewModels()
    private lateinit var phoneNumber: String
    private lateinit var name: String
    private var verificationId: String? = null

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                signInWithPhoneAuthCredentials(p0)
            }

            override fun onVerificationFailed(exc: FirebaseException) {
                Toast.makeText(this@OtpVerificationActivity, exc.message, Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                verificationId = p0
            }
        }

        phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)!!
        name = intent.getStringExtra(EXTRA_NAME)!!

        sendOTPCodeRequest()

        binding.buttonDone.setOnClickListener {
            if (binding.editTextName.text.isNullOrEmpty())
                binding.editTextName.error = "field tidak boleh kosong!"
            else {
                verificationId?.let {
                    val credential = PhoneAuthProvider.getCredential(
                        it, binding.editTextName.text.toString()
                    )
                    signInWithPhoneAuthCredentials(credential)
                }
            }
        }
    }

    private fun sendOTPCodeRequest() {
        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredentials(credential: PhoneAuthCredential) {
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    viewModel.checkIfAccountAlreadyAvailable(
                        phoneNumber, object : FirestoreListener {
                            override fun onSuccessListener(data: Any) {
                                val data1 = data as List<*>
                                if (data1.isEmpty()) {
                                    viewModel.createNewAccountByPhoneNumber(
                                        phoneNumber,
                                        name,
                                        object : FirestoreListener {
                                            override fun onSuccessListener(data: Any) {
                                                setSharedPreference()
                                                goToNextActivity()
                                            }

                                            override fun onFailureListener(errorMsg: String) {
                                                Toast.makeText(
                                                    this@OtpVerificationActivity,
                                                    "Failed to sign in: $errorMsg",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    )
                                } else {
                                    setSharedPreference()
                                    goToNextActivity()
                                }
                            }

                            override fun onFailureListener(errorMsg: String) {
                                Toast.makeText(
                                    this@OtpVerificationActivity,
                                    "Failed to sign in: $errorMsg",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                } else
                    Toast.makeText(
                        this,
                        "Failed to sign in: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
            }
    }

    private fun setSharedPreference() {
        getSharedPreferences("dolling", Context.MODE_PRIVATE).apply {
            with(this.edit()) {
                putBoolean("welcome_slider_already_opened", true)
                putBoolean("is_logged_in_with_email", false)
                apply()
            }
        }
    }

    private fun goToNextActivity() {
        Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            Toast.makeText(
                this@OtpVerificationActivity,
                "Welcome aboard $name",
                Toast.LENGTH_LONG
            ).show()
            startActivity(this)
        }
    }
}