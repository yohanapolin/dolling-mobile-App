package com.dolling.ui.another

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dolling.R
import com.dolling.databinding.ActivityMapBinding
import com.dolling.listeners.FirestoreListener
import com.dolling.modal.OrderModal
import com.dolling.view_model.another.MapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding
    private lateinit var gMap: GoogleMap
    private val viewModel: MapViewModel by viewModels()

    private lateinit var sellerId: String
    private lateinit var orderId: String
    private lateinit var userId: String
    private lateinit var myLocation: LatLng
    private lateinit var sellerLocation: LatLng

    private var isOrderInfoVisible = false

    companion object {
        const val EXTRA_SELLER_ID = "seller_id"
        const val EXTRA_ORDER_ID = "order_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sellerId = intent.getStringExtra(EXTRA_SELLER_ID)!!
        orderId = intent.getStringExtra(EXTRA_ORDER_ID)!!

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            gMap = it
        }

        binding.apply {
            buttonCall.setOnClickListener {
                Toast.makeText(this@MapActivity, "coming soon", Toast.LENGTH_SHORT).show()
            }

            buttonMessage.setOnClickListener {
                Toast.makeText(this@MapActivity, "coming soon", Toast.LENGTH_SHORT).show()
            }

            simulateSellerMovement.setOnClickListener {
                viewModel.simulateMovingSeller()
            }

            simulateTransDone.setOnClickListener {
                viewModel.simulateTransactionDone(userId)
            }
        }

        getOrder()
    }

    override fun onResume() {
        super.onResume()
        getMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getMyLocation() {
        val location = LocationServices.getFusedLocationProviderClient(this).lastLocation
        location.addOnCompleteListener(this) { task ->
            task.result?.let {
                myLocation = LatLng(it.latitude, it.longitude)
                getSellerLocation("+6285156645193")
            }
        }
    }

    private fun getSellerLocation(sellerId: String) {
        viewModel.getLiveSellerFromFirestore(sellerId)
        viewModel.seller.observe(this, { seller ->
            seller?.let {
                binding.simulateSellerMovement.visibility = View.VISIBLE
                binding.simulateTransDone.visibility = View.VISIBLE

                sellerLocation =
                    LatLng(it.location_point["latitude"]!!, it.location_point["longitude"]!!)

                addMarkerIntoGmaps()

                val distance = countDistanceAndRemainingTime()["distance"] as Int

                getMidLocationAndZoom(distance)

                var remainingTime = countDistanceAndRemainingTime()["remaining_time"] as Int
                remainingTime /= 60

                binding.apply {
                    tvRestaurantName.text = it.store_name
                    tvArrivalTime.text = "Arrive in $remainingTime minutes"
                }
            }
        })
    }

    private fun getOrder() {
        val isLoggedInWithEmail: Boolean
        getSharedPreferences("dolling", Context.MODE_PRIVATE).apply {
            isLoggedInWithEmail = this.getBoolean("is_logged_in_with_email", false)
        }

        userId =
            if (isLoggedInWithEmail) Firebase.auth.currentUser!!.email!! else Firebase.auth.currentUser!!.phoneNumber!!

        viewModel.getLiveOrder(orderId, userId)
        viewModel.order.observe(this, {
            it?.let {
                if (it.status == OrderModal.Companion.StatusType.DONE.value) {
                    viewModel.removeOrderFromSeller(userId, object : FirestoreListener {
                        override fun onSuccessListener(data: Any) {
                            Intent(this@MapActivity, ReviewActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                putExtra(ReviewActivity.EXTRA_SELLER, viewModel.seller.value!!)
                                startActivity(this)
                            }
                        }

                        override fun onFailureListener(errorMsg: String) {
                            Toast.makeText(this@MapActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                binding.apply {
                    tvPaymentType.text = it.payment_method
                    tvOrderPrice.text = "Rp. ${it.total_price}"
                }

                binding.buttonShowMore.setOnClickListener {
                    setOrderInfoVisibility()
                }
            }
        })
    }

    private fun setOrderInfoVisibility() {
        if (isOrderInfoVisible) {
            isOrderInfoVisible = false
            binding.cvViewMore.visibility = View.GONE
        } else {
            isOrderInfoVisible = true
            binding.cvViewMore.visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        viewModel.removeSellerListener()
        viewModel.removeOrderListener()
        super.onStop()
    }

    private fun addMarkerIntoGmaps() {
        gMap.clear()

        val myLocationMarkerOptions =
            MarkerOptions().position(myLocation).icon(
                getBitmapFromDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_my_location)!!
                )
            )

        gMap.addMarker(myLocationMarkerOptions)

        val sellerLocationMarkerOptions =
            MarkerOptions().position(sellerLocation).icon(
                getBitmapFromDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_food_stall)!!
                )
            )

        gMap.addMarker(sellerLocationMarkerOptions)
    }

    private fun countDistanceAndRemainingTime(): HashMap<String, Any> {
        val result = HashMap<String, Any>()

        val myLocationLocation = Location("")
        myLocationLocation.latitude = myLocation.latitude
        myLocationLocation.longitude = myLocation.longitude

        val sellerLocationLocation = Location("")
        sellerLocationLocation.latitude = sellerLocation.latitude
        sellerLocationLocation.longitude = sellerLocation.longitude

        val distance = myLocationLocation.distanceTo(sellerLocationLocation).toInt()
        result["distance"] = distance

        val remainingTime = distance * 2
        result["remaining_time"] = remainingTime

        return result
    }

    private fun getMidLocationAndZoom(distance: Int) {
        val zoomLevel = when {
            distance < 50 -> 20f
            distance in 51..250 -> 18f
            else -> 16f
        }

        val center =
            LatLngBounds.builder().include(myLocation).include(sellerLocation).build().center

        gMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(center, zoomLevel)
        )
    }

    private fun getBitmapFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}