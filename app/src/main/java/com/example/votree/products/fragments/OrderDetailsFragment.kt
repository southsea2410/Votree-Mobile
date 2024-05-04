package com.example.votree.products.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.databinding.FragmentOrderDetailsBinding
import com.example.votree.products.adapters.OrderManagementProductAdapter
import com.example.votree.products.models.Transaction
import com.example.votree.products.repositories.TransactionRepository
import com.example.votree.products.view_models.ProductViewModel
import com.example.votree.utils.ProgressDialogUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailsFragment : Fragment() {
    private lateinit var binding: FragmentOrderDetailsBinding
    private val transactionRepository = TransactionRepository(FirebaseFirestore.getInstance())
    private lateinit var coroutineScope: CoroutineScope
    private val args: OrderDetailsFragmentArgs by navArgs()
    private lateinit var transaction: Transaction

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        coroutineScope = CoroutineScope(Dispatchers.Main)
        ProgressDialogUtils.showLoadingDialog(requireContext())

        // Fetch the transaction details and update the UI
        coroutineScope.launch(Dispatchers.IO) {
            transaction = args.transaction
            withContext(Dispatchers.Main) {
                updateUI(transaction)
                setupButton()
                ProgressDialogUtils.hideLoadingDialog()
            }
        }
    }

    private fun updateUI(transaction: Transaction) {
        binding.userNameTv.text = transaction.name
        binding.userPhoneNumberTv.text = transaction.phoneNumber
        binding.userAddressTv.text = transaction.address
        binding.deliveryFeeTv.text = "$ +10"
        binding.totalAmountTv.text = "$ ${transaction.totalAmount}"
        // Date format YYYY-MM-DD HH:MM:SS
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        binding.orderTimeTv.text = dateFormat.format(transaction.createdAt)

        if (transaction.remainPrice.toInt() == 0) {
            binding.paymentOptionTv.text = "Paid"
        } else {
            binding.paymentOptionTv.text = "COD"
        }

        val productViewModel = ProductViewModel()
        binding.productsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.productsRv.adapter = OrderManagementProductAdapter(
            requireContext(),
            transaction,
            productViewModel
        )
    }

    private fun setupButton() {
        if (transaction.status == "pending") {
            binding.denyBtn.isEnabled = true
            binding.deliverBtn.isEnabled = true
        } else {
            // Disable the buttons
            binding.denyBtn.isEnabled = false
            binding.deliverBtn.isEnabled = false
        }

        binding.denyBtn.setOnClickListener {
            showDenyOrderDialog()
        }
        binding.deliverBtn.setOnClickListener {
            setupDeliverOrder()
        }
    }

    private fun showDenyOrderDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Deny Order")
        builder.setMessage("Are you sure you want to deny this order?")
        builder.setPositiveButton("Deny") { _, _ ->
            // Deny the order
            denyOrder()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun denyOrder() {
        coroutineScope.launch(Dispatchers.IO) {
            transactionRepository.toggleOrderStatus(transaction.id, "denied")
            withContext(Dispatchers.Main) {
                // Navigate back to the previous screen
                findNavController().popBackStack()
            }
        }
    }

    private fun setupDeliverOrder() {
        coroutineScope.launch(Dispatchers.IO) {
            transactionRepository.toggleOrderStatus(transaction.id, "delivered")
            withContext(Dispatchers.Main) {
                // Navigate back to the previous screen
                findNavController().popBackStack()
            }
        }
    }
}