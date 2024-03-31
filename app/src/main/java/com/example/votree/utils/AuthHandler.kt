package com.example.votree.utils

import android.content.Context
import android.content.Intent
import com.example.votree.users.activities.SignInActivity
import com.google.firebase.auth.FirebaseAuth

class AuthHandler(private val context: Context) {

    private lateinit var firebaseAuth: FirebaseAuth

    fun checkAuthentication() {
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Check if user is signed in (non-null) and update UI accordingly
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            // User is not signed in, redirect to SignInActivity
            val intent = Intent(context, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }
}
