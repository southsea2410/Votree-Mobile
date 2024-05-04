package com.example.votree.products.fragments

import android.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.votree.databinding.FragmentAddNewProductBinding
import com.example.votree.products.adapters.ProductImageAdapter
import com.example.votree.products.data.productCatagories.PlantType
import com.example.votree.products.data.productCatagories.SuitClimate
import com.example.votree.products.data.productCatagories.SuitEnvironment
import com.example.votree.products.models.Product
import com.example.votree.products.repositories.ProductRepository
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val IMAGE_REQUEST_CODE = 100

class AddNewProduct : Fragment() {
    private var _binding: FragmentAddNewProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var productRepository: ProductRepository
    private lateinit var firestore: FirebaseFirestore

    private val imageUris = mutableListOf<Uri>()
    private lateinit var imageAdapter: ProductImageAdapter

    //    private lateinit var permissionManager: PermissionManager
    private var imageUri: Uri? = null
    private lateinit var product: Product

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewProductBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        productRepository = ProductRepository(FirebaseFirestore.getInstance())

        setupSpinners()
        setupSaveButton()
        setupAddImageButton()

        return binding.root
    }

    private fun setupSpinners() {
        val plantType = PlantType.entries.toTypedArray()
        val suitEnvironment = SuitEnvironment.entries.toTypedArray()
        val suitClimate = SuitClimate.entries.toTypedArray()

        val plantTypeAdapter =
            ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, plantType)
        val suitEnvironmentAdapter =
            ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, suitEnvironment)
        val suitClimateAdapter =
            ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, suitClimate)

        (binding.suitClimateSpinner as? MaterialAutoCompleteTextView)?.setAdapter(suitClimateAdapter)
        (binding.suitEnvironmentSpinner as? MaterialAutoCompleteTextView)?.setAdapter(
            suitEnvironmentAdapter
        )
        (binding.typeSpinner as? MaterialAutoCompleteTextView)?.setAdapter(plantTypeAdapter)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getImageFromDevice() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupAddImageButton() {
        binding.addImageBtn.setOnClickListener {
            getImageFromDevice()
        }
    }

