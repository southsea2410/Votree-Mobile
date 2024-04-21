package com.example.votree.products.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.votree.MainActivity
import com.example.votree.databinding.FragmentProductListBinding
import com.example.votree.products.adapters.ProductAdapter
import com.example.votree.products.models.Product
import com.example.votree.products.view_models.ProductViewModel

class ProductList : Fragment(), MainActivity.SearchQueryListener {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mFirebaseProductViewModel: ProductViewModel
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        setUpViewModel()
        navigateToProductDetail()
        setUpSearchQuery()
    }

    private fun setUpRecyclerView(){
        productAdapter = ProductAdapter()
        binding.productListRv.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
        }
    }

    private fun setUpViewModel(){
        mFirebaseProductViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        mFirebaseProductViewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.setData(products)
        }
    }

    private fun navigateToProductDetail(){
        productAdapter.setOnProductClickListener(object : ProductAdapter.OnProductClickListener {
            override fun onProductClick(product: Product) {
                val action = ProductListDirections.actionProductListToProductDetail(product, "user")
                findNavController().navigate(action)
            }
        })
    }

    private fun filterProducts(query: String) {
        if(query.isNotEmpty()) {
            mFirebaseProductViewModel.products.observe(viewLifecycleOwner) { products ->
                val filteredProducts = products.filter { product ->
                    product.productName.contains(query, ignoreCase = true)
                }
                productAdapter.setData(filteredProducts)
            }
        } else {
            resetProductList()
        }
    }

    private fun resetProductList(){
        mFirebaseProductViewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.setData(products)
        }
    }

    private fun setUpSearchQuery(){
        val query = arguments?.getString("query") ?: ""
        Log.d("ProductList", "Query: $query")
        filterProducts(query)
        (activity as MainActivity).setSearchQueryListener(this)
    }

    override fun onQueryTextChange(newText: String) {
//        if (newText.isEmpty())
//            resetProductList()
//        else
//            filterProducts(newText)
        // Navigate to suggestion search fragment
        val navController = findNavController()
        if (navController.currentDestination?.id != com.example.votree.R.id.suggestionSearchFragment) {
            navController.navigate(com.example.votree.R.id.suggestionSearchFragment)
        }
    }

    override fun onQueryTextSubmit(query: String) {
//        filterProducts(query)

        // Navigate to product list fragment
        val navController = findNavController()
        if (navController.currentDestination?.id != com.example.votree.R.id.productList) {
            val action = ProductListDirections.actionProductListToSuggestionSearchFragment()
            navController.navigate(action)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity)
            context.setSearchQueryListener(this)
    }

    override fun onDetach() {
        super.onDetach()
        if (activity is MainActivity)
            (activity as MainActivity).setSearchQueryListener(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
