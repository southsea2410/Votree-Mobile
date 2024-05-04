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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.R
import com.example.votree.databinding.FragmentCheckoutBinding
import com.example.votree.products.activities.CheckoutActivity
import com.example.votree.products.adapters.CheckoutProductAdapter
import com.example.votree.products.models.Cart
import com.example.votree.products.models.ShippingAddress
import com.example.votree.products.repositories.PointTransactionRepository
import com.example.votree.products.view_models.CartViewModel
import com.example.votree.products.view_models.ProductViewModel
import com.example.votree.products.view_models.ShippingAddressViewModel
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val DELIVERY_FEE = 10.0


class Checkout : Fragment() {
    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var shippingAddressViewModel: ShippingAddressViewModel
    private lateinit var productViewModel: ProductViewModel
    private lateinit var cartViewModel: CartViewModel
    private var cart = Cart()
    private var shippingAddress: ShippingAddress? = null

    private var newAccumulatedPoints = 0
    private val args: CheckoutArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        shippingAddressViewModel =
            ViewModelProvider(requireActivity()).get(ShippingAddressViewModel::class.java)
        productViewModel = ViewModelProvider(requireActivity()).get(ProductViewModel::class.java)
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCheckout()
        setupAccumulatePoints()
        setupObservers()
        setupRecyclerView()
    }

    private fun proceedToPayment(cart: Cart, shippingAddress: ShippingAddress?) {
        val intent = Intent(activity, CheckoutActivity::class.java)
        intent.putExtra("totalAmount", binding.totalAmountTv.text.toString())
        intent.putExtra("cart", cart)
        intent.putExtra("receiver", shippingAddress)
        startActivityForResult(intent, 1)
    }

    private fun placeOrderWithoutPayment() {
        Toast.makeText(context, "Order placed without upfront payment.", Toast.LENGTH_SHORT).show()
    }

    private fun setupObservers() {
        val placeOrderButton: Button = binding.placeOrderBtn
        val payBeforeDeliverySwitch: MaterialSwitch = binding.paidBeforeDeliverySw

        placeOrderButton.setOnClickListener {
            if (binding.usePointsSw.isChecked) {
                val pointTransactionRepository = PointTransactionRepository()
                lifecycleScope.launch {
                    pointTransactionRepository.redeemPoints(
                        newAccumulatedPoints,
                        "Redeem points for purchase"
                    )
                    // Proceed to payment or place order without payment based on the payBeforeDeliverySwitch state
                    if (payBeforeDeliverySwitch.isChecked) {
                        proceedToPayment(cart, shippingAddress)
                    } else {
                        placeOrderWithoutPayment()
                    }
                }
            } else {
                // If the use points switch is unchecked, simply proceed without redeeming points
                if (payBeforeDeliverySwitch.isChecked) {
                    proceedToPayment(cart, shippingAddress)
                } else {
                    placeOrderWithoutPayment()
                }
            }
        }

        // Observe the shippingAddress LiveData
        shippingAddressViewModel.shippingAddress.observe(viewLifecycleOwner) { address ->
            if (address != null) {
                shippingAddress = address
                updateAddressUI(address)
            }
        }

        binding.addressView.setOnClickListener {
            val action = CheckoutDirections.actionCheckoutToShippingAddressFragment()
            findNavController().navigate(action)
        }
    }

    private fun setupCheckout() {
        if (args.cart != null) {
            cart = args.cart?.copy() ?: Cart()

            CoroutineScope(Dispatchers.Main).launch {

            }
            Log.d("Checkout", "cart: $cart")
            cartViewModel.calculateTotalProductsPrice(cart)
                .observe(viewLifecycleOwner) { totalPrice ->
                    val totalAmount = totalPrice + DELIVERY_FEE

                    binding.totalProductsPriceTv.text = getString(R.string.price_format, totalPrice)
                    binding.totalAmountTv.text = totalAmount.toString()
                    binding.deliveryFeeTv.text = getString(R.string.price_format, DELIVERY_FEE)
                    binding.totalAmountBottomTv.text = getString(R.string.price_format, totalAmount)
                }
        }

        lifecycleScope.launch {
            val pointTransactionRepository = PointTransactionRepository()
            val currentPoints = pointTransactionRepository.getCurrentPoints() ?: 0
            binding.accumulatePointsTv.text = getString(R.string.price_format, currentPoints * 0.01)
        }
    }

    private fun setupAccumulatePoints() {
        // Get the state of the switch
        val accumulatePointsSwitch: MaterialSwitch = binding.usePointsSw

        accumulatePointsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val pointTransactionRepository = PointTransactionRepository()
                lifecycleScope.launch {
                    val currentPoints = pointTransactionRepository.getCurrentPoints() ?: 0
                    val totalAmount = binding.totalAmountTv.text.toString().toDouble()

                    // Calculate the discount from points (assuming 1 point = $0.01)
                    val discountFromPoints = currentPoints * 0.01

                    if (discountFromPoints >= totalAmount) {
                        // If discount is more than or equal to total amount, purchase is free and update points
                        val remainingPoints = ((discountFromPoints - totalAmount) / 0.01).toInt()
                        newAccumulatedPoints = remainingPoints

                        // Update UI to show no amount due
                        binding.saleByPointsTv.text = getString(R.string.price_format, -totalAmount)
                        binding.totalAmountTv.text = "0.0"
                        binding.totalAmountBottomTv.text = "0.0"
                    } else {
                        // If discount is less than total amount, deduct all points and update total amount
                        newAccumulatedPoints = currentPoints

                        // Calculate new total amount after applying points
                        val newTotalAmount = totalAmount - discountFromPoints
                        binding.saleByPointsTv.text =
                            getString(R.string.price_format, -discountFromPoints)
                        binding.totalAmountTv.text = newTotalAmount.toString()
                        binding.totalAmountBottomTv.text = newTotalAmount.toString()
                    }
                }
            } else {
                // If the switch is unchecked, re-update the total amount without applying points
                cartViewModel.calculateTotalProductsPrice(cart)
                    .observe(viewLifecycleOwner) { totalPrice ->
                        val totalAmount = totalPrice + DELIVERY_FEE

                        binding.totalProductsPriceTv.text =
                            getString(R.string.price_format, totalPrice)
                        binding.totalAmountTv.text = totalAmount.toString()
                        binding.deliveryFeeTv.text = getString(R.string.price_format, DELIVERY_FEE)
                        binding.totalAmountBottomTv.text =
                            getString(R.string.price_format, totalAmount)
                        binding.saleByPointsTv.text = getString(R.string.price_format, -0.0)
                    }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.productsRv.layoutManager = LinearLayoutManager(context)
        binding.productsRv.adapter =
            CheckoutProductAdapter(requireContext(), cart, productViewModel)
    }

    private fun updateAddressUI(address: ShippingAddress) {
        binding.userNameTv.text = address.recipientName
        binding.userPhoneNumberTv.text = address.recipientPhoneNumber
        binding.userAddressTv.text = address.recipientAddress
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Checkout", "onActivityResult: requestCode: $requestCode, resultCode: $resultCode")
        // If success result from CheckoutActivity, navigate to CheckoutResultFragment
        if (requestCode == 1 && resultCode == -1) {
            val action = CheckoutDirections.actionCheckoutToCheckoutResultFragment(
                true,
                newAccumulatedPoints,
                null
            )
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}