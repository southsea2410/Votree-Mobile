package com.example.votree.products.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.votree.databinding.ActivityProductReviewAcitivityBinding
import com.example.votree.products.models.ProductReview
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.products.repositories.ProductReviewRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductReviewAcitivityBinding
    private var imageUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val productReviewRepository = ProductReviewRepository(firestore)
    private val productRepository = ProductRepository(firestore)
    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.productImageView.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductReviewAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButton(binding)
    }

    private fun getImageFromDevice() {
        if (checkPermission()) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getImage.launch(intent)
        } else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), IMAGE_REQUEST_CODE)
        }
    }

    companion object {
        const val IMAGE_REQUEST_CODE = 1001
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImageFromDevice()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButton(binding: ActivityProductReviewAcitivityBinding)
    {
        binding.addImageButton.setOnClickListener {
            getImageFromDevice()
        }

        binding.submitReviewButton.setOnClickListener {
            val reviewText = binding.reviewEditText.text.toString()
            val rating = binding.ratingBar.rating
            val transactionId = intent.getStringExtra("transactionId") ?: ""
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            if(imageUri != null){
                productRepository.uploadProductImage(imageUri!!, onSuccess = { imageUrl ->
                    val productReview = ProductReview(
                        id = "",
                        transactionId = transactionId,
                        userId = userId,
                        reviewText = reviewText,
                        rating = rating,
                        imageUrl = imageUrl
                    )

                    productReviewRepository.addProductReview(productReview, transactionId, onSuccess = {
                        Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }, onFailure = {
                        Toast.makeText(this, "Failed to submit review: ${it.message}", Toast.LENGTH_SHORT).show()
                    })
                }, onFailure = {
                    Toast.makeText(this, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
                })
            }
        }
    }
}
