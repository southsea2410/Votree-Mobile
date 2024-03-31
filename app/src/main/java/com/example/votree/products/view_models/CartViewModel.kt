package com.example.votree.products.view_models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.votree.products.models.Cart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val cartCollection = firestore.collection("carts")
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val _cart: MutableLiveData<Cart?> = MutableLiveData()
    val cart: MutableLiveData<Cart?>
        get() = _cart

    init {
        fetchCart()
    }

    private fun fetchCart() {
        currentUser?.uid?.let { userId ->
            Log.d(TAG, "Fetching cart for user: $userId")
            cartCollection.document(userId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching cart: $error")
                    return@addSnapshotListener
                }

                val cart = snapshot?.toObject(Cart::class.java)
                _cart.value = cart
            }
        }
    }

    fun addProductToCart(productId: String, quantity: Int) {
        currentUser?.uid?.let { userId ->
            val userCartRef = cartCollection.document(userId)
            userCartRef.get().addOnSuccessListener { documentSnapshot ->
                val cart = documentSnapshot.toObject(Cart::class.java)
                if (cart != null) {
                    cart.productsMap[productId] = quantity
                    // Update the cart in Firestore
                    userCartRef.set(cart)
                        .addOnSuccessListener {
                            Log.d(TAG, "Product added to cart successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error adding product to cart", e)
                        }
                } else {
                    // Create a new cart with the product
                    val newCart =
                        Cart(userId = userId, productsMap = mutableMapOf(productId to quantity))
                    userCartRef.set(newCart)
                        .addOnSuccessListener {
                            // Update the cart's ID after creation
                            val newCartId = userCartRef.id
                            newCart.id = newCartId
                            userCartRef.set(newCart)
                                .addOnSuccessListener {
                                    Log.d(TAG, "New cart created with product and updated ID")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error updating cart ID", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error creating new cart", e)
                        }
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error fetching cart", e)
            }
        }
    }

    companion object {
        private const val TAG = "CartViewModel"
    }
}
