package com.example.votree.users.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R
import com.example.votree.databinding.FragmentStoreManagementBinding
import com.example.votree.products.adapters.ProductAdapter
import com.example.votree.products.models.Product
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.utils.CustomToast
import com.example.votree.utils.GridSpacingItemDecoration
import com.example.votree.utils.ToastType
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class StoreManagementFragment : Fragment() {
    private var _binding: FragmentStoreManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val productRepository = ProductRepository(firestore)
    private lateinit var products: MutableList<Product>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreManagementBinding.inflate(inflater, container, false)
        products = mutableListOf()
        binding.addNewProductBtn.setOnClickListener { navigateToAddNewProductFragment() }
        binding.orderManagementBtn.setOnClickListener { navigateToOrderManagementFragment() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupTabLayout()
        loadProducts()
        setupProductClick()
    }

    private fun setupRecyclerView() {
        // Calculate the number of columns based on the screen width and desired column width
        val numberOfColumns = calculateNoOfColumns(requireContext())
        productAdapter = ProductAdapter()

        binding.storeManagementRecyclerView.apply {
            layoutManager = GridLayoutManager(context, numberOfColumns)
            adapter = productAdapter
            setHasFixedSize(true)

            addItemDecoration(GridSpacingItemDecoration(numberOfColumns, 10, true))
        }

        binding.storeManagementRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) { // Check if the recycler view can scroll down
                    loadProducts()
                }
            }
        })
    }

    private fun calculateNoOfColumns(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val columnWidthDp = 180
        return (screenWidthDp / columnWidthDp).toInt()
    }

    private fun setupTabLayout() {
        binding.productFilterTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val filters = when (tab?.position) {
                    0 -> mapOf<String, Any>()
                    1 -> mapOf("active" to true)
                    2 -> mapOf("active" to false)
                    else -> mapOf<String, Any>() // Add more filters as needed
                }
                loadProductsBasedOnFilters(filters)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadProductsBasedOnFilters(filters: Map<String, Any>) = lifecycleScope.launch {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        try {
            val document = withContext(Dispatchers.IO) {
                firestore.collection("users").document(userId).get().await()
            }
            val storeId = document.getString("storeId") ?: ""
            var query = firestore.collection("products").whereEqualTo("storeId", storeId)
            filters.forEach { (key, value) -> query = query.whereEqualTo(key, value) }
            val snapshot = withContext(Dispatchers.IO) { query.get().await() }
            val productList = snapshot.toObjects(Product::class.java)
            productAdapter.setData(productList)
        } catch (e: Exception) {
            Log.d("StoreManagementFragment", "Error getting products based on filters: ", e)
        }
    }

    private fun setupProductClick() {
        productAdapter.setOnProductClickListener(object : ProductAdapter.OnProductClickListener {
            override fun onProductClick(product: Product) {
                val action =
                    StoreManagementFragmentDirections.actionStoreManagement2ToProductDetailFragmentForStore(
                        product
                    )
                findNavController().navigate(action)
            }
        })
    }

    private var lastVisibleProduct: DocumentSnapshot? = null
    private val pageSize = 5

    private fun loadProducts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val storeId = document.getString("storeId") ?: ""
                var query = firestore.collection("products")
                    .whereEqualTo("storeId", storeId)
                    .orderBy("createdAt")
                    .limit(pageSize.toLong())

                lastVisibleProduct?.let {
                    query = query.startAfter(it)
                }

                query.get()
                    .addOnSuccessListener { snapshot ->
                        Log.d("StoreManagementFragment", "Products: ${snapshot.documents.size}")
                        val newProducts = snapshot.toObjects(Product::class.java)
                        if (newProducts.isNotEmpty()) {
                            lastVisibleProduct = snapshot.documents[snapshot.size() - 1]
                            productAdapter.setData(newProducts)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("StoreManagementFragment", "Error getting documents: ", exception)
                        CustomToast.show(
                            requireContext(),
                            "Error getting products",
                            ToastType.FAILURE
                        )
                    }
            }
    }

    private fun navigateToAddNewProductFragment() {
        findNavController().navigate(R.id.action_storeManagement2_to_addNewProduct2)
    }

    private fun navigateToOrderManagementFragment() {
        findNavController().navigate(R.id.action_storeManagement2_to_orderManagementForStoreFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}