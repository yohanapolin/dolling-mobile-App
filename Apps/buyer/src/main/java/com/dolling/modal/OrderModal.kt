package com.dolling.modal

import com.google.firebase.Timestamp

data class OrderModal(
    val id: String = "",
    val payment_method: String = "cash",
    val item_count: Int = 0,
    val store_name: String = "",
    val status: String = "unconfirmed",
    val total_price: Int = 0,
    val user_id: String = "",
    val seller_id: String = "",
    val created_at: Timestamp = Timestamp.now()
) {
    companion object {
        enum class StatusType(val value: String) {
            REJECTED("rejected"),
            CONFIRMED("confirmed"),
            DONE("done")
        }
    }
}