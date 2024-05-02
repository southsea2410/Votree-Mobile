//package com.example.votree.products.activities
//
//import android.app.Activity
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.provider.MediaStore
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.lifecycleScope
//import com.example.votree.databinding.ActivityProductReviewBinding
//import com.example.votree.products.models.ProductReview
//import com.example.votree.products.repositories.ProductRepository
//import com.example.votree.products.repositories.ProductReviewRepository
//import com.example.votree.utils.CustomToast
//import com.example.votree.utils.ToastType
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.storage.FirebaseStorage
//import kotlinx.coroutines.launch
//
//class ProductReviewActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityProductReviewBinding
//
//    private var imageUri: Uri? = null
//    private val firestore = FirebaseFirestore.getInstance()
//
//    private val productReviewRepository = ProductReviewRepository(firestore)
//    private val productRepository = ProductRepository(firestore)
//    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            imageUri = result.data?.data
//            binding.productImageView.setImageURI(imageUri)
//        }
//    }
//
//    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityProductReviewBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        setupButton(binding)
//        setupReviewSubmitted()
//    }
//
//    private fun setupReviewSubmitted(){
//        val isSubmitted = intent.getBooleanExtra("isSubmitted", false)
//        val transactionId = intent.getStringExtra("transactionId") ?: ""
//
//        if (isSubmitted) {
//            // Using coroutine to fetch review
//            lifecycleScope.launchWhenCreated {
//                try {
//                    val review = productReviewRepository.fetchReviewByTransactionId(transactionId, userId)
//                    // Populate the UI with the fetched review data
//                    binding.reviewEditText.setText(review?.reviewText)
//                    binding.ratingBar.rating = review?.rating ?: 0f
//                    imageUri = Uri.parse(review?.imageUrl)
//                    binding.productImageView.setImageURI(imageUri)
//                } catch (e: Exception) {
//                    CustomToast.show(this@ProductReviewActivity, "Failed to fetch review: ${e.message}", ToastType.FAILURE)
//                }
//            }
//        }
//    }
//
//    private fun getImageFromDevice() {
//        if (checkPermission()) {
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            getImage.launch(intent)
//        } else {
//            requestPermission()
//        }
//    }
//
//    private fun checkPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun requestPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), IMAGE_REQUEST_CODE)
//        }
//    }
//
//    companion object {
//        const val IMAGE_REQUEST_CODE = 1001
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == IMAGE_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getImageFromDevice()
//            } else {
//                CustomToast.show(this, "Permission denied", ToastType.FAILURE)
//            }
//        }
//    }
//
////    private fun setupButton(binding: ActivityProductReviewBinding)
////    {
////        binding.addImageButton.setOnClickListener {
////            getImageFromDevice()
////        }
////
////        binding.submitReviewButton.setOnClickListener {
////            val reviewText = binding.reviewEditText.text.toString()
////            val rating = binding.ratingBar.rating
////            val transactionId = intent.getStringExtra("transactionId") ?: ""
////            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
////
////            if(imageUri != null){
////                productRepository.uploadProductImage(imageUri!!, onSuccess = { imageUrl ->
////                    val productReview = ProductReview(
////                        id = "",
////                        transactionId = transactionId,
////                        userId = userId,
////                        reviewText = reviewText,
////                        rating = rating,
////                        imageUrl = imageUrl
////                    )
////
////                    productReviewRepository.addProductReview(productReview, transactionId, onSuccess = {
////                        CustomToast.show(this, "Review submitted successfully", ToastType.SUCCESS)
////                        finish()
////                    }, onFailure = {
////                        CustomToast.show(this, "Failed to submit review: ${it.message}", ToastType.FAILURE)
////                    })
////                }, onFailure = {
////                    CustomToast.show(this, "Failed to upload image: ${it.message}", ToastType.FAILURE)
////                })
////            }
////        }
////    }
//
//    private fun setupButton(binding: ActivityProductReviewBinding) {
//        binding.addImageButton.setOnClickListener {
//            getImageFromDevice()
//        }
//
//        binding.submitReviewButton.setOnClickListener {
//            val reviewText = binding.reviewEditText.text.toString()
//            val rating = binding.ratingBar.rating
//            val transactionId = intent.getStringExtra("transactionId") ?: ""
//
//            if (imageUri != null) {
//                // Check if it's an update or a new submission
//                val isSubmitted = intent.getBooleanExtra("isSubmitted", false)
//                if (isSubmitted) {
//                    updateReview(transactionId, reviewText, rating, imageUri!!)
//                } else {
//                    addNewReview(transactionId, reviewText, rating, imageUri!!)
//                }
//            }
//        }
//    }
//
//    private fun updateReview(transactionId: String, reviewText: String, rating: Float, newImageUri: Uri) {
//        lifecycleScope.launch {
//            try {
//                val existingReview = productReviewRepository.fetchReviewByTransactionId(transactionId, userId)
//                if (existingReview != null) {
//                    // Check if the image has changed
//                    if (existingReview.imageUrl != newImageUri.toString()) {
//                        // Delete the old image
//                        val oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(
//                            existingReview.imageUrl.toString()
//                        )
//                        oldImageRef.delete().addOnSuccessListener {
//                            // Upload the new image and update the review
//                            uploadAndUpdateReview(existingReview, newImageUri, reviewText, rating)
//                        }
//                    } else {
//                        // Update review without changing the image
//                        productReviewRepository.updateReview(existingReview.copy(reviewText = reviewText, rating = rating),
//                            onSuccess = {
//                                CustomToast.show(this@ProductReviewActivity, "Review updated successfully", ToastType.SUCCESS)
//                                finish()
//                            }, onFailure = {
//                                CustomToast.show(this@ProductReviewActivity, "Failed to update review: ${it.message}", ToastType.FAILURE)
//                            })
//                    }
//                }
//            } catch (e: Exception) {
//                CustomToast.show(this@ProductReviewActivity, "Failed to update review: ${e.message}", ToastType.FAILURE)
//            }
//        }
//    }
//
//    private fun uploadAndUpdateReview(existingReview: ProductReview, newImageUri: Uri, reviewText: String, rating: Float) {
//        productRepository.uploadProductImage(newImageUri, onSuccess = { imageUrl ->
//            val updatedReview = existingReview.copy(reviewText = reviewText, rating = rating, imageUrl = imageUrl)
//            productReviewRepository.updateReview(updatedReview, onSuccess = {
//                CustomToast.show(this, "Review updated successfully", ToastType.SUCCESS)
//                finish()
//            }, onFailure = {
//                CustomToast.show(this, "Failed to update review: ${it.message}", ToastType.FAILURE)
//            })
//        }, onFailure = {
//            CustomToast.show(this, "Failed to upload new image: ${it.message}", ToastType.FAILURE)
//        })
//    }
//
//    private fun addNewReview(transactionId: String, reviewText: String, rating: Float, imageUri: Uri) {
//        lifecycleScope.launch {
//            try {
//                productRepository.uploadProductImage(imageUri, onSuccess = { imageUrl ->
//                    val productReview = ProductReview(
//                        id = "",
//                        transactionId = transactionId,
//                        userId = userId,
//                        reviewText = reviewText,
//                        rating = rating,
//                        imageUrl = imageUrl
//                    )
//                    productReviewRepository.addProductReview(productReview, transactionId, onSuccess = {
//                        CustomToast.show(this@ProductReviewActivity, "Review submitted successfully", ToastType.SUCCESS)
//                        finish()
//                    }, onFailure = {
//                        CustomToast.show(this@ProductReviewActivity, "Failed to submit review: ${it.message}", ToastType.FAILURE)
//                    })
//                }, onFailure = {
//                    CustomToast.show(this@ProductReviewActivity, "Failed to upload image: ${it.message}", ToastType.FAILURE)
//                })
//            } catch (e: Exception) {
//                CustomToast.show(this@ProductReviewActivity, "Failed to submit review: ${e.message}", ToastType.FAILURE)
//            }
//        }
//    }
//}


