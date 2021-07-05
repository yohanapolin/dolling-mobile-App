package com.dolling.modal

data class OrderedMenuModal(
    val id: String = "",
    val name: String = "",
    val photo_url: String = "",
    val price: Int = 0,
    val quantity: Int = 0
)