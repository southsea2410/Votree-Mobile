package com.example.votree.users.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.databinding.ActivityRegisterToSellerBinding
import com.example.votree.users.models.Store
import com.example.votree.users.repositories.StoreRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class RegisterToSeller : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterToSellerBinding
    private val storeRepository = StoreRepository()
    private val storageReference = FirebaseStorage.getInstance().getReference("images/storeAvatars")
    private var avatarUri = Uri.EMPTY

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

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.shopAvatarIv.setImageURI(uri)
            avatarUri = uri
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
            uploadImageToFirebase(avatarUri)
        }

        // Set up the button click listener
        binding.uploadAvatarBtn.setOnClickListener {
            // Launch the image picker
            getImage.launch("image/*")
        }
    }

    private fun createNewStore(avatarUrl: String) {
        if (validateForm()) {
            val store = Store(
                id = "",
                storeName = binding.etShopName.text.toString(),
                storeLocation = binding.etShopAddress.text.toString(),
                storeEmail = binding.etShopEmail.text.toString(),
                storePhoneNumber = binding.etShopPhone.text.toString(),
                storeAvatar = avatarUrl
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = Firebase.auth.currentUser?.uid ?: ""
                    storeRepository.createNewStore(store, userId)
                    runOnUiThread {
                        Toast.makeText(this@RegisterToSeller, "Store created successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@RegisterToSeller, "Failed to create store: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val fileRef = storageReference.child(fileName)
        val uploadTask = fileRef.putFile(fileUri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            fileRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                createNewStore(downloadUri.toString())
            } else {
                Toast.makeText(this, "Upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
