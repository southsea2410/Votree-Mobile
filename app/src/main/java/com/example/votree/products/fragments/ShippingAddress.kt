package com.example.votree.products.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.votree.databinding.FragmentShippingAddressBinding
import com.example.votree.products.models.ShippingAddress
import com.example.votree.products.view_models.ShippingAddressViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ShippingAddressFragment : Fragment() {
    private lateinit var binding: FragmentShippingAddressBinding // View Binding
    private lateinit var viewModel: ShippingAddressViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShippingAddressBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ShippingAddressViewModel::class.java)

        // Set up data observation and button listener
        setUpObservers()
        binding.confirmBtn.setOnClickListener {
            onSaveAddress()
        }

        return binding.root
    }

    private fun setUpObservers() {
        // Observe the selectedShippingAddress LiveData
        viewModel.selectedShippingAddress.observe(viewLifecycleOwner) { address ->
            if (address != null) {
                viewModel.prefillForm(address)
                fillForm(address)
            }
        }
    }

    private fun fillForm(address: ShippingAddress) {
        binding.recipientNameEt.setText(address.recipientName)
        binding.recipentAddressEt.setText(address.recipientAddress)
        binding.recipientPhoneNumberEt.setText(address.recipientPhoneNumber)
    }

    private fun onSaveAddress() {
        val recipientName = binding.recipientNameEt.text.toString().trim()
        val phoneNumber = binding.recipientPhoneNumberEt.text.toString().trim()
        val addressText = binding.recipentAddressEt.text.toString().trim()
        val isDefault = binding.setAsDefaultSwitch.isChecked

        // Basic Input validation
        if (recipientName.isBlank() || phoneNumber.isBlank() || addressText.isBlank()) {
            Snackbar.make(binding.root, "Please fill all fields", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Create a ShippingAddress object
        val shippingAddress = ShippingAddress(
            recipientName = recipientName,
            recipientPhoneNumber = phoneNumber,
            recipientAddress = addressText,
            default = isDefault
        )

        lifecycleScope.launch {
            try {
                viewModel.saveShippingAddress(shippingAddress)
                viewModel.isSaveAddressSuccessful.observe(viewLifecycleOwner) { isSuccessful ->
                    if (isSuccessful) {
                        Log.d(
                            "ShippingAddressFragment",
                            "Address saved successfully ${viewModel.selectedShippingAddress.value}"
                        )
                        Snackbar.make(
                            binding.root,
                            "Address saved successfully",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        // Navigate back to the previous fragment
                        findNavController().popBackStack()
                    } else {
                        Snackbar.make(binding.root, "Error saving address", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error saving address", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
