package com.example.votree.fragment.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.votree.R
import com.example.votree.data.product.ProductViewModel
import com.example.votree.databinding.FragmentProductListBinding

class ProductList : Fragment() {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mProductViewModel: ProductViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)

        val adapter = ProductAdapter()
        val productRecyclerView = binding.productListRv
        productRecyclerView.adapter = adapter
        productRecyclerView.setHasFixedSize(true)

        val numberOfColumns = 2;
        productRecyclerView.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)

        mProductViewModel = ProductViewModel(requireActivity().application)
        mProductViewModel.readAllData.observe(viewLifecycleOwner) { product ->
            adapter.setData(product)
        }

        binding.addProductFab.setOnClickListener {
            findNavController().navigate(R.id.action_productList_to_addNewProduct)
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

