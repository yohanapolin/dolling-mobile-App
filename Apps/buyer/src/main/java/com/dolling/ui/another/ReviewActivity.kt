package com.dolling.ui.another

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dolling.R
import com.dolling.databinding.ActivityReviewBinding
import com.dolling.glide.GlideApp
import com.dolling.listeners.FirestoreListener
import com.dolling.modal.ReviewModal
import com.dolling.modal.SellerModal
import com.dolling.ui.main.MainActivity
import com.dolling.view_model.another.ReviewViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private val viewModel: ReviewViewModel by viewModels()
    private lateinit var seller: SellerModal

    companion object {
        const val EXTRA_SELLER = "extra_seller"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        seller = intent.getParcelableExtra(EXTRA_SELLER)!!

        binding.apply {
            val storageRef = Firebase.storage.getReference("store")

            GlideApp.with(this@ReviewActivity)
                .load(storageRef.child(seller.store_photo_url))
                .placeholder(R.drawable.food_image_placeholder)
                .into(ivFoodImage)

            tvRestaurantName.text = seller.store_name
            tvRatingNumber.text = seller.rating.toString()
            tvRatingCount.text = "(${seller.rating_total} Ratings)"
        }

        viewModel.getReviewListFromFirestore(seller.id)

        binding.buttonDone.setOnClickListener {
            if (isRatingBarNotEmpty()) {
                val description =
                    if (binding.reviewDesc.text.isNullOrEmpty()) "" else binding.reviewDesc.text

                val review = ReviewModal(
                    description.toString(),
                    binding.ratingBarRestaurant.rating.toDouble()
                )

                viewModel.insertReviewIntoFirestore(review, seller.id, object : FirestoreListener {
                    override fun onSuccessListener(data: Any) {
                        backToMainActivity()
                    }

                    override fun onFailureListener(errorMsg: String) {
                        Toast.makeText(this@ReviewActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        binding.buttonCancel.setOnClickListener {
            backToMainActivity()
        }
    }

    private fun isRatingBarNotEmpty(): Boolean {
        return binding.ratingBarRestaurant.numStars != 0
    }

    private fun backToMainActivity() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
    }
}