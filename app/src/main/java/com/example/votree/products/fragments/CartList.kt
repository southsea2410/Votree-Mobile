package com.example.votree.products.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.databinding.FragmentCartListBinding
import com.example.votree.products.adapters.CartAdapter
import com.example.votree.products.view_models.CartViewModel
import com.example.votree.products.view_models.ProductViewModel

class CartList : Fragment() {

    private lateinit var viewModel: CartViewModel
    private lateinit var binding: FragmentCartListBinding
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        observeCart()
        gotoCheckout()
        setupRecyclerView()
    }

    private fun observeCart() {
        val productViewModel = ProductViewModel()

        viewModel.cart.observe(viewLifecycleOwner) { cart ->
            cart?.let {
                cartAdapter = CartAdapter(requireContext(), cart, productViewModel, viewModel)
                binding.cartListRv.adapter = cartAdapter
            }
        }
    }

    private fun setupRecyclerView() {
        binding.cartListRv.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun gotoCheckout() {
        binding.buyNowBtn.setOnClickListener {
            val action = CartListDirections.actionCartListToCheckout()
            findNavController().navigate(action)
        }
    }
}
