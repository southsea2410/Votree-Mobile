package com.example.votree.users.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.databinding.ActivitySignUpBinding
import com.example.votree.users.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val username = binding.usernameEt.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()
            val fullName = binding.nameEt.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {

                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val user = User(
                                id = firebaseAuth.currentUser!!.uid, // Use the UID
                                email = email,
                                username = username,
                                password = pass,
                                fullName = fullName
                            )

                            storeUserInFirestore(user)
                            createStripeCustomer()
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun storeUserInFirestore(user: User) {
        db.collection("users").document(user.id)
            .set(user)
            .addOnSuccessListener {
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to create user: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun createStripeCustomer() {
        val functions = FirebaseFunctions.getInstance()
        functions.getHttpsCallable("createStripeCustomer")
            .call(mapOf("email" to firebaseAuth.currentUser!!.email))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result?.data as String
                    Toast.makeText(this, "Stripe customer created successfully", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val exception = task.exception
                    Toast.makeText(
                        this,
                        "Failed to create Stripe customer: ${exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}