package com.example.votree.products.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.votree.databinding.FragmentProductListBinding
import com.example.votree.products.adapters.ProductAdapter
import com.example.votree.products.models.Product
import com.example.votree.products.view_models.ProductViewModel

class ProductList : Fragment() {
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

        productAdapter = ProductAdapter()
        binding.productListRv.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
        }

        mFirebaseProductViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        mFirebaseProductViewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.setData(products)
        }

        productAdapter.setOnProductClickListener(object : ProductAdapter.OnProductClickListener {
            override fun onProductClick(product: Product) {
                val action = ProductListDirections.actionProductListToProductDetail(product)
                findNavController().navigate(action)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
