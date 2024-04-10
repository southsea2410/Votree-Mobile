package com.example.votree.products.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.votree.products.models.Cart
import com.example.votree.products.repositories.CartRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class CartViewModel : ViewModel() {
    private val cartRepository = CartRepository()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val productViewModel = ProductViewModel()
    private val _cart: MutableLiveData<Cart?> = MutableLiveData()
    val cart: MutableLiveData<Cart?>
        get() = _cart

    init {
        fetchCart()
    }

    private fun fetchCart() {
        currentUser?.uid?.let { userId ->
            cartRepository.getCart(userId).onEach { cart ->
                _cart.value = cart
            }.launchIn(viewModelScope)
        }
    }

    fun addProductToCart(productId: String, quantity: Int) {
        currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                cartRepository.addToCart(userId, productId, quantity)
            }
        }
    }

    fun updateCartItem(productId: String, quantityChange: Int) {
        currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                cartRepository.updateCartItem(userId, productId, quantityChange)
            }
        }
    }


    fun removeCartItem(productId: String) {
        currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                cartRepository.removeCartItem(userId, productId)
            }
        }
    }

    fun calculateTotalProductsPrice(cart: Cart): LiveData<Double> {
        return liveData {
            val total = cart.productsMap.entries.sumByDouble { (productId, quantity) ->
                val price = productViewModel.getProductPriceById(productId)
                price?.times(quantity) ?: 0.0
            }
            emit(total)
        }
    }
}