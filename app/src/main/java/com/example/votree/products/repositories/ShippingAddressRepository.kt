package com.example.votree.products.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.votree.products.models.ShippingAddress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShippingAddressRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId get() = auth.currentUser?.uid!!
    private val addressesCollection =
        db.collection("users").document(userId).collection("addresses")

    suspend fun saveShippingAddress(address: ShippingAddress) {
        try {
            Log.d("ShippingAddressRepo", "Checking for duplicate addresses")

            // Query Firestore for any addresses matching the new address's details
            val duplicateQuery = addressesCollection
                .whereEqualTo("recipientName", address.recipientName)
                .whereEqualTo("recipientAddress", address.recipientAddress)
                .whereEqualTo("recipientPhoneNumber", address.recipientPhoneNumber)
                .get().await()

            // Check if the query returned any documents
            if (duplicateQuery.documents.isNotEmpty()) {
                Log.d("ShippingAddressRepo", "Duplicate address found. Aborting save.")
                return
            }

            // If the new address is set as the default, update all other default addresses to non-default
            if (address.default) {
                val defaultAddresses =
                    addressesCollection.whereEqualTo("default", true).get().await().documents
                for (doc in defaultAddresses) {
                    doc.reference.update("default", false).await()
                    Log.d("ShippingAddressRepo", "Updated address ${doc.id} to not be the default.")
                }
            }

            // Save the new address or update the existing one
            if (address.id.isNullOrEmpty()) {
                val newDocRef = addressesCollection.document()
                address.id = newDocRef.id
                newDocRef.set(address).await()
                Log.d("ShippingAddressRepo", "Saved new address with ID: ${address.id}")
            } else {
                addressesCollection.document(address.id).set(address).await()
                Log.d("ShippingAddressRepo", "Updated address with ID: ${address.id}")
            }
        } catch (e: Exception) {
            Log.e("ShippingAddressRepo", "Error saving/updating address: ${e.message}", e)
            throw e
        }
    }

    suspend fun getShippingAddresses(
        shippingAddress: MutableLiveData<List<ShippingAddress>?>,
        selectedShippingAddress: MutableLiveData<ShippingAddress?>
    ): List<ShippingAddress> {
        val addresses = addressesCollection.get().await()
            .documents.mapNotNull { it.toObject(ShippingAddress::class.java) }
        Log.d("ShippingAddressRepo", "Fetched shipping addresses: $addresses")
//        shippingAddress.postValue(addresses)
        selectedShippingAddress.postValue(addresses.find { it.default })
        selectedShippingAddress.value = addresses.find { it.default }
        return addresses
    }

    suspend fun fetchAddresses(): List<ShippingAddress> {
        return addressesCollection.get()
            .await().documents.mapNotNull { it.toObject(ShippingAddress::class.java) }
    }
}
