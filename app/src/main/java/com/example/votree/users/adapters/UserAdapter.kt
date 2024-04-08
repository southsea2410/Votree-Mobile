package com.example.votree.users.adapters

import com.example.votree.users.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class  UserAdapter{

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

    fun getUser(email: String, callback: (List<User>) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val users = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(User::class.java)
                }
                callback(users)
            }
            .addOnFailureListener { callback(emptyList()) }
    }
}