package com.example.votree.products.fragments

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
import com.example.votree.products.view_models.CartViewModel

class CartList : Fragment() {
    private lateinit var viewModel: CartViewModel
    private lateinit var binding: FragmentCartListBinding
    private lateinit var adapter: ProductGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(CartViewModel::class.java)

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

    private fun observeCart() {
        viewModel.groupedProducts.observe(viewLifecycleOwner) { groupedItems ->
            if (!groupedItems.isNullOrEmpty()) {
                adapter.updateItems(groupedItems)
                adapter.notifyDataSetChanged()
                Log.d("CartList", "Items in cart: $groupedItems")
            } else {
                Log.d("CartList", "No items in cart")
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }


    private fun gotoCheckout() {
        binding.buyNowBtn.setOnClickListener {
            val action = CartListDirections.actionCartListToCheckout(null)
            findNavController().navigate(action)
        }
    }
}



