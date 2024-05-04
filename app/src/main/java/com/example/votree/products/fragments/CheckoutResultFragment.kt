package com.example.votree.products.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.votree.R
import com.example.votree.databinding.FragmentCheckoutResultBinding
import com.example.votree.users.activities.OrderHistoryActivity

class CheckoutResultFragment : Fragment() {
    private var _binding: FragmentCheckoutResultBinding? = null
    private val binding get() = _binding!!
    private val args: CheckoutResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI()
    }

    private fun updateUI() {
        if (args.checkoutResult) {
            // Payment was successful
            binding.productImageIv.setImageResource(R.drawable.ic_checkout_sc)
            binding.pointsTv.text = "+ ${args.points} Points"
            binding.orderHistoryBtn.setOnClickListener {
                startActivity(Intent(requireContext(), OrderHistoryActivity::class.java))
            }
        } else {
            // Payment failed
            binding.productImageIv.setImageResource(R.drawable.ic_checkout_fail)
            binding.thankYouTv.text = "Payment Failed"
            binding.orderHistoryBtn.text = "Checkout Again"
            binding.orderHistoryBtn.setOnClickListener {
                val action =
                    CheckoutResultFragmentDirections.actionCheckoutResultFragmentToCheckout(args.cart)
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}