package com.example.votree.users.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.votree.R
import com.example.votree.databinding.FragmentStoreManagementBinding
import com.example.votree.products.adapters.ProductAdapter
import com.example.votree.products.models.Product
import com.example.votree.products.repositories.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StoreManagementFragment : Fragment() {
    private var _binding: FragmentStoreManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val productRepository = ProductRepository(firestore)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStoreManagementBinding.inflate(inflater, container, false)

        binding.addNewProductBtn.setOnClickListener {
            navigateToAddNewProductFragment()
        }

//        setupRecyclerView()
//        loadProducts()

        return binding.root
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter()
        binding.storeManagementRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2) // Sử dụng GridLayoutManager với 2 cột
            adapter = productAdapter
        }
    }

    private fun loadProducts() {
        // Get current user's store id
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("StoreManagementFragment", "DocumentSnapshot data: ${document.data}")

                    val storeId = document.getString("storeId") ?: ""
                    productRepository.fetchProducts(storeId, onSuccess = { productList ->
                        productAdapter.setData(productList)
                    }, onFailure = { exception ->
                        Log.d("StoreManagementFragment", "Error getting documents: ", exception)
                    })
                } else {
                    Log.d("StoreManagementFragment", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("StoreManagementFragment", "get failed with ", exception)
            }
    }

    private fun navigateToAddNewProductFragment() {
        // Use the findNavController() method to get the NavController
        val navController = findNavController()
        navController.navigate(R.id.action_storeManagement2_to_addNewProduct2)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadProducts()

        productAdapter.setOnProductClickListener(object : ProductAdapter.OnProductClickListener {
            override fun onProductClick(product: Product) {
                val action =
                    StoreManagementFragmentDirections.actionStoreManagement2ToProductDetail2(product)
                findNavController().navigate(action)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}