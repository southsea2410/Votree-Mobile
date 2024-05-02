package com.example.votree.users.repositories

import com.example.votree.users.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore) {
    private val usersCollection = db.collection("users")

    suspend fun getUser(userId: String): User? {
        val snapshot = usersCollection.document(userId).get().await()
        return snapshot.toObject(User::class.java)
    }

    suspend fun updateUser(userId: String, user: User) {
        usersCollection.document(userId).set(user).await()
    }

    suspend fun updateToStore(userId: String, storeId: String) {
        usersCollection.document(userId).update(
            mapOf(
                "storeId" to storeId,
                "role" to "store"
            )
        ).await()
    }
}