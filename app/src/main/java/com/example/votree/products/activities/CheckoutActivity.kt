package com.example.votree.products.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.votree.R
import com.example.votree.products.models.Cart
import com.example.votree.products.models.PointTransaction
import com.example.votree.products.models.ShippingAddress
import com.example.votree.products.models.Transaction
import com.example.votree.products.repositories.CartRepository
import com.example.votree.products.repositories.PointTransactionRepository
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.products.repositories.TransactionRepository
import com.example.votree.users.repositories.StoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

@Suppress("DEPRECATION")
class CheckoutActivity : AppCompatActivity() {
    private lateinit var paymentSheet: PaymentSheet
    private var paymentIntentClientSecret: String? = null
    private lateinit var functions: FirebaseFunctions
    private var customerId: String = ""

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
        initializeStripePaymentConfiguration()

        // Initialize PaymentSheet
        initializePaymentSheet()

        // Fetch or create a Stripe customer
        fetchOrCreateStripeCustomer()
    }

    private fun initializeStripePaymentConfiguration() {
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51OM54hL1bECNnFcvEJXdx2V7gJ6RxaDq7WVV1Jw3UZydI3Cag3lrQLQFwbadyM7Rp5uj8LdRXBSlAS0x5cJVuxuc00Yx8eUBEk" // Replace with your actual publishable key
        )
    }

    private fun initializePaymentSheet() {
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
    }

    private fun fetchOrCreateStripeCustomer() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        currentUser?.let { user ->
            db.collection("stripe_customers").document(user.uid).get()
                .addOnSuccessListener { document ->
                    customerId = document.getString("customer_id") ?: ""
                    fetchPaymentIntentClientSecret()
                }
                .addOnFailureListener { exception ->
                    createStripeCustomer(user)
                }
        }
    }

    private fun createStripeCustomer(user: com.google.firebase.auth.FirebaseUser) {
        val data = hashMapOf(
            "email" to user.email
        )
        functions.getHttpsCallable("createStripeCustomer").call(data)
            .addOnSuccessListener { result ->
                val customer = result.data as Map<String, Any>
                customerId = customer["id"] as String
                storeStripeCustomerId(user.uid, customerId)
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

    private fun storeStripeCustomerId(userId: String, customerId: String) {
        FirebaseFirestore.getInstance().collection("stripe_customers").document(userId)
            .set(mapOf("customer_id" to customerId))
    }

    private fun fetchPaymentIntentClientSecret() {
        val amount = intent.getStringExtra("totalAmount")?.toFloat()?.times(100) ?: 0
        val currency = "usd"

        val data = hashMapOf(
            "amount" to amount.toLong(),
            "currency" to currency,
            "customerId" to customerId
        )

        functions
            .getHttpsCallable("createPaymentIntent")
            .call(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result?.data as Map<String, Any>
                    paymentIntentClientSecret = result["clientSecret"] as String?
                    configurePaymentSheet()
                } else {
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
            val configuration = PaymentSheet.Configuration("VoTree")
            paymentSheet.presentWithPaymentIntent(secret, configuration)
        }
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(this, "Payment succeeded", Toast.LENGTH_LONG).show()
                handleSuccessfulPayment()
            }

            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Payment canceled", Toast.LENGTH_LONG).show()
            }

            is PaymentSheetResult.Failed -> {
                Toast.makeText(
                    this,
                    "Payment failed: ${paymentSheetResult.error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun handleSuccessfulPayment() {
        lifecycleScope.launch {
            val cart = intent.getParcelableExtra<Cart>("cart")
            val receiver = intent.getParcelableExtra<ShippingAddress>("receiver")
            cart?.let {
                receiver?.let {
                    updateProductInventory(cart)
                    createTransactionFromCart(cart, receiver)
                    updateProductSoldQuantity(cart)
                    clearCartAfterCheckout(cart)
                    finish()
                }
            }
        }
    }

    private suspend fun updateProductInventory(cart: Cart) {
        cart.productsMap.forEach { (productId, quantity) ->
            productRepository.updateProductInventory(productId, quantity)
        }
    }

    private suspend fun updateProductSoldQuantity(cart: Cart) {
        cart.productsMap.forEach { (productId, quantity) ->
            productRepository.updateProductSoldQuantity(productId, quantity)
        }
    }

    private fun notifyStoreAboutNewOrder(transaction: Transaction) {
        val data = hashMapOf(
            "senderId" to userId,
            "receiverId" to transaction.storeId,
            "collectionPath" to "stores",
            "title" to "New Order",
            "body" to "You have a new order!",
            "data" to hashMapOf("orderId" to transaction.id)
        )

        functions.getHttpsCallable("sendNotification").call(data)
            .addOnSuccessListener {
                Log.d(TAG, "Notification sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error sending notification", e)
            }
    }

    // Function to earn points after successful payment
    private fun earnPointsAfterPayment(totalAmount: Double, storeId: String) {
        val storeRepository = StoreRepository()
        CoroutineScope(lifecycleScope.coroutineContext).launch {
            val storeName = storeRepository.getStoreName(storeId)
            val description = "$storeName"
            val pointTransaction = PointTransaction(
                userId = userId,
                points = totalAmount.toInt(),
                type = "earn",
                description = description,
                transactionDate = Date()
            )
            lifecycleScope.launch {
                val pointTransactionRepository = PointTransactionRepository()
                pointTransactionRepository.addPointTransaction(pointTransaction)
            }
        }
    }

    private fun createTransactionFromCart(cart: Cart, receiver: ShippingAddress) {
        val currentDate = Date()

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
                    name = receiver.recipientName,
                    address = receiver.recipientAddress,
                    phoneNumber = receiver.recipientPhoneNumber,
                    createdAt = currentDate
                )
                Log.d(TAG, "Transaction: $transaction")
                lifecycleScope.launch {
                    val totalAmount =
                        transactionRepository.calculateTotalPrice(transaction.productsMap)
                    transaction.totalAmount = totalAmount + 10.0 // Add delivery fee
                    transactionRepository.createAndUpdateTransaction(transaction)
                    notifyStoreAboutNewOrder(transaction)

                    // Earn points after successful payment
                    earnPointsAfterPayment(totalAmount, storeId)
                }
            }
    }

    private suspend fun clearCartAfterCheckout(cart: Cart) {
        cartRepository.clearCartAfterCheckout(userId, cart)
    }

    companion object {
        private const val TAG = "CheckoutActivity"
    }
}