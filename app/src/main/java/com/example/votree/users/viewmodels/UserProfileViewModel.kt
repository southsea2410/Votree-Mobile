package com.example.votree.users.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.example.votree.users.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val userLiveData = MutableLiveData<User?>()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: return

        firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                userLiveData.postValue(user)
            }
        }
    }

        fun updateUser(data: Map<String, Any>) {
            val userId = auth.currentUser?.uid ?: return

            firestore.collection("users").document(userId).update(data).addOnSuccessListener {
                loadUserData()
            }
        }
}
