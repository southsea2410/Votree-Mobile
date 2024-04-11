package com.example.votree.users.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.votree.databinding.FragmentUserProfileBinding
import com.example.votree.users.activities.OrderHistoryActivity
import com.example.votree.users.activities.RegisterToSeller
import com.example.votree.users.activities.SignInActivity
import com.example.votree.utils.AuthHandler
import com.example.votree.utils.RoleManagement

class UserProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        setupLogoutButton(binding)
        setupOrderHistoryButton(binding)

        RoleManagement.checkUserRole(firebaseAuth = AuthHandler.firebaseAuth, onSuccess = {
            if (it == "user") setupRegisterToSellerButton(binding)
            else binding.registerToSellerBtn.isEnabled = false
        })

        return binding.root
    }

    private fun setupRegisterToSellerButton(binding: FragmentUserProfileBinding) {
        // Call function registerToSeller of SignInActivity
        binding.registerToSellerBtn.setOnClickListener {
            val intent = Intent(this.context, RegisterToSeller::class.java)
            startActivityForResult(intent, RegisterToSeller.REGISTER_TO_SELLER_CODE)
        }
    }

    private fun setupLogoutButton(binding: FragmentUserProfileBinding) {
        // Call function logout of SignInActivity
        binding.logoutButton.setOnClickListener {
            SignInActivity().signOut()
            val intent = Intent(this.context, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupOrderHistoryButton(binding: FragmentUserProfileBinding) {
        // Call function userProfile of SignInActivity
        binding.orderHistoryBtn.setOnClickListener {
            val intent = Intent(this.context, OrderHistoryActivity::class.java)
            startActivity(intent)
        }
    }
}