//    private fun setupSaveButton() {
//        binding.saveProductBtn.setOnClickListener {
//            val productName = binding.productNameEt.text.toString()
//            val shortDescription = binding.shortDescriptionEt.text.toString()
//            val longDescription = binding.descriptionEt.text.toString()
//            val price = binding.priceEt.text.toString()
//            val quantity = binding.quantityEt.text.toString()
//            val type = binding.typeSpinner.text.toString()
//            val suitEnvironment = binding.suitEnvironmentSpinner.text.toString()
//            val suitClimate = binding.suitClimateSpinner.text.toString()
//            val saleOff = binding.saleOffEt.text.toString()
//
//            if (imageUri != null) {
//                productRepository.uploadProductImage(imageUri!!, onSuccess = { imageUrl ->
//                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
//                    Log.d("AddNewProduct", "User ID: $userId")
//                    firestore.collection("users").document(userId).get()
//                        .addOnSuccessListener { document ->
//                            Log.d("AddNewProduct", "DocumentSnapshot data: ${document.data}")
//                            if (document != null) {
//                                val storeId = document.getString("storeId") ?: ""
//                                product = Product(
//                                    id = "",
//                                    storeId = storeId,
//                                    imageUrl = imageUris.map { it.toString() },
//                                    productName = productName,
//                                    shortDescription = shortDescription,
//                                    description = longDescription,
//                                    averageRate = 0.0,
//                                    quantityOfRate = 0,
//                                    price = price.toDouble(),
//                                    inventory = quantity.toInt(),
//                                    quantitySold = 0,
//                                    type = PlantType.valueOf(type),
//                                    suitEnvironment = SuitEnvironment.valueOf(suitEnvironment),
//                                    suitClimate = SuitClimate.valueOf(suitClimate),
//                                    saleOff = saleOff.toDouble()
//                                )
//
//                                productRepository.addProduct(product, onSuccess = { productId ->
//                                    findNavController().popBackStack()
//                                }, onFailure = { exception ->
//                                    Toast.makeText(
//                                        requireContext(),
//                                        "Error: ${exception.message}",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                })
//                            } else {
//                                Log.d("AddNewProduct", "No such document")
//                            }
//                        }
//                        .addOnFailureListener { exception ->
//                            Log.d("AddNewProduct", "get failed with ", exception)
//                        }
//                }, onFailure = { exception ->
//                    Toast.makeText(
//                        requireContext(),
//                        "Error uploading image: ${exception.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    Log.d("AddNewProduct", "Error uploading image: ${exception.message}")
//                })
//            } else {
//                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT)
//                    .show()
//                Log.d("AddNewProduct", "Image URI is null")
//            }
//        }
//    }

    private fun setupSaveButton() {
        binding.saveProductBtn.setOnClickListener {
            val productName = binding.productNameEt.text.toString()
            val shortDescription = binding.shortDescriptionEt.text.toString()
            val longDescription = binding.descriptionEt.text.toString()
            val price = binding.priceEt.text.toString()
            val quantity = binding.quantityEt.text.toString()
            val type = binding.typeSpinner.text.toString()
            val suitEnvironment = binding.suitEnvironmentSpinner.text.toString()
            val suitClimate = binding.suitClimateSpinner.text.toString()
            val saleOff = binding.saleOffEt.text.toString()

            if (imageUris.isNotEmpty()) {
                val imageUrls = mutableListOf<String>()
                for (imageUri in imageUris) {
                    productRepository.uploadProductImage(imageUri, onSuccess = { imageUrl ->
                        imageUrls.add(imageUrl)
                        if (imageUrls.size == imageUris.size) {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            Log.d("AddNewProduct", "User ID: $userId")
                            firestore.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    Log.d(
                                        "AddNewProduct",
                                        "DocumentSnapshot data: ${document.data}"
                                    )
                                    if (document != null) {
                                        val storeId = document.getString("storeId") ?: ""
                                        product = Product(
                                            id = "",
                                            storeId = storeId,
                                            imageUrl = imageUrls,
                                            productName = productName,
                                            shortDescription = shortDescription,
                                            description = longDescription,
                                            averageRate = 0.0,
                                            quantityOfRate = 0,
                                            price = price.toDouble(),
                                            inventory = quantity.toInt(),
                                            quantitySold = 0,
                                            type = PlantType.valueOf(type),
                                            suitEnvironment = SuitEnvironment.valueOf(
                                                suitEnvironment
                                            ),
                                            suitClimate = SuitClimate.valueOf(suitClimate),
                                            saleOff = saleOff.toDouble()
                                        )

                                        productRepository.addProduct(
                                            product,
                                            onSuccess = { productId ->
                                                findNavController().popBackStack()
                                            },
                                            onFailure = { exception ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Error: ${exception.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            })
                                    } else {
                                        Log.d("AddNewProduct", "No such document")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.d("AddNewProduct", "get failed with ", exception)
                                }
                        }
                    }, onFailure = { exception ->
                        Toast.makeText(
                            requireContext(),
                            "Error uploading image: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("AddNewProduct", "Error uploading image: ${exception.message}")
                    })
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select at least one image",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.d("AddNewProduct", "No image selected")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                imageUris.add(imageUri)
                updateImageViewPager()
            }
        }
    }

    private fun updateImageViewPager() {
        if (imageUris.isNotEmpty()) {
            imageAdapter = ProductImageAdapter(imageUris)
            binding.productImageViewPager.adapter = imageAdapter
        }
    }
//    private fun updateImageViewPager() {
//        if (imageUris.isNotEmpty()) {
//            val imageUrls = imageUris.map { it.toString() }
//            imageAdapter = ProductImageAdapter(imageUrls)
//            binding.productImageViewPager.adapter = imageAdapter
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}