package com.example.votree.fragment.product

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.votree.R
import com.example.votree.data.product.Product
import com.example.votree.data.product.ProductViewModel
import com.example.votree.data.productCatagories.PlantType
import com.example.votree.data.productCatagories.SuitClimate
import com.example.votree.data.productCatagories.SuitEnvironment
import com.example.votree.databinding.FragmentAddNewProductBinding

class AddNewProduct : Fragment() {
    private var _binding: FragmentAddNewProductBinding? = null
    private val binding get() = _binding!!
    private var productViewModel: ProductViewModel? = null

    private lateinit var plantType: PlantType
    private lateinit var suitEnvironment: SuitEnvironment
    private lateinit var suitClimate: SuitClimate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewProductBinding.inflate(inflater, container, false)
        productViewModel = ProductViewModel(requireActivity().application)

        val plantType = PlantType.entries.toTypedArray()
        val suitEnvironment = SuitEnvironment.entries.toTypedArray()
        val suitClimate = SuitClimate.entries.toTypedArray()

        val plantTypeAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, plantType)
        val suitEnvironmentAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, suitEnvironment)
        val suitClimateAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, suitClimate)

        plantTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        suitEnvironmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        suitClimateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.typeSpinner.adapter = plantTypeAdapter
        binding.suitEnvironmentSpinner.adapter = suitEnvironmentAdapter
        binding.suitClimateSpinner.adapter = suitClimateAdapter

        binding.saveProductBtn.setOnClickListener {
            insertDataToDatabase()
        }
        return binding.root
    }

    private fun insertDataToDatabase() {
        val productName = binding.productNameEt.text.toString()
        val shortDescription = binding.shortDescriptionEt.text.toString()
        val longDescription = binding.descriptionEt.text.toString()
        val price = binding.priceEt.text.toString()
        val quantity = binding.quantityEt.text.toString()
        val type = binding.typeSpinner.selectedItem.toString()
        val suitEnvironment = binding.suitEnvironmentSpinner.selectedItem.toString()
        val suitClimate = binding.suitClimateSpinner.selectedItem.toString()
        val saleOff = binding.saleOffEt.text.toString()

        if (inputCheck(
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
            // Create Product Object
            val product = Product(
                0,
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
            // Add Data to Database
            productViewModel!!.addProduct(product)

            Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_LONG).show()
            // Navigate Back
            findNavController().navigate(R.id.action_addNewProduct_to_productList)
        }
    }

    private fun inputCheck(
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
        return !(TextUtils.isEmpty(productName) && TextUtils.isEmpty(shortDescription) &&
                TextUtils.isEmpty(longDescription) && TextUtils.isEmpty(price) && TextUtils.isEmpty(
            quantity
        ) &&
                TextUtils.isEmpty(type) && TextUtils.isEmpty(suitEnvironment) && TextUtils.isEmpty(
            suitClimate
        ) && TextUtils.isEmpty(saleOff))
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}