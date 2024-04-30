package com.example.votree.users.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.votree.R
import com.example.votree.databinding.FragmentUserProfileBinding
import com.example.votree.users.activities.OrderHistoryActivity
import com.example.votree.users.activities.RegisterToSeller
import com.example.votree.users.activities.SignInActivity
import com.example.votree.users.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupClickListeners()
        loadUserProfileDetailsFragment()
    }

    private fun setupView(){
        val userRepository = UserRepository(FirebaseFirestore.getInstance())
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        lifecycleScope.launch {
            val user = userRepository.getUser(userId)
            user?.let {
                binding.userfullNameTv.text = user.fullName
                binding.userRoleTv.text = user.role

                if (user.role == "store") {
                    disableBecomeSeller()
                }
            }
        }
    }

    private fun disableBecomeSeller() {
        // Disable the becomeSeller layout
        binding.becomeSellerLayout.isEnabled = false
        // Change the text color to gray
        binding.becomeSellerTv.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.md_theme_outlineVariant)
        )
    }

    private fun loadUserProfileDetailsFragment() {
        val userProfileDetailsFragment = UserProfileDetailsFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.userProfileDetail_fcv, userProfileDetailsFragment)
            .commit()
    }

    private fun setupClickListeners() {
        binding.settingLayout.setOnClickListener {
            // Navigate to Settings
            navigateToSettings()
        }

        binding.ordersLayout.setOnClickListener {
            // Navigate to Orders
            navigateToOrders()
        }

        binding.becomeSellerLayout.setOnClickListener {
            // Navigate to Become Seller
            navigateToBecomeSeller()
        }

        binding.logoutBtn.setOnClickListener {
            // Navigate to Sign In Activity
            navigateLogout()
        }
    }

    private fun navigateToSettings() {
        // Implement navigation logic to User Profile Settings Fragment
        val action = UserProfileFragmentDirections.actionUserProfileFragmentToUserProfileSettingFragment()
        findNavController().navigate(action)
    }

    private fun navigateToOrders() {
        val intent = Intent(context, OrderHistoryActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToBecomeSeller() {
        val intent = Intent(context, RegisterToSeller::class.java)
        startActivity(intent)
    }

    private fun navigateLogout(){
        SignInActivity().signOut()
        val intent = Intent(this.context, SignInActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}