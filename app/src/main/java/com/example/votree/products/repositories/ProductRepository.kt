package com.example.votree.products.repositories

import android.net.Uri
import android.util.Log
import com.example.votree.products.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProductRepository(private val firestore: FirebaseFirestore) {

    fun fetchProducts(
        storeId: String,
        onSuccess: (List<Product>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("products")
            .whereEqualTo("storeId", storeId)
            .get()
            .addOnSuccessListener { documents ->
                val productList = documents.toObjects(Product::class.java)
                onSuccess(productList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
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

    fun addProduct(product: Product, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("products").add(product)
            .addOnSuccessListener { documentReference ->
                val productId = documentReference.id
                firestore.collection("products").document(productId).update("id", productId)
                    .addOnSuccessListener {
                        onSuccess(productId)
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProductRepository", "Error updating product with ID", e)
                        onFailure(e)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun updateProduct(product: Product, onComplete: () -> Unit) {
        product.id.takeIf { it.isNotEmpty() }?.let { productId ->
            firestore.collection("products").document(productId).set(product)
                .addOnSuccessListener {
                    onComplete()
                }
                .addOnFailureListener {
                    Log.e("ProductRepository", "Error updating product", it)
                    onComplete()
                }
        }
    }

    fun deleteProduct(product: Product, onComplete: (Boolean) -> Unit) {
        // First, delete the image from Firebase Storage
        if (product.imageUrl.isNotEmpty()) {
            val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(product.imageUrl)
            imageRef.delete().addOnSuccessListener {
                // Case image is deleted, delete the product from Firestore
                Log.d("ProductRepository", "Image deleted")
                firestore.collection("products").document(product.id).delete()
                    .addOnSuccessListener {
                        Log.d("ProductRepository", "Product deleted")
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        Log.e("ProductRepository", "Error deleting product", it)
                        onComplete(false)
                    }
            }.addOnFailureListener {
                Log.e("ProductRepository", "Error deleting image", it)
                onComplete(false)
            }
        } else {
            // Case product doesn't have an image, delete the product from Firestore
            firestore.collection("products").document(product.id).delete()
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }
    }
}