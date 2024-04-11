package com.example.votree.products.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository(private val db: FirebaseFirestore) {
    suspend fun updateProductInventory(productId: String, quantityPurchased: Int) {
        val productRef = db.collection("products").document(productId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(productRef)
            val newInventory = snapshot.getLong("inventory")!! - quantityPurchased
            transaction.update(productRef, "inventory", newInventory)
        }.await()
    }
}