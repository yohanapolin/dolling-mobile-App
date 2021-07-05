package com.dolling.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SellerModal(
    val id: String = "",
    val available: Boolean = false,
    val location_point: Map<String, Double> = mapOf("latitude" to 0.0, "longitude" to 0.0),
    val rating: Double = 0.0,
    val rating_total: Int = 0,
    val store_name: String = "",
    val store_photo_url: String = ""
) : Parcelable