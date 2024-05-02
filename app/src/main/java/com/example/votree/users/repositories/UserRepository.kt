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
}