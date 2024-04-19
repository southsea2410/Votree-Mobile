package com.example.votree.products.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.databinding.FragmentCartListBinding
import com.example.votree.products.adapters.ProductGroupAdapter
import com.example.votree.products.models.Cart
import com.example.votree.products.models.ProductItem
import com.example.votree.products.view_models.CartViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class CartList : Fragment() {
    private lateinit var viewModel: CartViewModel
    private lateinit var binding: FragmentCartListBinding
    private lateinit var adapter: ProductGroupAdapter
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        currentUser = FirebaseAuth.getInstance().currentUser!!

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeCart()
        gotoCheckout()
    }

    private fun setupRecyclerView() {
        binding.cartListRv.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProductGroupAdapter(emptyList(), viewModel)
        binding.cartListRv.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeCart() {
        viewModel.groupedProducts.observe(viewLifecycleOwner) { groupedItems ->
            if (!groupedItems.isNullOrEmpty()) {
                adapter.updateItems(groupedItems)
                adapter.notifyDataSetChanged()
                Log.d("CartList", "Items in cart: $groupedItems")
            } else {
                adapter.notifyDataSetChanged()
                Log.d("CartList", "No items in cart")
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }


    private fun gotoCheckout() {
        binding.buyNowBtn.setOnClickListener {
            val cartWithSelectedProduct = Cart(
                id = "",
                userId = currentUser.uid,
                productsMap = adapter.items
                    .filterIsInstance<ProductItem.ProductData>()
                    .filter { it.isChecked }
                    .associate { it.product.id to it.quantity }
                    .toMutableMap(),
                totalPrice = 0.0
            )

            Log.d("CartList", "Selected products: ${cartWithSelectedProduct.productsMap}")
            val action = CartListDirections.actionCartListToCheckout(cartWithSelectedProduct)
            findNavController().navigate(action)
        }
    }
}



