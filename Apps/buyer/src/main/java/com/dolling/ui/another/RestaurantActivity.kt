package com.dolling.ui.another

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolling.R
import com.dolling.adapter.MenuLocalAdapter
import com.dolling.databinding.ActivityRestaurantBinding
import com.dolling.glide.GlideApp
import com.dolling.listeners.FirestoreListener
import com.dolling.modal.SellerModal
import com.dolling.utils.Utils
import com.dolling.view_model.another.RestaurantViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class RestaurantActivity : AppCompatActivity(), MenuLocalAdapter.MenuLocalAdapterListener {

    private lateinit var binding: ActivityRestaurantBinding
    private val viewModel: RestaurantViewModel by viewModels()
    private lateinit var adapterMenuLocal: MenuLocalAdapter

    private lateinit var userId: String
    private lateinit var sellerId: String
    private lateinit var myLocation: LatLng

    private var isFavorited: Boolean = false

    companion object {
        const val EXTRA_SELLER_ID = "extra_seller"
        const val EXTRA_LOCATION_LAT = "extra_location_lat"
        const val EXTRA_LOCATION_LONG = "extra_location_long"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isLoggedInWithEmail: Boolean
        getSharedPreferences("dolling", Context.MODE_PRIVATE).apply {
            isLoggedInWithEmail = this.getBoolean("is_logged_in_with_email", false)
        }
        userId =
            if (isLoggedInWithEmail) Firebase.auth.currentUser!!.email!! else Firebase.auth.currentUser!!.phoneNumber!!
        sellerId = intent.getStringExtra(EXTRA_SELLER_ID)!!
        myLocation = LatLng(
            intent.getDoubleExtra(EXTRA_LOCATION_LAT, 0.0),
            intent.getDoubleExtra(EXTRA_LOCATION_LONG, 0.0)
        )

        setFavoriteButtonOnClickListener()

        viewModel.getSellerFirestore(sellerId)
        viewModel.seller.observe(this, { seller ->
            seller?.let {
                setViewsValue(it)
            }
        })

        viewModel.getIsRestaurantFavorited(userId, sellerId, object : FirestoreListener {
            override fun onSuccessListener(data: Any) {
                isFavorited = data as Boolean
                setFavoriteButtonResource()
            }

            override fun onFailureListener(errorMsg: String) {
                Toast.makeText(this@RestaurantActivity, errorMsg, Toast.LENGTH_SHORT).show()
            }
        })

        setMenuAdapter()

        viewModel.getMenuListFromFirestore(sellerId)
        viewModel.menuList.observe(this, { menuList ->
            if (!menuList.isNullOrEmpty())
                adapterMenuLocal.setList(menuList)
            else adapterMenuLocal.delList()
        })

        binding.btnGoToCheckout.setOnClickListener {
            CheckoutActivity.setViewModel(viewModel)
            startActivity(Intent(this, CheckoutActivity::class.java))
        }
    }

    private fun setFavoriteButtonResource() {
        val resource =
            if (isFavorited) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24

        binding.ivFavorite.setImageDrawable(ContextCompat.getDrawable(this, resource))
    }

    private fun setFavoriteButtonOnClickListener() {
        binding.ivFavorite.setOnClickListener {
            if (isFavorited)
                viewModel.deleteRestaurantFromFavorite(
                    userId,
                    sellerId,
                    object : FirestoreListener {
                        override fun onSuccessListener(data: Any) {
                            isFavorited = !isFavorited
                            setFavoriteButtonResource()
                        }

                        override fun onFailureListener(errorMsg: String) {
                            Toast.makeText(this@RestaurantActivity, errorMsg, Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
            else
                viewModel.setRestaurantIntoFavorite(userId, sellerId, object : FirestoreListener {
                    override fun onSuccessListener(data: Any) {
                        isFavorited = !isFavorited
                        setFavoriteButtonResource()
                    }

                    override fun onFailureListener(errorMsg: String) {
                        Toast.makeText(this@RestaurantActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun setMenuAdapter() {
        adapterMenuLocal = MenuLocalAdapter(this, sellerId)
        binding.rvMenus.apply {
            layoutManager = LinearLayoutManager(this@RestaurantActivity)
            adapter = adapterMenuLocal
        }
    }

    private fun setViewsValue(seller: SellerModal) {
        binding.apply {
            val storageRef = Firebase.storage.getReference("store")

            GlideApp.with(this@RestaurantActivity)
                .load(storageRef.child(seller.store_photo_url))
                .placeholder(R.drawable.restaurant_image_placeholder)
                .into(ivSellerPhoto)

            tvRestaurantName.text = seller.store_name
            tvRatingCount.text = seller.rating.toString()
            tvRatingNumber.text = "(${seller.rating_total} Ratings)"

            val distance = Utils.countDistanceFromTwoGeoPoints(
                myLocation,
                LatLng(
                    seller.location_point["latitude"]!!,
                    seller.location_point["longitude"]!!
                )
            )
            tvSellerDistance.text = "${distance}M"
        }
    }

    override fun onClickListener(position: Int) {
        viewModel.updateQuantityMenu(position, isIncreaseQty = true)
    }

    override fun onLongClickListener(position: Int) {
        viewModel.updateQuantityMenu(position, isIncreaseQty = false)
    }
}