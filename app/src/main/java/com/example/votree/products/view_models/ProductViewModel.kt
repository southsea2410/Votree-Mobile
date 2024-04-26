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

    fun getProductById(productId: String): LiveData<Product?> {
        val productLiveData = MutableLiveData<Product?>()

        productsCollection.document(productId).get().addOnSuccessListener { documentSnapshot ->
            Log.d(TAG, "Fetched product with ID: $documentSnapshot")
            val product = documentSnapshot.toObject(Product::class.java)
            productLiveData.value = product
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error fetching product with ID: $productId", e)
            productLiveData.value = null
        }

        return productLiveData
    }

    // Function to get product price by id
    fun getProductPriceById(productId: String): Double? {
        val product = _products.value?.find { it.id == productId }
        Log.d("_product", _products.value.toString())
        return product?.price
    }

    companion object {
        private const val TAG = "ProductViewModel"
    }
}