package com.example.votree.products.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.votree.databinding.ActivityProductReviewBinding
import com.example.votree.products.models.ProductReview
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.products.repositories.ProductReviewRepository
import com.example.votree.utils.CustomToast
import com.example.votree.utils.ToastType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProductReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductReviewBinding
    private var imageUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val productReviewRepository = ProductReviewRepository(firestore)
    private val productRepository = ProductRepository(firestore)
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupButton()
        checkReviewSubmitted()
    }

    private fun setupButton() {
        binding.addImageButton.setOnClickListener { pickImage() }
        binding.submitReviewButton.setOnClickListener { submitReview() }
    }

    private fun pickImage() {
        if (hasImagePermission()) {
            Log.d("ProductReviewActivity", "Picking image")
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getImage.launch(pickImageIntent)
        } else {
            requestImagePermission()
        }
    }

    private fun hasImagePermission() = ContextCompat.checkSelfPermission(
        this, android.Manifest.permission.READ_MEDIA_IMAGES
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                Log.d("ProductReviewActivity", "Requesting permission")
                requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), IMAGE_REQUEST_CODE)
            }
        }
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.productImageView.setImageURI(imageUri)
        }
    }

    private fun checkReviewSubmitted() {
        val isSubmitted = intent.getBooleanExtra("isSubmitted", false)
        val transactionId = intent.getStringExtra("transactionId") ?: return

        if (isSubmitted) {
            lifecycleScope.launch {
                binding.submitReviewButton.text = "Update Review"
                productReviewRepository.fetchReviewByTransactionId(transactionId, userId)?.let { review ->
                    with(binding) {
                        reviewEditText.setText(review.reviewText)
                        ratingBar.rating = review.rating
                        imageUri = Uri.parse(review.imageUrl)
                        Glide.with(this@ProductReviewActivity).load(imageUri).into(productImageView)
                        productImageView.setImageURI(imageUri)
                    }
                } ?: CustomToast.show(this@ProductReviewActivity, "Failed to fetch review", ToastType.FAILURE)
            }
        }
    }

    private fun submitReview() {
        val reviewText = binding.reviewEditText.text.toString()
        val rating = binding.ratingBar.rating
        val transactionId = intent.getStringExtra("transactionId") ?: return

        imageUri?.let { uri ->
            lifecycleScope.launch {
                try {
                    productRepository.uploadProductImage(uri, { imageUrl ->
                        val review = ProductReview("", transactionId, userId, reviewText, rating, imageUrl)
                        if (intent.getBooleanExtra("isSubmitted", false)) {
                            updateReview(review)
                        } else {
                            addNewReview(review)
                        }
                    }, { e ->
                        CustomToast.show(this@ProductReviewActivity, "Failed to upload image: ${e.message}", ToastType.FAILURE)
                    })

                } catch (e: Exception) {
                    CustomToast.show(this@ProductReviewActivity, "Failed to submit review: ${e.message}", ToastType.FAILURE)
                }
            }
        }
    }

    private fun updateReview(review: ProductReview) {
        productReviewRepository.updateReview(review, onSuccess = {
            CustomToast.show(this, "Review updated successfully", ToastType.SUCCESS)
            finish()
        }, onFailure = {
            CustomToast.show(this, "Failed to update review: ${it.message}", ToastType.FAILURE)
        })
    }

    private fun addNewReview(review: ProductReview) {
        productReviewRepository.addProductReview(review, review.transactionId, onSuccess = {
            CustomToast.show(this@ProductReviewActivity, "Review submitted successfully", ToastType.SUCCESS)
            finish()
        }, onFailure = {
            CustomToast.show(this@ProductReviewActivity, "Failed to submit review: ${it.message}", ToastType.FAILURE)
        })
    }

    companion object {
        const val IMAGE_REQUEST_CODE = 1001
    }
}