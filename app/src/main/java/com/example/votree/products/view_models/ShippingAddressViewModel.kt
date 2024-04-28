package com.example.votree.products.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votree.products.models.ShippingAddress
import com.example.votree.products.repositories.ShippingAddressRepository
import com.example.votree.utils.SingleLiveEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ShippingAddressViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val repository = ShippingAddressRepository(db, auth)

    private val _shippingAddresses = SingleLiveEvent<List<ShippingAddress>?>()
    val shippingAddresses: LiveData<List<ShippingAddress>?> = _shippingAddresses

    private val _isSaveAddressSuccessful = SingleLiveEvent<Boolean>()
    val isSaveAddressSuccessful: LiveData<Boolean> = _isSaveAddressSuccessful

    // LiveData for holding the default shipping address
    private val _selectedShippingAddress = SingleLiveEvent<ShippingAddress?>()
    val selectedShippingAddress: LiveData<ShippingAddress?> = _selectedShippingAddress

    init {
        fetchShippingAddresses()
    }

    private fun fetchShippingAddresses() {
        viewModelScope.launch {
            try {
                runBlocking {
                    repository.getShippingAddresses(
                        _shippingAddresses,
                        _selectedShippingAddress
                    )
                }
                _isSaveAddressSuccessful.postValue(true)
            } catch (e: Exception) {
                Log.e("ShippingAddressViewModel", "Error fetching shipping addresses: $e")
            }
        }
    }

    suspend fun saveShippingAddress(address: ShippingAddress) {
        viewModelScope.launch {
            try {
                runBlocking {
                    repository.saveShippingAddress(address)
                }
                fetchShippingAddresses()
            } catch (e: Exception) {
                Log.e("ShippingAddressViewModel", "Error saving shipping address: $e")
                _isSaveAddressSuccessful.postValue(false)
            }
        }
    }

    fun prefillForm(address: ShippingAddress) {
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

    fun updateAddresses() {
        viewModelScope.launch {
            val updatedAddresses = repository.fetchAddresses()
            _shippingAddresses.postValue(updatedAddresses)
        }
    }
}