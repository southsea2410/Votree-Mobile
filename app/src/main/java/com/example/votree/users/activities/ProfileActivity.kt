package com.example.votree.users.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.votree.R
import com.example.votree.users.viewmodels.UserProfileViewModel

class ProfileActivity : AppCompatActivity() {
    private lateinit var userProfileViewModel: UserProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_personal_account)

        userProfileViewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)

        val nameEditText = findViewById<EditText>(R.id.Name)
        val phoneEditText = findViewById<EditText>(R.id.Phone)
        val addressEditText = findViewById<EditText>(R.id.Address)
        val emailEditText = findViewById<EditText>(R.id.Email)

        userProfileViewModel.userLiveData.observe(this, Observer { user ->
            user?.let {
                nameEditText.setText(it.fullName)
                phoneEditText.setText(it.phoneNumber)
                addressEditText.setText(it.address)
                emailEditText.setText(it.email)
            }
        })

        val submitButton = findViewById<Button>(R.id.SubmitBtn)
        submitButton.setOnClickListener {
            val updatedData = mapOf(
                "fullName" to nameEditText.text.toString(),
                "phoneNumber" to phoneEditText.text.toString(),
                "address" to addressEditText.text.toString(),
                "email" to emailEditText.text.toString()
            )

            userProfileViewModel.updateUser(updatedData)

            Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
        }
    }
}