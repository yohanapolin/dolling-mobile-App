package com.dolling.modal

data class MenuModal(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val photo_url: String = "",
    val price: Int = 0
) {
    fun convertToLocalModal(): MenuLocalModal {
        return MenuLocalModal(
            id = this.id,
            name = this.name,
            description = this.description,
            photo_url = this.photo_url,
            price = this.price,
            quantity = 0
        )
    }
}