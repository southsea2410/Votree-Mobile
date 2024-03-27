package com.example.votree.products.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.votree.products.models.Product
import com.google.firebase.firestore.FirebaseFirestore

class ProductViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")

    private val _products: MutableLiveData<List<Product>> = MutableLiveData()
    val products: LiveData<List<Product>>
        get() = _products

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Handle error
                Log.e(TAG, "Error fetching products: $error")
                return@addSnapshotListener
            }

            val productsList = mutableListOf<Product>()
            snapshot?.let {
                for (doc in it.documents) {
                    val product = doc.toObject(Product::class.java)
                    product?.let { product ->
                        productsList.add(product)
                    }
                }
            }
            _products.value = productsList
        }
    }

    companion object {
        private const val TAG = "ProductViewModel"
    }
}

