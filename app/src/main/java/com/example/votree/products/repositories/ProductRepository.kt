package com.example.votree.products.repositories

import android.net.Uri
import com.example.votree.products.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ProductRepository(private val db: FirebaseFirestore) {
    suspend fun updateProductInventory(productId: String, quantityPurchased: Int) {
        val productRef = db.collection("products").document(productId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(productRef)
            val newInventory = snapshot.getLong("inventory")!! - quantityPurchased
            transaction.update(productRef, "inventory", newInventory)
        }.await()
    }

    fun uploadProductImage(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val formatter = java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
        val now = java.util.Calendar.getInstance().time
        val fileName = formatter.format(now)
        val storageRef = FirebaseStorage.getInstance().reference.child("images/products/$fileName")

        storageRef.putFile(imageUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    suspend fun getProduct(productId: String?): Product {
        val productRef = db.collection("products").document(productId!!)
        val snapshot = productRef.get().await()
        return snapshot.toObject(Product::class.java)!!
    }
}