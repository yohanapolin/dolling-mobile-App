package com.dolling.modal

class MenuLocalModal(
    val id: String,
    val name: String,
    val description: String,
    val photo_url: String,
    val price: Int,
    var quantity: Int
) {
    fun convertToOrderedMenuModal(): OrderedMenuModal {
        return OrderedMenuModal(
            id = this.id,
            name = this.name,
            photo_url = this.photo_url,
            price = this.price,
            quantity = this.quantity
        )
    }
}