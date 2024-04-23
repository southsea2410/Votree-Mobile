package com.example.votree.users.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.votree.users.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _user = MutableLiveData<User?>()
    val user: MutableLiveData<User?> get() = _user

    init {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserProfile(userId)
        }
    }

    private fun loadUserProfile(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                if (user != null) {
                    _user.value = user
                }
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    suspend fun updateUserProfile(updatedUser: User) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            withContext(Dispatchers.IO) {
                firestore.collection("users").document(userId).set(updatedUser)
            }
        }
    }
}