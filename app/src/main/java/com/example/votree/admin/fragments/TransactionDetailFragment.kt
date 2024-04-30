package com.example.votree.admin.fragments

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.votree.R
import com.example.votree.models.Product
import com.example.votree.models.Store
import com.example.votree.models.Transaction
import com.example.votree.models.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class TransactionDetailFragment : Fragment() {
    private var transaction: Transaction? = null
    private var currentTransactionId: String = ""
    private var productBoughtList: String = ""
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_detail, container, false)
//        val approveButton = view?.findViewById<Button>(R.id.approveButton)
//        val rejectButton = view?.findViewById<Button>(R.id.rejectButton)
//
//        approveButton?.setOnClickListener {
//            db.collection("ProductTip2").document(tip!!.id).update("approvalStatus", 1)
//                .addOnSuccessListener {
//                    activity?.supportFragmentManager?.popBackStack("tip_list_fragment", POP_BACK_STACK_INCLUSIVE)
//                }
//        }
//
//        rejectButton?.setOnClickListener {
//            db.collection("ProductTip2").document(tip!!.id).update("approvalStatus", -1)
//                .addOnSuccessListener {
//                    activity?.supportFragmentManager?.popBackStack("tip_list_fragment", POP_BACK_STACK_INCLUSIVE)
//                }
//        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transaction = arguments?.getParcelable("transaction", Transaction::class.java)
        setTransactionId(transaction?.id ?: "")
        updateUI()
    }

    private fun updateUI() {
        if (currentTransactionId != "" && transaction != null) {
            productBoughtList = ""
            var isFirstProduct = true
            for ((productId, _) in transaction!!.productsMap) {
                db.collection("products").whereEqualTo("id", productId)
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            Log.w("TransactionDetailFragment", "listen:error", e)
                            return@addSnapshotListener
                        }

                        for (doc in snapshots!!) {
                            val product = doc.toObject(Product::class.java)
                            if (product.id == productId) {
                                productBoughtList += if (isFirstProduct) {
                                    isFirstProduct = false
                                    product.productName
                                } else {
                                    ", ${product.productName}"
                                }
                            }
                        }
                        val transactionProductBought: TextView? = view?.findViewById(R.id.transactionProducts)

                        transactionProductBought?.text = "Product(s): $productBoughtList"

                    }
            }
        } else {
            productBoughtList = "No product bought"
        }

        // Get store information
        db.collection("stores").whereEqualTo("id", transaction?.storeId)
            .addSnapshotListener { userSnapshots, userError ->
                if (userError != null) {
                    return@addSnapshotListener
                }

                for (userDoc in userSnapshots!!) {
                    val store = userDoc.toObject(Store::class.java)
                    if (store.id == transaction?.storeId) {
                        view?.findViewById<TextView>(R.id.store_name)?.text = store.storeName
                    }
                }
            }

        // Get customer information
        db.collection("users").whereEqualTo("id", transaction?.customerId)
            .addSnapshotListener { userSnapshots, userError ->
                if (userError != null) {
                    return@addSnapshotListener
                }

                for (userDoc in userSnapshots!!) {
                    val customer = userDoc.toObject(User::class.java)
                    if (customer.id == transaction?.customerId) {
                        view?.findViewById<TextView>(R.id.transactionCustomer)?.text = "Customer: ${customer.fullName}"
                    }
                }
            }

        transaction?.let { nonNullTransaction ->
            val transactionAddress: TextView? = view?.findViewById(R.id.address)
            val transactionOrderedDate: TextView? = view?.findViewById(R.id.ordered_date)
            val transactionPaymentOption: TextView? = view?.findViewById(R.id.payment_option_value)
            val transactionVoucherDiscount: TextView? = view?.findViewById(R.id.voucher_discount_value)
            val transactionTotalPaymentValue: TextView? = view?.findViewById(R.id.total_payment_value)

            transactionAddress?.text = "Address: ${nonNullTransaction.address}"
            transactionOrderedDate?.text = "Ordered on ${dateFormat(nonNullTransaction.createdAt.toString())}"
            if (nonNullTransaction.remainPrice == 0.0) {
                transactionPaymentOption?.text = "Prepay"
            } else {
                transactionPaymentOption?.text = if (nonNullTransaction.remainPrice.compareTo(nonNullTransaction.totalAmount) < 0) "Prepay and Cash" else "Cash"
            }

            transactionTotalPaymentValue?.text = formatPrice(nonNullTransaction.totalAmount.toString())

        }
    }

    private fun dateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val inputDate = inputFormat.parse(date)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        return outputFormat.format(inputDate)
    }

    private fun formatPrice(price: String): String {
        val priceString = price.reversed()
        var result = ""
        for (i in priceString.indices) {
            result += priceString[i]
            if ((i + 1) % 3 == 0 && i != priceString.length - 1) {
                result += "."
            }
        }
        return "đ$result".reversed()
    }

    fun setTransactionId(transactionId: String) {
        this.currentTransactionId = transactionId
    }
}
