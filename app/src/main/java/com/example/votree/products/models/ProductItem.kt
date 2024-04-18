package com.example.votree.products.models

sealed class ProductItem {
    data class ProductHeader(val shopName: String) : ProductItem()
    data class ProductData(val product: Product, val quantity: Int) : ProductItem()
}
