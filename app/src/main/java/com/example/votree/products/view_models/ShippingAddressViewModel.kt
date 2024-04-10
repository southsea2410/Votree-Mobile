package com.example.votree.products.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votree.products.models.ShippingAddress
import com.example.votree.products.repositories.ShippingAddressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ShippingAddressViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val repository = ShippingAddressRepository(db, auth)

    private val _shippingAddresses = MutableLiveData<List<ShippingAddress>?>()
    val shippingAddresses: LiveData<List<ShippingAddress>?> = _shippingAddresses

    // LiveData for holding the default shipping address
    private val _selectedShippingAddress = MutableLiveData<ShippingAddress?>()
    val selectedShippingAddress: LiveData<ShippingAddress?> = _selectedShippingAddress

    init {
        fetchShippingAddresses()
    }

    fun fetchShippingAddresses() {
        viewModelScope.launch {
            _shippingAddresses.value = repository.getShippingAddresses()
            _selectedShippingAddress.value = _shippingAddresses.value?.find { it.default }
        }
    }

    fun saveShippingAddress(address: ShippingAddress) {
        viewModelScope.launch {
            repository.saveShippingAddress(address)
            fetchShippingAddresses()
        }
    }

    private fun prefillForm(address: ShippingAddress) {
        val _recipientName = MutableLiveData(address.recipientName)
        val _recipientPhoneNumber = MutableLiveData(address.recipientPhoneNumber)
        val _recipientAddress = MutableLiveData(address.recipientAddress)
        val _isDefault = MutableLiveData(address.default)

        // Update the form fields
        _recipientName.value = address.recipientName
        _recipientPhoneNumber.value = address.recipientPhoneNumber
        _recipientAddress.value = address.recipientAddress
        _isDefault.value = address.default
    }
}
