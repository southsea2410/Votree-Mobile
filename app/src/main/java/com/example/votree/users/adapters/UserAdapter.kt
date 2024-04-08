package com.example.votree.users.adapters

import com.example.votree.users.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserFirestoreAdapter {

    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    fun addUser(user: User, callback: (Boolean) -> Unit) {
        db.collection("users")
            .document(user.id)
            .set(user)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getUser(userId: String, callback: (User?) -> Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                callback(user)
            }
            .addOnFailureListener { callback(null) }
    }
}