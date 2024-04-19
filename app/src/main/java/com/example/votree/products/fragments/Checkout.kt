package com.example.votree.products.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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

const val DELIVERY_FEE = 10.0

class Checkout : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    private val args: CheckoutArgs by navArgs()

    private lateinit var shippingAddressViewModel: ShippingAddressViewModel
    private lateinit var productViewModel: ProductViewModel
    private lateinit var cartViewModel: CartViewModel
    private var cart = Cart()
    private var shippingAddress: ShippingAddress? = null
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

        setupCheckout()
        setupObservers()
        setupRecyclerView()

        return binding.root
    }

    private fun proceedToPayment(cart: Cart, shippingAddress: ShippingAddress?) {
        val intent = Intent(activity, CheckoutActivity::class.java)
        intent.putExtra("totalAmount", binding.totalAmountTv.text.toString())
        intent.putExtra("cart", cart)
        intent.putExtra("receiver", shippingAddress)
        startActivity(intent)
    }

    private fun placeOrderWithoutPayment() {
        Toast.makeText(context, "Order placed without upfront payment.", Toast.LENGTH_SHORT).show()
    }

    private fun setupObservers() {
        val placeOrderButton: Button = binding.placeOrderBtn
        val payBeforeDeliverySwitch: MaterialSwitch = binding.paidBeforeDeliverySw

        placeOrderButton.setOnClickListener {
            if (payBeforeDeliverySwitch.isChecked) {
                proceedToPayment(cart, shippingAddress)
            } else {
                placeOrderWithoutPayment()
            }
        }

        // Observe the shippingAddresses LiveData
        shippingAddressViewModel.shippingAddresses.observe(viewLifecycleOwner) { addresses ->
            if (addresses != null) {
                Log.d("Checkout", "Shipping addresses: $addresses")
                // Select the address that has the default field set to true
                val defaultAddress = addresses.find { it.default }
                if (defaultAddress != null) {
                    shippingAddress = defaultAddress
                    updateAddressUI(defaultAddress)
                } else {
                    // If no default address is found, select the first address in the list
                    shippingAddress = addresses.firstOrNull()
                    updateAddressUI(shippingAddress)
                }
            }
        }

        // Observe the selectedShippingAddress LiveData
        shippingAddressViewModel.selectedShippingAddress.observe(viewLifecycleOwner) { address ->
            if (address != null) {
                Log.d("Checkout", "Selected address: $address")
                shippingAddress = address
                updateAddressUI(address)
            }
        }

        binding.addressView.setOnClickListener {
            val action = CheckoutDirections.actionCheckoutToShippingAddressFragment()
            findNavController().navigate(action)
        }
    }

    private fun setupCheckoutWithCart() {
        cart = args.cart?.copy() ?: Cart()
        Log.d("Checkout", "cart: $cart")

        cartViewModel.cart.observe(viewLifecycleOwner) {
            adapter = CheckoutProductAdapter(requireContext(), cart!!, productViewModel)
            binding.productsRv.adapter = adapter
            if (it != null) {
                cart = it
            }

            cartViewModel.calculateTotalProductsPrice(cart)
                .observe(viewLifecycleOwner) { totalPrice ->
                    val totalAmount = totalPrice + DELIVERY_FEE

                    binding.totalProductsPriceTv.text = getString(R.string.price_format, totalPrice)
                    binding.totalAmountTv.text = totalAmount.toString()
                    binding.deliveryFeeTv.text = getString(R.string.price_format, DELIVERY_FEE)
                    binding.totalAmountTv.text = getString(R.string.price_format, totalAmount)
                    binding.totalAmountBottomTv.text = getString(R.string.price_format, totalAmount)
                }
        }
    }

    private fun setupCheckout() {
        if (args.cart != null) {
            Log.d("Checkout", "Setting up checkout with cart")
            setupCheckoutWithCart()
        }
    }

    private fun setupRecyclerView() {
        binding.productsRv.layoutManager = LinearLayoutManager(context)
        binding.productsRv.adapter =
            CheckoutProductAdapter(requireContext(), Cart(), productViewModel)
    }

    private fun updateAddressUI(address: ShippingAddress?) {
        if (address != null) {
            Log.d("Checkout", "Updating address UI")
            binding.userNameTv.text = address.recipientName
            binding.userPhoneNumberTv.text = address.recipientPhoneNumber
            binding.userAddressTv.text = address.recipientAddress
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
