package com.example.votree.products.fragments

import android.Manifest
import android.R
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.votree.databinding.FragmentAddNewProductBinding
import com.example.votree.products.data.productCatagories.PlantType
import com.example.votree.products.data.productCatagories.SuitClimate
import com.example.votree.products.data.productCatagories.SuitEnvironment
import com.example.votree.products.models.Product
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

private const val IMAGE_REQUEST_CODE = 100
private const val PERMISSION_REQUEST_CODE = 101

@Suppress("DEPRECATION")
class AddNewProduct : Fragment() {
    private var _binding: FragmentAddNewProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore

    private lateinit var product: Product
    private lateinit var imageUri: Uri

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewProductBinding.inflate(inflater, container, false)

        // Initialize Firestore
        FirebaseApp.initializeApp(requireContext())
        firestore = FirebaseFirestore.getInstance()

        // Request permission for accessing images and videos if not granted
        if (!checkPermission()) {
            requestPermission()
        }

        setupSpinners()
        setupSaveButton()
        setupAddImageButton() // New function to set up the click listener for addImage_btn

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private var required_permissions = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
            PERMISSION_REQUEST_CODE
        )
    }

    // Add this function to set up click listener for addImage_btn
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupAddImageButton() {
        binding.addImageBtn.setOnClickListener {
            getImageFromDevice()
        }
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

//        suitEnvironmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        suitClimateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        (binding.suitClimateSpinner as? MaterialAutoCompleteTextView)?.setAdapter(suitClimateAdapter)
        (binding.suitEnvironmentSpinner as? MaterialAutoCompleteTextView)?.setAdapter(
            suitEnvironmentAdapter
        )
        (binding.typeSpinner as? MaterialAutoCompleteTextView)?.setAdapter(plantTypeAdapter)
//        binding.typeSpinner.adapter = plantTypeAdapter
//        binding.suitEnvironmentSpinner.adapter = suitEnvironmentAdapter
//        binding.suitClimateSpinner.adapter = suitClimateAdapter
    }

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

            var imageUrl = ""

            if (imageUri == null) {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else {
                imageUrl = imageUri.toString()
            }

            if (inputCheck(
                    imageUrl,
                    productName,
                    shortDescription,
                    longDescription,
                    price,
                    quantity,
                    type,
                    suitEnvironment,
                    suitClimate,
                    saleOff
                )
            ) {
                // Using firestore auto-generated for id
                product = Product(
                    "",
                    imageUrl,
                    productName,
                    shortDescription,
                    longDescription,
                    0.0,
                    0,
                    price.toDouble(),
                    quantity.toInt(),
                    0,
                    PlantType.valueOf(type),
                    SuitEnvironment.valueOf(suitEnvironment),
                    SuitClimate.valueOf(suitClimate),
                    saleOff.toDouble()
                )
                pushProductToDatabase(product, imageUri)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getImageFromDevice() {
        if (checkPermission()) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_REQUEST_CODE)
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data!!
            binding.productImageIv.setImageURI(imageUri)
            try {
                val bitmap =
                    MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                binding.productImageIv.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.d("AddNewProduct", "Image URI: $imageUri")
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT)
                        .show()
                    getImageFromDevice()
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun inputCheck(
        imageUrl: String,
        productName: String,
        shortDescription: String,
        longDescription: String,
        price: String,
        quantity: String,
        type: String,
        suitEnvironment: String,
        suitClimate: String,
        saleOff: String
    ): Boolean {
        return !(TextUtils.isEmpty(productName) && TextUtils.isEmpty(shortDescription) && TextUtils.isEmpty(
            longDescription
        )
                && TextUtils.isEmpty(price) && TextUtils.isEmpty(quantity) && TextUtils.isEmpty(type) &&
                TextUtils.isEmpty(suitEnvironment) && TextUtils.isEmpty(suitClimate) && TextUtils.isEmpty(
            saleOff
        ) && imageUrl.isEmpty())
    }

    private fun pushProductToDatabase(product: Product, imageUri: Uri) {
        val formatter = java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
        val now = java.util.Calendar.getInstance().time
        val fileName = formatter.format(now)
        val storageRef = FirebaseStorage.getInstance().reference.child("images/products/$fileName")

        storageRef.putFile(imageUri).addOnSuccessListener {
            binding.productImageIv.setImageURI(null)

            storageRef.downloadUrl.addOnSuccessListener { uri ->
                product.imageUrl = uri.toString()
                firestore.collection("products").add(product)
                    .addOnSuccessListener { documentReference ->
                        val documentId = documentReference.id.toString()
                        firestore.collection("products").document(documentId)
                            .update("id", documentId)
                        Toast.makeText(
                            requireContext(),
                            "Product added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(com.example.votree.R.id.action_addNewProduct_to_productList)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}