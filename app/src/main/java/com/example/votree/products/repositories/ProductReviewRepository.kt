package com.example.votree.products.repositories

import com.example.votree.products.models.ProductReview
import com.google.firebase.firestore.FirebaseFirestore

class ProductReviewRepository(private val db: FirebaseFirestore) {

    fun addProductReview(review: ProductReview, transactionId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Step 1: Add the ProductReview to the productReviews collection
        db.collection("productReviews").add(review)
            .addOnSuccessListener { reviewDocumentRef ->
                val reviewId = reviewDocumentRef.id
                // Step 2: Retrieve the transaction to get all the products from productsMap
                db.collection("transactions").document(transactionId).get()
                    .addOnSuccessListener { transactionDocumentSnapshot ->
                        val productsMap = transactionDocumentSnapshot.data?.get("productsMap") as? Map<String, Any> ?: return@addOnSuccessListener
                        // Step 3: For each product in the productsMap, create a reviews subcollection under the products/productId document
                        productsMap.forEach { (productId, _) ->
                            db.collection("products").document(productId).collection("reviews").document(reviewId).set(review)
                                .addOnSuccessListener {
                                    onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    onFailure(e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
