package com.dolling.ui.start

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.dolling.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        removeError(binding.editTextName)
        removeError(binding.editTextNumber)

        binding.buttonDone.setOnClickListener {
            when {
                binding.editTextName.text.isNullOrEmpty() -> binding.editTextName.error =
                    "field tidak boleh kosong!"
                binding.editTextNumber.text.isNullOrEmpty() -> binding.editTextNumber.error =
                    "field tidak boleh kosong!"
                else -> goToNextActivity()
            }
        }
    }

    private fun goToNextActivity() {
        Intent(this, OtpVerificationActivity::class.java).apply {
            this.putExtra(
                OtpVerificationActivity.EXTRA_PHONE_NUMBER,
                binding.editTextNumber.text.toString()
            )
            this.putExtra(
                OtpVerificationActivity.EXTRA_NAME,
                binding.editTextName.text.toString()
            )
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
}