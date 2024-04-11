package com.example.votree.products.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.R
import com.example.votree.databinding.FragmentCheckoutBinding
import com.example.votree.products.activities.CheckoutActivity
import com.example.votree.products.adapters.CheckoutProductAdapter
import com.example.votree.products.models.Cart
import com.example.votree.products.models.ShippingAddress
import com.example.votree.products.view_models.CartViewModel
import com.example.votree.products.view_models.ProductViewModel
import com.example.votree.products.view_models.ShippingAddressViewModel
import com.google.android.material.materialswitch.MaterialSwitch

class Checkout : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var shippingAddressViewModel: ShippingAddressViewModel
    private lateinit var productViewModel: ProductViewModel
    private lateinit var cartViewModel: CartViewModel
    private lateinit var adapter: CheckoutProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        shippingAddressViewModel =
            ViewModelProvider(requireActivity()).get(ShippingAddressViewModel::class.java)
        productViewModel = ViewModelProvider(requireActivity()).get(ProductViewModel::class.java)
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)

        setupObservers()
        setupRecyclerView()
        binding.addressView.setOnClickListener {
            val action = CheckoutDirections.actionCheckoutToShippingAddressFragment()
            findNavController().navigate(action)
        }

        val placeOrderButton: Button = binding.placeOrderBtn
        val payBeforeDeliverySwitch: MaterialSwitch = binding.paidBeforeDeliverySw

        placeOrderButton.setOnClickListener {
            if (payBeforeDeliverySwitch.isChecked) {
                proceedToPayment()
            } else {
                placeOrderWithoutPayment()
            }
        }

        return binding.root
    }

    private fun proceedToPayment() {
        val intent = Intent(activity, CheckoutActivity::class.java)
        intent.putExtra("totalAmount", binding.totalAmountTv.text.toString())
        startActivity(intent)
    }

    private fun placeOrderWithoutPayment() {
        Toast.makeText(context, "Order placed without upfront payment.", Toast.LENGTH_SHORT).show()
    }

    private fun setupObservers() {
        // Observe the selectedShippingAddress LiveData
        shippingAddressViewModel.selectedShippingAddress.observe(viewLifecycleOwner) { address ->
            updateAddressUI(address)
        }

        // Assuming cartViewModel.cart is a LiveData holding the Cart object
        cartViewModel.cart.observe(viewLifecycleOwner) { cart ->
            adapter = CheckoutProductAdapter(requireContext(), cart!!, productViewModel)
            binding.productsRv.adapter = adapter
        }

        // Observe the cart to calculate totals
        cartViewModel.cart.observe(viewLifecycleOwner) { cart ->
            cart?.let {
                cartViewModel.calculateTotalProductsPrice(it)
                    .observe(viewLifecycleOwner) { totalPrice ->
                        binding.totalProductsPriceTv.text =
                            getString(R.string.price_format, totalPrice)
                        val deliveryFee = 10.0 // Constant delivery fee
                        val totalAmount = totalPrice + deliveryFee
                        binding.totalAmountTv.text = totalAmount.toString()
                        binding.deliveryFeeTv.text = getString(R.string.price_format, deliveryFee)
                        binding.totalAmountTv.text = getString(R.string.price_format, totalAmount)
                        binding.totalAmountBottomTv.text =
                            getString(R.string.price_format, totalAmount)
                    }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.productsRv.layoutManager = LinearLayoutManager(context)
        binding.productsRv.adapter =
            CheckoutProductAdapter(requireContext(), Cart(), productViewModel)
    }

    private fun updateAddressUI(address: ShippingAddress?) {
        if (address != null) {
            binding.userNameTv.text = address.recipientName
            binding.userPhoneNumberTv.text = address.recipientPhoneNumber
            binding.userAddressTv.text = address.recipientAddress
        } else {
            Toast.makeText(requireContext(), "Please select a shipping address", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
