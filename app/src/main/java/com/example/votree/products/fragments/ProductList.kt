package com.example.votree.products.fragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.GridLayoutManager
//import com.example.votree.R
//import com.example.votree.databinding.FragmentProductListBinding
//import com.example.votree.products.adapters.ProductAdapter
//import com.example.votree.products.data.product.ProductViewModel
//
//class ProductList : Fragment() {
//    private var _binding: FragmentProductListBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var mProductViewModel: ProductViewModel
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentProductListBinding.inflate(inflater, container, false)
//
//        val adapter = ProductAdapter()
//        val productRecyclerView = binding.productListRv
//        productRecyclerView.adapter = adapter
//        productRecyclerView.setHasFixedSize(true)
//
//        val numberOfColumns = 2;
//        productRecyclerView.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)
//
//        mProductViewModel = ProductViewModel(requireActivity().application)
//        mProductViewModel.readAllData.observe(viewLifecycleOwner) { product ->
//            adapter.setData(product)
//        }
//
//        binding.addProductFab.setOnClickListener {
//            findNavController().navigate(R.id.action_productList_to_addNewProduct)
//        }
//        return binding.root
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        _binding = null
//    }
//}
//

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.votree.R
import com.example.votree.databinding.FragmentProductListBinding
import com.example.votree.products.adapters.ProductAdapter
import com.example.votree.products.view_models.ProductViewModel

class ProductList : Fragment() {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mFirebaseProductViewModel: ProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)

        val adapter = ProductAdapter()
        val productRecyclerView = binding.productListRv
        productRecyclerView.adapter = adapter
        productRecyclerView.setHasFixedSize(true)

        val numberOfColumns = 2
        productRecyclerView.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)

        mFirebaseProductViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        mFirebaseProductViewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.setData(products)
        }

        binding.addProductFab.setOnClickListener {
            findNavController().navigate(R.id.action_productList_to_addNewProduct)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
