package com.example.votree.products.repositories

import android.util.Log
import com.example.votree.products.models.Cart
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CartRepository {
    private val db = FirebaseFirestore.getInstance()
    private val cartsCollection = db.collection("carts")

    suspend fun addToCart(userId: String, productId: String, quantity: Int) {
        val documentSnapshot = cartsCollection.document(userId).get().await()
        if (documentSnapshot.exists()) {
            // Cart exists, update the product quantity
            val cart = documentSnapshot.toObject(Cart::class.java)!!
            cart.productsMap[productId] = (cart.productsMap[productId] ?: 0) + quantity
            cartsCollection.document(userId).set(cart).await()
            updateTotalPrice(userId) // Update totalPrice
        } else {
            // Cart doesn't exist, create a new one
            val newCart = Cart(userId = userId, productsMap = mutableMapOf(productId to quantity))
            cartsCollection.document(userId).set(newCart).await()
            updateTotalPrice(userId) // Update totalPrice
        }
    }

    suspend fun updateCartItem(userId: String, productId: String, quantityChange: Int) {
        val currentCartQuantity = getCurrentCartQuantity(userId, productId)
        if (quantityChange == 1) {
            val inventoryQuantity = fetchProductInventory(productId) ?: return
            if (currentCartQuantity + 1 > inventoryQuantity) {
                return
            }
        } else if (quantityChange == -1 && currentCartQuantity <= 1) {
            return
        }

        // Proceed to update item quantity in the cart
        cartsCollection.document(userId)
            .update("productsMap.$productId", FieldValue.increment(quantityChange.toLong()))
            .await()
        updateTotalPrice(userId)
    }

    private suspend fun getCurrentCartQuantity(userId: String, productId: String): Int {
        val documentSnapshot = cartsCollection.document(userId).get().await()
        val cart = documentSnapshot.toObject(Cart::class.java) ?: return 0
        return cart.productsMap[productId] ?: 0
    }

    private suspend fun fetchProductInventory(productId: String): Int? {
        return db.collection("products").document(productId).get().await()
            .getLong("inventory")?.toInt()
    }

    suspend fun removeCartItem(userId: String, productId: String) {
        cartsCollection.document(userId)
            .update("productsMap.$productId", FieldValue.delete())
            .await()
        updateTotalPrice(userId)
    }

    fun getCart(userId: String): Flow<Cart?> = callbackFlow {
        val listenerRegistration = cartsCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Close the channel on error
                    return@addSnapshotListener
                }
                val cart = snapshot?.toObject(Cart::class.java)
                trySend(cart).isSuccess
            }
        awaitClose { listenerRegistration.remove() }
    }

    private suspend fun updateTotalPrice(userId: String) {
        val cart = cartsCollection.document(userId).get().await().toObject(Cart::class.java)
        cart?.let {
            val newTotalPrice = it.productsMap.mapNotNull { (productId, quantity) ->
                fetchProductPrice(productId)?.times(quantity)
            }.sum()
            cartsCollection.document(userId).update("totalPrice", newTotalPrice).await()
        }
    }

    private suspend fun fetchProductPrice(productId: String): Double? {
        return db.collection("products").document(productId).get().await()
            .getDouble("price")
    }

    suspend fun clearCartAfterCheckout(userId: String) {
        val cartRef = db.collection("carts").document(userId)
        // Clear the cart
        cartRef.update("productsMap", mapOf<String, Any>()).await()
        cartRef.update("totalPrice", 0.0).await()
        Log.d("CartRepository", "Cart cleared successfully")
    }
}
