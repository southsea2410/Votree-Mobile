package com.example.votree.users.repositories

import android.util.Log
import com.example.votree.products.repositories.ProductReviewRepository
import com.example.votree.users.models.Store
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storeCollection = db.collection("stores")

    suspend fun createNewStore(store: Store, userId: String) {
        val documentReference = storeCollection.add(store).await()
        val storeId = documentReference.id
        store.id = storeId

        val userDocumentReference = db.collection("users").document(userId)
        userDocumentReference.update("storeId", storeId).await()
    }

    fun fetchStoreById(
        storeId: String,
        onSuccess: (com.example.votree.users.models.Store) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        storeCollection.document(storeId).get()
            .addOnSuccessListener { document ->
                val store = document.toObject(Store::class.java)
                if (store != null) {
                    onSuccess(store)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    suspend fun fetchStore(storeId: String): Store {
        val document = storeCollection.document(storeId).get().await()
        return document.toObject(Store::class.java) ?: throw RuntimeException("Store not found")
    }

    suspend fun getNumberOfProductsOfStore(storeId: String): Int {
        try {
            val documents = db.collection("products")
                .whereEqualTo("storeId", storeId)
                .get()
                .await() // Using Kotlin coroutines to wait for the result

            return documents.size()
        } catch (e: Exception) {
            Log.e("StoreRepository", "Failed to get number of products", e)
            throw RuntimeException("Failed to get number of products", e)
        }
    }

    suspend fun getAverageProductRating(storeId: String): Float {
        try {
            val documents = db.collection("products")
                .whereEqualTo("storeId", storeId)
                .get()
                .await() // Using Kotlin coroutines to wait for the result

            var totalRating = 0f
            var totalReviews = 0

            documents.forEach { document ->
                val productId = document.id
                val productReviewRepository = ProductReviewRepository(db)

                try {
                    val rating = productReviewRepository.getProductRating(productId)
                    // If product has not review, we not calculate on its
                    if (rating != -1f) {
                        totalRating += rating
                        totalReviews++
                    }
                } catch (e: Exception) {
                    Log.d("StoreRepository", "Failed to get rating for product $productId", e)
                    throw e // Rethrow the exception to be handled by the outer catch block
                }
            }

            if (totalReviews == 0) {
                return 0f
            }

            return totalRating / totalReviews
        } catch (e: Exception) {
            Log.e("StoreRepository", "Failed to calculate average rating", e)
            throw RuntimeException("Failed to calculate average rating", e)
        }
    }
}