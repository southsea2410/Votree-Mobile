package com.example.votree.users.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.votree.databinding.FragmentUserProfileBinding
import com.example.votree.users.activities.OrderHistoryActivity
import com.example.votree.users.activities.RegisterToSeller
import com.example.votree.users.activities.SignInActivity
import com.example.votree.users.viewmodels.UserProfileViewModel
import com.example.votree.utils.AuthHandler
import com.example.votree.utils.RoleManagement

class UserProfileFragment : Fragment() {
    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var viewModel: UserProfileViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)

        setupLogoutButton(binding)
        setupOrderHistoryButton(binding)

        RoleManagement.checkUserRole(firebaseAuth = AuthHandler.firebaseAuth, onSuccess = {
            if (it == "user") setupRegisterToSellerButton(binding)
            else binding.registerToSellerBtn.isEnabled = false
        })

        setupUpdateButton()
        observeUserData()

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

    private fun updateUserToSeller(role: String) {
        // If RegisterToSeller activity is successful, and return the role as store, then update the user role to store
        RoleManagement.updateUserRole(AuthHandler.firebaseAuth, role)
    }

    private fun observeUserData() {
        viewModel.userLiveData.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                // Cập nhật thông tin người dùng trong các EditText
                binding.Name.setText(user.fullName)
                binding.Phone.setText(user.phoneNumber)
                binding.Address.setText(user.address)
                binding.Email.setText(user.email)
            }
        })
    }

    private fun setupUpdateButton() {
        binding.SubmitBtn.setOnClickListener {
            val updatedData = mutableMapOf<String, Any>()

            // Lấy dữ liệu từ các EditText và lưu vào updatedData nếu có thay đổi
            val fullName = binding.Name.text.toString()
            if (fullName.isNotEmpty()) updatedData["fullName"] = fullName

            val phoneNumber = binding.Phone.text.toString()
            if (phoneNumber.isNotEmpty()) updatedData["phoneNumber"] = phoneNumber

            val address = binding.Address.text.toString()
            if (address.isNotEmpty()) updatedData["address"] = address

            val email = binding.Email.text.toString()
            if (email.isNotEmpty()) updatedData["email"] = email

            // Kiểm tra thay đổi mật khẩu
            val newPassword = binding.NewPass.text.toString()
            val confirmNewPassword = binding.ConPass.text.toString()

            if (newPassword.isNotEmpty() && newPassword == confirmNewPassword) {
                // Cập nhật mật khẩu
                AuthHandler.firebaseAuth.currentUser?.updatePassword(newPassword)
            }

            viewModel.updateUser(updatedData)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RegisterToSeller.REGISTER_TO_SELLER_CODE && resultCode == RESULT_OK) {
            val role = data?.getStringExtra("role")
            if (role == "store") {
                updateUserToSeller(role)

                SignInActivity().signOut()
                val intent = Intent(this.context, SignInActivity::class.java)
                startActivity(intent)
            }
        }
    }
}