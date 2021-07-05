package com.dolling.ui.main.favorite

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolling.adapter.SellerAdapter
import com.dolling.databinding.FragmentFavoriteBinding
import com.dolling.listeners.AdapterOnClickListener
import com.dolling.listeners.FirestoreListener
import com.dolling.modal.SellerModal
import com.dolling.ui.another.RestaurantActivity
import com.dolling.view_model.main.FavoriteViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FavoriteFragment :
    Fragment(),
    AdapterOnClickListener {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapterFavoriteAvailable: SellerAdapter
    private lateinit var adapterFavoriteNotAvailable: SellerAdapter
    private val viewModel: FavoriteViewModel by viewModels()

    private var myLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        checkLocationPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            Toast.makeText(context, "please restart application", Toast.LENGTH_SHORT).show()
        } else getMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getMyLocation() {
        if (checkIsLocationEnabled()) {
            val location =
                LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation
            location.addOnCompleteListener(requireActivity()) { task ->
                task.result?.let {
                    myLocation = LatLng(it.latitude, it.longitude)
                    setNearestAdapter()
                }
            }
        } else
            Toast.makeText(context, "please enable location first", Toast.LENGTH_SHORT).show()
    }

    private fun checkIsLocationEnabled(): Boolean {
        val locManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun setNearestAdapter() {
        adapterFavoriteAvailable = SellerAdapter(this, myLocation!!)
        adapterFavoriteNotAvailable = SellerAdapter(this, myLocation!!)

        binding.rvRestaurant.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterFavoriteAvailable
        }
        binding.rvUnavaiableRestaurant.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterFavoriteNotAvailable
        }

        getFavoriteSeller()
    }

    private fun getFavoriteSeller() {
        val isLoggedInWithEmail: Boolean
        requireContext().getSharedPreferences("dolling", Context.MODE_PRIVATE).apply {
            isLoggedInWithEmail = this.getBoolean("is_logged_in_with_email", false)
        }

        val userId =
            if (isLoggedInWithEmail) Firebase.auth.currentUser!!.email!! else Firebase.auth.currentUser!!.phoneNumber!!

        viewModel.getLiveFavoriteListIdFromFirestore(userId, object : FirestoreListener {
            override fun onSuccessListener(data: Any) {
                viewModel.favoriteListAvailable.observe(viewLifecycleOwner, { list ->
                    list?.let {
                        adapterFavoriteAvailable.setList(it)
                    }
                })

                viewModel.favoriteListNotAvailable.observe(viewLifecycleOwner, { list ->
                    list?.let {
                        adapterFavoriteNotAvailable.setList(it)
                    }
                })
            }

            override fun onFailureListener(errorMsg: String) {
                adapterFavoriteAvailable.delList()
                adapterFavoriteNotAvailable.delList()
            }
        })
    }

    override fun onAdapterParentViewClicked(_object: Any) {
        val seller = _object as SellerModal

        if (seller.available)
            if (checkIsLocationEnabled()) {
                Intent(requireContext(), RestaurantActivity::class.java).apply {
                    putExtra(RestaurantActivity.EXTRA_SELLER_ID, seller.id)
                    putExtra(RestaurantActivity.EXTRA_LOCATION_LAT, myLocation!!.latitude)
                    putExtra(
                        RestaurantActivity.EXTRA_LOCATION_LONG,
                        myLocation!!.longitude
                    )
                    startActivity(this)
                }
            } else
                Toast.makeText(requireContext(), "please enable location first", Toast.LENGTH_SHORT)
                    .show()
    }
}