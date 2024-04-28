package com.example.votree.products.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.votree.MainActivity
import com.example.votree.R
import com.example.votree.databinding.FragmentProductListBinding
import com.example.votree.products.adapters.ProductAdapter
import com.example.votree.products.models.Product
import com.example.votree.products.view_models.ProductFilterViewModel
import com.example.votree.products.view_models.ProductViewModel
import com.google.android.material.tabs.TabLayout

class ProductList : Fragment(), MainActivity.SearchQueryListener {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mFirebaseProductViewModel: ProductViewModel
    private val FilterProductViewModel: ProductFilterViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ProductFilterViewModel::class.java)
    }
    private lateinit var productAdapter: ProductAdapter
    private var sortPriceAscending = true

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
        setUpTabLayout()
        navigateToProductDetail()
        setUpSearchQuery()
        setupFilterObserver()
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

    private fun setUpTabLayout()
    {
        binding.sortProductTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> mFirebaseProductViewModel.sortProductsBySoldQuantity()
                    1 -> mFirebaseProductViewModel.sortProductsByCreationDate()
                    2 -> {
                        mFirebaseProductViewModel.sortProductsByPrice(sortPriceAscending)
                        tab.icon = if (sortPriceAscending) {
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_ascending)
                        } else {
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_descending)
                        }
                    }
                    3 -> {
                        showProductFilterBottomSheet()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // If unseleted the 2nd tab, reset the sortPriceAscending to true
                if (tab?.position == 2) {
                    sortPriceAscending = true
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_unfold)
                }else if(tab?.position == 3){
                    resetProductList()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                if (tab?.position == 2) {
                    sortPriceAscending = !sortPriceAscending
                    mFirebaseProductViewModel.sortProductsByPrice(sortPriceAscending)
                    tab.icon = if (sortPriceAscending) {
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_ascending)
                    } else {
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_descending)
                    }
                }
                else if (tab?.position == 3) {
                    showProductFilterBottomSheet()
                }
            }
        })
    }

    private fun showProductFilterBottomSheet() {
        val bottomSheetFragment = ProductFilterBottomSheet()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
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
        // Navigate to suggestion search fragment
        val navController = findNavController()
        if (navController.currentDestination?.id != com.example.votree.R.id.suggestionSearchFragment) {
            navController.navigate(com.example.votree.R.id.suggestionSearchFragment)
        }
    }

    override fun onQueryTextSubmit(query: String) {
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

    private fun setupFilterObserver() {
        FilterProductViewModel.currentFilterCriteria.observe(viewLifecycleOwner) { criteria ->
            applyFiltersToProductList(criteria)
        }
    }

    private fun applyFiltersToProductList(criteria: ProductFilterViewModel.FilterCriteria) {
        // Log the criteria to check if the filter is working correctly
        Log.d("ProductList", "Filter criteria: $criteria")
        mFirebaseProductViewModel.products.observe(viewLifecycleOwner) { products ->
            Log.d("ProductList", "All products: $products")

            // Filter products based on any matching criteria within each category
            val filteredProducts = products.filter { product ->
                val matchesPlantType = criteria.selectedPlantTypes.isEmpty() || criteria.selectedPlantTypes.contains(product.type)
                val matchesClimate = criteria.selectedClimates.isEmpty() || criteria.selectedClimates.contains(product.suitClimate)
                val matchesEnvironment = criteria.selectedEnvironments.isEmpty() || criteria.selectedEnvironments.contains(product.suitEnvironment)

                matchesPlantType && matchesClimate && matchesEnvironment
            }

            Log.d("ProductList", "Filtered products: $filteredProducts")
            productAdapter.setData(filteredProducts)
        }
    }

//    private fun setupPriceRangeSlider() {
//        priceRangeSlider =
//        priceRangeSlider.addOnChangeListener { slider, _, _ ->
//            val values = slider.values
//            viewModel.setPriceRange(values[0], values[1])
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
