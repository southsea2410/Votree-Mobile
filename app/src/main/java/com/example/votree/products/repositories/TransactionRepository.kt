package com.example.votree.products.repositories

import android.util.Log
import com.example.votree.products.models.Transaction
import com.google.firebase.firestore.FirebaseFirestore
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
}