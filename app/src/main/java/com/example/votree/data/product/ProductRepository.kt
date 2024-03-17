package com.example.votree.data.product

import androidx.lifecycle.LiveData

class ProductRepository(private val productDao: ProductDao) {

    val readAllData: LiveData<List<Product>> = productDao.readAllData()

    suspend fun addProduct(product: Product) {
        productDao.insert(product)
    }

}