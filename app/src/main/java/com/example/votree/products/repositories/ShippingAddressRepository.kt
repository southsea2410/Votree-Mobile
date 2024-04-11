package com.example.votree.products.repositories

import android.util.Log
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
                // Handle the duplicate case, e.g., by notifying the user or aborting the save operation
                // You might want to throw an exception or return a specific result indicating a duplicate was found.
                return
            }

            // Proceed with saving the address if no duplicates were found
            if (address.default) {
                val defaultAddresses =
                    addressesCollection.whereEqualTo("isDefault", true).get().await().documents
                for (doc in defaultAddresses) {
                    doc.reference.update("isDefault", false).await()
                    Log.d("ShippingAddressRepo", "Updated address ${doc.id} to not be default.")
                }
            }

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
            throw e // Optionally re-throw the exception if you want calling code to handle it
        }
    }

    suspend fun getShippingAddresses(): List<ShippingAddress> {
        return addressesCollection.get().await()
            .documents.mapNotNull { it.toObject(ShippingAddress::class.java) }
    }
}
