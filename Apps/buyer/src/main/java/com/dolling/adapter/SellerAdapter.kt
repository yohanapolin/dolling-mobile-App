package com.dolling.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolling.R
import com.dolling.databinding.ItemSellerBinding
import com.dolling.glide.GlideApp
import com.dolling.listeners.AdapterOnClickListener
import com.dolling.modal.SellerModal
import com.dolling.utils.Utils
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class SellerAdapter(
    private val adapterOnClickListener: AdapterOnClickListener,
    private val myLocation: LatLng
) : RecyclerView.Adapter<SellerAdapter.ViewHolder>() {

    private val storageRef = Firebase.storage.getReference("store")
    private val list = arrayListOf<SellerModal>()

    fun setList(list: List<SellerModal>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun delList() {
        this.list.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val context: Context,
        private val binding: ItemSellerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setViewValue(seller: SellerModal) {
            binding.apply {
                GlideApp.with(context)
                    .load(storageRef.child(seller.store_photo_url))
                    .placeholder(R.drawable.restaurant_image_placeholder)
                    .into(ivSellerPhoto)

                tvRestaurantName.text = seller.store_name
                tvRatingNumber.text = seller.rating.toString()
                tvRatingCount.text = "${seller.rating_total} Ratings"

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

        fun setOnClickListener(seller: SellerModal) {
            binding.parent.setOnClickListener {
                adapterOnClickListener.onAdapterParentViewClicked(seller)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            parent.context,
            ItemSellerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val seller = list[position]
        holder.setViewValue(seller)
        holder.setOnClickListener(seller)
    }

    override fun getItemCount(): Int = list.size
}