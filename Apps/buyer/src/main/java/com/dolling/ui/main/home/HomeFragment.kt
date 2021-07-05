package com.dolling.ui.main.home

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
import com.dolling.databinding.FragmentHomeBinding
import com.dolling.listeners.AdapterOnClickListener
import com.dolling.modal.SellerModal
import com.dolling.ui.another.RestaurantActivity
import com.dolling.view_model.main.HomeViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class HomeFragment :
    Fragment(),
    AdapterOnClickListener {

    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var nearestAdapter: SellerAdapter

    private var myLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClickListeners()
    }

    override fun onAdapterParentViewClicked(_object: Any) {
        if (checkIsLocationEnabled()) {
            val seller = _object as SellerModal
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

    override fun onResume() {
        super.onResume()
        checkLocationPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        viewModel.removeSellerListener()
    }

    private fun setOnClickListeners() {
        binding.btnSetLocation.setOnClickListener {
            Toast.makeText(context, "coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.ivCamera.setOnClickListener {
            Toast.makeText(context, "coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.ivFilterIcon.setOnClickListener {
            Toast.makeText(context, "coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.frameLayoutChicken.setOnClickListener {
            Toast.makeText(context, "coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.frameLayoutOffers.setOnClickListener {
            Toast.makeText(context, "coming soon", Toast.LENGTH_SHORT).show()
        }
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

    private fun setNearestAdapter() {
        nearestAdapter = SellerAdapter(this, myLocation!!)
        binding.rvNearestRestaurant.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = nearestAdapter
        }
        getNearestSeller()
    }

    private fun getNearestSeller() {
        viewModel.getLiveSellerListFromFirestore()
        viewModel.sellerList.observe(viewLifecycleOwner, { list ->
            if (!list.isNullOrEmpty())
                nearestAdapter.setList(list)
            else nearestAdapter.delList()
        })
    }

    private fun checkIsLocationEnabled(): Boolean {
        val locManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}