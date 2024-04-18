package com.example.votree.products.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.votree.products.models.Cart
import com.example.votree.products.models.ProductItem
import com.example.votree.products.repositories.CartRepository
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.users.repositories.StoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CartViewModel : ViewModel() {
    private val cartRepository = CartRepository()
    private val storeRepository = StoreRepository()
    private val productViewModel = ProductViewModel()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    val groupedProducts: MutableLiveData<List<ProductItem>?> = MutableLiveData()

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(true)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _cart: MutableLiveData<Cart?> = MutableLiveData()
    val cart: MutableLiveData<Cart?>
        get() = _cart

    init {
        fetchCart()
    }

    fun fetchCart() {
        currentUser?.uid?.let { userId ->
            cartRepository.getCart(userId).onEach { cart ->
                _cart.value = cart
            }.launchIn(viewModelScope)
        }

        _cart.observeForever { cart ->
            cart?.let {
                viewModelScope.launch {
                    val groupedItems = groupProductsByShopId(cart)
//                    groupedProducts.value = groupedItems
                    groupedProducts.postValue(groupedItems)
                    _isLoading.value = false
                }
            }
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

    // Make sure this function is a suspending function
    suspend fun groupProductsByShopId(cart: Cart): List<ProductItem> {
        _isLoading.postValue(true)

        val firestore = FirebaseFirestore.getInstance()
        return withContext(Dispatchers.IO) {
            val productIds = cart.productsMap.keys.toList()
            val productsList = productIds.mapNotNull { productId ->
                ProductRepository(firestore).getProduct(productId) // Assuming getProduct is a suspending function
            }

            val productsGroupedByShopId = productsList.groupBy { it.storeId }
            val groupedItems = mutableListOf<ProductItem>()

            productsGroupedByShopId.forEach { (storeId, products) ->
                val store = storeRepository.fetchStoreById(storeId, {
                    groupedItems.add(ProductItem.ProductHeader(it.storeName))
                    products.forEach { product ->
                        groupedItems.add(
                            ProductItem.ProductData(
                                product,
                                cart.productsMap[product.id] ?: 0
                            )
                        )
                    }
                }, {
                    Log.d("CartViewModel", "Failed to fetch store with id $storeId")
                })
            }

            _isLoading.postValue(false)
            // Return the grouped items
            groupedItems
        }
    }
}