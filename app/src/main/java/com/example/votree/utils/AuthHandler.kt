package com.example.votree.utils

import android.content.Context
import android.content.Intent
import com.example.votree.users.activities.SignInActivity
import com.google.firebase.auth.FirebaseAuth

class AuthHandler(private val context: Context) {

    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val isUserAuthenticated: Boolean
        get() = firebaseAuth.currentUser != null

    fun redirectToSignIn() {
        // User is not signed in, redirect to SignInActivity
        val intent = Intent(context, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    fun storeUserIdInSharedPreferences() {
        firebaseAuth.currentUser?.uid?.let { userId ->
            val sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("uid", userId)
            editor.apply()
        }
    }
}
