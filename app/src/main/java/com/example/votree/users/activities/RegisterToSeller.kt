package com.example.votree.users.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.databinding.ActivityRegisterToSellerBinding
import com.example.votree.users.models.Store
import com.example.votree.users.repositories.StoreRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterToSeller : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterToSellerBinding
    private val storeRepository = StoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterToSellerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButton()
        setupAddress()
    }

    // Handle the address result from AddressActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADDRESS_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the address from the result
            val address = data?.getStringExtra("address")
            binding.etShopAddress.setText(address)
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.etShopName.text.toString().trim().isEmpty()) {
            binding.etShopName.error = "Shop name is required"
            isValid = false
        }

        if (binding.etShopAddress.text.toString().trim().isEmpty()) {
            binding.etShopAddress.error = "Shop address is required"
            isValid = false
        }

        if (binding.etShopEmail.text.toString().trim().isEmpty()) {
            binding.etShopEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etShopEmail.text.toString().trim()).matches()) {
            binding.etShopEmail.error = "Invalid email format"
            isValid = false
        }

        if (binding.etShopPhone.text.toString().trim().isEmpty()) {
            binding.etShopPhone.error = "Phone number is required"
            isValid = false
        }

        return isValid
    }

    private fun setupButton() {
        binding.btnRegister.setOnClickListener {
            if (validateForm()) {
                // return the result to the parent calling
                val intent = Intent()
                intent.putExtra("role", "store")
                // Create a store object and pass it to the parent activity
                val store = Store(
                    id = "",
                    storeName = binding.etShopName.text.toString(),
                    storeLocation = binding.etShopAddress.text.toString(),
                    storeEmail = binding.etShopEmail.text.toString(),
                    storePhoneNumber = binding.etShopPhone.text.toString()
                )
                // Call the storeRepository to create new store in the database
                CoroutineScope(Dispatchers.IO).launch {
                    try{
                        val userId = Firebase.auth.currentUser?.uid ?: ""
                        storeRepository.createNewStore(store, userId)

                        // Return the result to the parent activity
                        val intent = Intent()
                        intent.putExtra("role", "store")
                        intent.putExtra("store", store)
                        setResult(RESULT_OK, intent)
                        finish()
                    } catch (e: Exception){
                        runOnUiThread{
                            Toast.makeText(this@RegisterToSeller, "Failed to create store: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

//                intent.putExtra("store", store)
//                setResult(RESULT_OK, intent)
//                finish()
            }
        }
    }

    private fun setupAddress() {
        binding.etShopAddress.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            startActivityForResult(intent, ADDRESS_REQUEST_CODE)
        }
    }

    companion object {
        const val ADDRESS_REQUEST_CODE = 1111
        const val REGISTER_TO_SELLER_CODE = 1110
    }
}
