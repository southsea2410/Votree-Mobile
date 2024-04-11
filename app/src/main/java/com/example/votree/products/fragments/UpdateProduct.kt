package com.example.votree.products.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.FragmentUpdateProductBinding
import com.example.votree.products.data.productCatagories.PlantType
import com.example.votree.products.data.productCatagories.SuitClimate
import com.example.votree.products.data.productCatagories.SuitEnvironment
import com.example.votree.products.models.Product
import com.example.votree.products.repositories.ProductRepository
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.firestore.FirebaseFirestore

class UpdateProduct : Fragment() {
    private var _binding: FragmentUpdateProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var productRepository: ProductRepository
    private val args by navArgs<UpdateProductArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateProductBinding.inflate(inflater, container, false)
        productRepository = ProductRepository(FirebaseFirestore.getInstance())

        setupViews()
        setupSpinners()
        setupSaveButton()

        return binding.root
    }

    private fun setupSpinners() {
        val plantType = PlantType.entries.toTypedArray()
        val suitEnvironment = SuitEnvironment.entries.toTypedArray()
        val suitClimate = SuitClimate.entries.toTypedArray()

        val plantTypeAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, plantType)
        val suitEnvironmentAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                suitEnvironment
            )
        val suitClimateAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suitClimate)

        (binding.suitClimateSpinner as? MaterialAutoCompleteTextView)?.setAdapter(suitClimateAdapter)
        (binding.suitEnvironmentSpinner as? MaterialAutoCompleteTextView)?.setAdapter(
            suitEnvironmentAdapter
        )
        (binding.typeSpinner as? MaterialAutoCompleteTextView)?.setAdapter(plantTypeAdapter)
    }

    private fun setupSaveButton() {
        binding.saveProductBtn.setOnClickListener {
            val product = Product(
                id = args.currentProduct.id,
                storeId = args.currentProduct.storeId,
                productName = binding.productNameEt.text.toString(),
                shortDescription = binding.shortDescriptionEt.text.toString(),
                description = binding.descriptionEt.text.toString(),
                price = binding.priceEt.text.toString().toDouble(),
                inventory = binding.quantityEt.text.toString().toInt(),
                type = binding.typeSpinner.text.toString().let { PlantType.valueOf(it) },
                suitEnvironment = binding.suitEnvironmentSpinner.text.toString()
                    .let { SuitEnvironment.valueOf(it) },
                suitClimate = binding.suitClimateSpinner.text.toString()
                    .let { SuitClimate.valueOf(it) },
                saleOff = binding.saleOffEt.text.toString().toDouble(),
//                imageUrl = binding.productImageIv.toString()
                imageUrl = args.currentProduct.imageUrl
            )

            productRepository.updateProduct(product) {
                Toast.makeText(requireContext(), "Product updated successfully", Toast.LENGTH_SHORT)
                    .show()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupViews() {
        val product = args.currentProduct
        binding.productNameEt.setText(product.productName)
        binding.shortDescriptionEt.setText(product.shortDescription)
        binding.descriptionEt.setText(product.description)
        binding.priceEt.setText(product.price.toString())
        binding.quantityEt.setText(product.inventory.toString())
        binding.saleOffEt.setText(product.saleOff.toString())
        binding.typeSpinner.setText(product.type.toString(), false)
        binding.suitEnvironmentSpinner.setText(product.suitEnvironment.toString(), false)
        binding.suitClimateSpinner.setText(product.suitClimate.toString(), false)

        Glide.with(this)
            .load(product.imageUrl)
            .placeholder(R.drawable.img_placeholder)
            .into(binding.productImageIv)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}