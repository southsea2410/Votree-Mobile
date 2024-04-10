package com.example.votree.products.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cart(
    var id: String = "",
    val userId: String = "",
    val productsMap: MutableMap<String, Int> = mutableMapOf(),
    val totalPrice: Double = 0.0
) : Parcelable
