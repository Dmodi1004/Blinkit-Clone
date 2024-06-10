package com.example.blinkitclone.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("CartProducts")
data class CartProducts(
    @PrimaryKey
    val productId: String = "random",
    var productTitle: String? = null,
    var productQuantity: String? = null,
    var productPrice: String? = null,
    var productCount: Int? = null,
    var productStock: Int? = null,
    var productImage: String? = null,
    var productCategory: String? = null,
    var adminUid: String? = null
)