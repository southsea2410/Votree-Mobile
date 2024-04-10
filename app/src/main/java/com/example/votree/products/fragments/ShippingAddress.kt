package com.example.votree.products.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.votree.databinding.FragmentShippingAddressBinding
import com.example.votree.products.models.ShippingAddress
import com.example.votree.products.view_models.ShippingAddressViewModel
import com.google.android.material.snackbar.Snackbar

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
        binding.confirmBtn.setOnClickListener { onSaveAddress() }

        return binding.root
    }

    private fun setUpObservers() {
        // Observe the selectedShippingAddress LiveData
        viewModel.selectedShippingAddress.observe(viewLifecycleOwner) { address ->
            // Update the form fields with the selected address details
            if (address != null) {
                prefillForm(address)
            }
        }
    }

    private fun prefillForm(address: ShippingAddress) {
        binding.recipientNameEt.setText(address.recipientName)
        binding.recipientPhoneNumberEt.setText(address.recipientPhoneNumber)
        binding.recipentAddressEt.setText(address.recipientAddress)
        binding.setAsDefaultSwitch.isChecked = address.default
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

        viewModel.saveShippingAddress(shippingAddress)

        // Handle Success or Failure (e.g. show messages, navigate if needed)
        Snackbar.make(binding.root, "Address saved successfully", Snackbar.LENGTH_SHORT).show()
        // Navigate back to the previous screen
//        requireActivity().onBackPressed()
    }
}
