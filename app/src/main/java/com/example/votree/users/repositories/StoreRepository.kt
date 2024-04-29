package com.example.votree.users.repositories

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

    fun fetchStores(
        onSuccess: (List<com.example.votree.models.Store>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        storeCollection.get()
            .addOnSuccessListener { documents ->
                val storeList = documents.toObjects(com.example.votree.models.Store::class.java)
                onSuccess(storeList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun fetchStoreById(
        storeId: String,
        onSuccess: (com.example.votree.models.Store) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        storeCollection.document(storeId).get()
            .addOnSuccessListener { document ->
                val store = document.toObject(com.example.votree.models.Store::class.java)!!
                onSuccess(store)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    suspend fun getStoreName(storeId: String): String {
        val document = storeCollection.document(storeId).get().await()
        return document.getString("storeName") ?: ""
    }
}