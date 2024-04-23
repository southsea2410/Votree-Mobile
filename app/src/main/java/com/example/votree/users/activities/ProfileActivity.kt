package com.example.votree.users.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.votree.databinding.ActivityUserProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private val viewModel: UserProfileViewModel by viewModels()

    override fun onCreate(savedname: Bundle?) {
        super.onCreate(savedname)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.user.observe(this, Observer { user ->
            binding.user = user
        })

        binding.saveButton.setOnClickListener {
            val updatedUser = binding.user!!
            viewModel.updateUserProfile(updatedUser)
        }
    }
}