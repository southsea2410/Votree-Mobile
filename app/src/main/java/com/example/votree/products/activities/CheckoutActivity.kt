package com.example.votree.products.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.votree.R
import com.example.votree.products.models.Cart
import com.example.votree.products.models.Transaction
import com.example.votree.products.repositories.CartRepository
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.products.repositories.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date

class CheckoutActivity : AppCompatActivity() {
    private lateinit var paymentSheet: PaymentSheet
    private var paymentIntentClientSecret: String? = null
    private lateinit var functions: FirebaseFunctions
    var customerId = ""

    private val cartRepository = CartRepository()
    private val productRepository = ProductRepository(FirebaseFirestore.getInstance())
    private val transactionRepository = TransactionRepository(FirebaseFirestore.getInstance())
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Initialize Firebase Functions
        functions = FirebaseFunctions.getInstance()

        // Initialize Stripe PaymentConfiguration with your publishable key
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51OM54hL1bECNnFcvEJXdx2V7gJ6RxaDq7WVV1Jw3UZydI3Cag3lrQLQFwbadyM7Rp5uj8LdRXBSlAS0x5cJVuxuc00Yx8eUBEk" // Replace with your actual publishable key
        )

        // Initialize PaymentSheet
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        currentUser?.let { user ->
            db.collection("stripe_customers").document(user.uid).get()
                .addOnSuccessListener { document ->
                    Log.d("CheckoutActivity", "DocumentSnapshot data: ${document.data}")
                    customerId = document.getString("customer_id") ?: ""

                    val amount = 1099L
                    val currency = "usd"
                    fetchPaymentIntentClientSecret(amount, currency, customerId)
                }
                .addOnFailureListener { exception ->
                    // If not exists, create a new customer
                    val data = hashMapOf(
                        "email" to user.email
                    )
                    functions.getHttpsCallable("createStripeCustomer").call(data)
                        .addOnSuccessListener { result ->
                            val customer = result.data as Map<String, Any>
                            customerId = customer["id"] as String
                            db.collection("stripe_customers").document(user.uid)
                                .set(mapOf("customer_id" to customerId))
                        }
                        .addOnFailureListener { exception ->
                            Log.w("CheckoutActivity", "Error creating customer", exception)
                            Toast.makeText(
                                this,
                                "Failed to create a new customer.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
        }


    }

    private fun fetchPaymentIntentClientSecret(amount: Long, currency: String, customerId: String) {
        // Prepare the data to send to the Cloud Function
        val data = hashMapOf(
            "amount" to amount,
            "currency" to currency,
            "customerId" to customerId
        )

        // Call the 'createPaymentIntent' Cloud Function
        functions
            .getHttpsCallable("createPaymentIntent")
            .call(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result?.data as Map<String, Any>
                    paymentIntentClientSecret = result["clientSecret"] as String?
                    // Configure PaymentSheet with the fetched client secret
                    configurePaymentSheet()
                } else {
                    // Handle error
                    Toast.makeText(
                        this,
                        "Failed to fetch payment intent secret.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun configurePaymentSheet() {
        paymentIntentClientSecret?.let { secret ->
            // Configure PaymentSheet with the client secret
            val configuration =
                PaymentSheet.Configuration("VoTree") // Replace with your company name
            paymentSheet.presentWithPaymentIntent(secret, configuration)
        }
    }

    private fun createTransactionFromCart(cart: Cart) {
        val currentDate = Date()
        val storeId = ""
        FirebaseFirestore.getInstance().collection("carts").document(userId).get()
            .addOnSuccessListener { document ->
                // Get the first product in the cart

                val firstProduct = cart.productsMap.entries.firstOrNull()
                val productId = firstProduct?.key ?: ""

                FirebaseFirestore.getInstance().collection("products").document(productId).get()
                    .addOnSuccessListener { productDocument ->
                        val storeId = productDocument.getString("storeId") ?: ""
                        val transaction = Transaction(
                            id = "",
                            customerId = userId,
                            storeId = storeId,
                            productsMap = cart.productsMap,
                            remainPrice = 0.0,
                            status = "pending",
                            name = "John Doe",
                            address = "123 Main St, San Francisco, CA",
                            phoneNumber = "123-456-7890",
                            createdAt = currentDate
                        )
                        Log.d("CheckoutActivity", "Transaction: $transaction")
                        transactionRepository.createAndUpdateTransaction(transaction)
                    }
            }
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                // Handle payment success
                Toast.makeText(this, "Payment succeeded", Toast.LENGTH_LONG).show()

                // Launch a coroutine to perform suspend functions
                lifecycleScope.launch {
                    // Retrieve the cart once and use it for all operations
                    val cart = cartRepository.getCart(userId).firstOrNull()
                    cart?.let {
                        // Update inventory for each product in the cart
                        it.productsMap.forEach { (productId, quantity) ->
                            productRepository.updateProductInventory(productId, quantity)
                        }
                        Log.d("CheckoutActivity", "Cart: $cart")
                        // Clear the cart after successful checkout
                        cartRepository.clearCartAfterCheckout(userId)
                        Log.d("CheckoutActivity", "Cart cleared")
                        // Create a transaction record for the purchase
                        createTransactionFromCart(it)
                        Log.d("CheckoutActivity", "Transaction created")
                    }
                    // Redirect to the order history page
                    finish()
                }
            }
            is PaymentSheetResult.Canceled -> {
                // Handle payment cancellation
                Toast.makeText(this, "Payment canceled", Toast.LENGTH_LONG).show()
            }
            is PaymentSheetResult.Failed -> {
                // Handle payment failure
                Toast.makeText(
                    this,
                    "Payment failed: ${paymentSheetResult.error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }
}