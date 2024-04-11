package com.example.votree.products.repositories

import android.util.Log
import com.example.votree.products.models.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


class TransactionRepository(private val db: FirebaseFirestore) {
    suspend fun createTransaction(transaction: Transaction) {
        db.collection("transactions").add(transaction).await()
    }

    fun createAndUpdateTransaction(transaction: Transaction) {
        val db = FirebaseFirestore.getInstance()
        val transactionsCollection = db.collection("transactions")

        // Step 2: Push the Transaction to Firestore
        transactionsCollection.add(transaction)
            .addOnSuccessListener { documentReference ->
                val generatedId = documentReference.id

                // Step 3 & 4: Retrieve the Generated ID and Update the Transaction
                transactionsCollection.document(generatedId).update("id", generatedId)
                    .addOnSuccessListener {
                        Log.d("TransactionRepository", "Transaction updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.w("TransactionRepository", "Error updating transaction", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("TransactionRepository", "Error adding transaction", e)
            }
    }

    suspend fun fetchShopName(storeId: String): String? {
        val db = FirebaseFirestore.getInstance()
        val storeDoc = db.collection("stores").document(storeId).get().await()
        return storeDoc.getString("storeName") // Assuming the field containing the shop name is called "shopName"
    }

    fun calculateTotalQuantity(productsMap: MutableMap<String, Int>): Int {
        return productsMap.values.sum()
    }

    suspend fun calculateTotalPrice(productsMap: MutableMap<String, Int>): Double {
        // Assuming each product has a price field
        return productsMap.entries.sumByDouble { (productId, quantity) ->
            // Fetch the product price from Firestore
            val productDoc = db.collection("products").document(productId).get().await()
            val price = productDoc.getDouble("price") ?: 0.0
            price * quantity
        }
    }

    // Function to fetch transactions for a specific user
    fun getTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
        val transactionsCollection = db.collection("transactions")
        val query = transactionsCollection.whereEqualTo("customerId", userId)

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Close the channel on error
                return@addSnapshotListener
            }
            val transactions = snapshot?.documents?.mapNotNull { it.toObject(Transaction::class.java) } ?: emptyList()
            trySend(transactions).isSuccess
        }

        awaitClose { listenerRegistration.remove() }
    }
}