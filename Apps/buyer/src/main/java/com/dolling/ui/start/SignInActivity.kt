package com.dolling.ui.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dolling.R
import com.dolling.databinding.ActivitySignInBinding
import com.dolling.listeners.FirestoreListener
import com.dolling.ui.main.MainActivity
import com.dolling.view_model.start.SignInViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val viewModel: SignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        removeError(binding.editTextName)
        removeError(binding.editTextNumber)

        binding.buttonDone.setOnClickListener {
            when {
                binding.editTextName.text.isNullOrEmpty() -> binding.editTextName.error =
                    "field tidak boleh kosong!"
                binding.editTextNumber.text.isNullOrEmpty() -> binding.editTextNumber.error =
                    "field tidak boleh kosong!"
                else -> goToOTPVerifActivity()
            }
        }

        binding.buttonSignInGoogle.setOnClickListener {
            signInWithGoogleAccount()
        }
    }

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
                                            this@SignInActivity,
                                            "Failed to sign in: $errorMsg",
                                            Toast.LENGTH_LONG
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
                                this@SignInActivity,
                                "Failed to sign in: $errorMsg",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
                } else
                    Toast.makeText(
                        this,
                        "Failed to sign in: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
            }
    }

    private fun goToNextActivity() {
        Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            Toast.makeText(
                this@SignInActivity,
                "Welcome aboard ${Firebase.auth.currentUser!!.displayName}",
                Toast.LENGTH_LONG
            ).show()
            startActivity(this)
        }
    }

    private fun removeError(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                editText.error = null
            }

            override fun afterTextChanged(p0: Editable?) {
                //do nothing
            }
        })
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

    private fun goToOTPVerifActivity() {
        Intent(this, OtpVerificationActivity::class.java).apply {
            this.putExtra(
                OtpVerificationActivity.EXTRA_NAME,
                binding.editTextName.text.toString()
            )
            this.putExtra(
                OtpVerificationActivity.EXTRA_PHONE_NUMBER,
                binding.editTextNumber.text.toString()
            )
            startActivity(this)
        }
    }
}