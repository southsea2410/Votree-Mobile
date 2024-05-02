package com.example.votree.products.repositories

import com.example.votree.products.models.ProductReview
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductReviewRepository(private val db: FirebaseFirestore) {

    fun addProductReview(review: ProductReview, transactionId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        addReviewToCollection(review, transactionId, onSuccess, onFailure)
    }

    private fun addReviewToCollection(review: ProductReview, transactionId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("productReviews").add(review)
            .addOnSuccessListener { reviewDocumentRef ->
                val reviewId = reviewDocumentRef.id
                review.id = reviewId
                updateReviewDocument(review, reviewId, transactionId, onSuccess, onFailure)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun updateReviewDocument(review: ProductReview, reviewId: String, transactionId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("productReviews").document(reviewId).set(review)
            .addOnSuccessListener {
                updateTransactionWithReviewId(transactionId, reviewId, onSuccess, onFailure)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun updateTransactionWithReviewId(transactionId: String, reviewId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("transactions").document(transactionId).update("reviewId", reviewId)
            .addOnSuccessListener {
                retrieveTransactionToUpdateProducts(transactionId, reviewId, onSuccess, onFailure)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun retrieveTransactionToUpdateProducts(transactionId: String, reviewId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("transactions").document(transactionId).get()
            .addOnSuccessListener { transactionDocumentSnapshot ->
                val productsMap = transactionDocumentSnapshot.data?.get("productsMap") as? Map<String, Any> ?: return@addOnSuccessListener
                updateProductsReviews(productsMap, reviewId, onSuccess, onFailure)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun updateProductsReviews(productsMap: Map<String, Any>, reviewId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val productIds = productsMap.keys
        for (productId in productIds) {
            db.collection("products").document(productId).collection("reviews").document(reviewId).set(mapOf("reviewId" to reviewId))
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        }
    }

    suspend fun getProductRating(productId: String): Float {
        val reviewsSnapshot =
            db.collection("products").document(productId).collection("reviews").get().await()
        var totalRating = 0f
        var totalReviews = 0
        for (reviewDocument in reviewsSnapshot.documents) {
            val review = reviewDocument.toObject(ProductReview::class.java)
            review?.let {
                totalRating += it.rating
                totalReviews++
            }
        }
        if (totalReviews == 0) return -1f

        return totalRating / totalReviews
    }

    suspend fun fetchReviewByTransactionId(transactionId: String, userId: String): ProductReview? {
        val reviewSnapshot = db.collection("productReviews")
            .whereEqualTo("transactionId", transactionId)
            .whereEqualTo("userId", userId)
            .get()
            .await()
        return reviewSnapshot.documents.firstOrNull()?.toObject(ProductReview::class.java)
    }

    fun updateReview(review: ProductReview, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("productReviews").document(review.id).set(review)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
