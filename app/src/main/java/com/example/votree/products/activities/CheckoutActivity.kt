package com.example.votree.products.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

class CheckoutActivity : AppCompatActivity() {
    private lateinit var paymentSheet: PaymentSheet
    private var paymentIntentClientSecret: String? = null
    private lateinit var functions: FirebaseFunctions
    var customerId = "" // Replace with your actual Stripe customer ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

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

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                // Handle payment success
                Toast.makeText(this, "Payment succeeded", Toast.LENGTH_LONG).show()
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