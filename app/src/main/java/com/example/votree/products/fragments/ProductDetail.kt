package com.example.votree.products.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.FragmentProductDetailBinding
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.products.view_models.CartViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ProductDetail : Fragment() {

    private val args by navArgs<ProductDetailArgs>()
    private lateinit var binding: FragmentProductDetailBinding
    private val cartViewModel = CartViewModel()
    private val firestore = FirebaseFirestore.getInstance()
    private val productRepository = ProductRepository(firestore)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false)

        setUpProductView(args)

        // Determine the user role and adjust UI elements
        val isSeller = args.role == "store"
        updateUIForUserRole(isSeller)


        return binding.root
    }

    private fun updateUIForUserRole(isSeller: Boolean) {
        if (isSeller) {
            binding.chattingBtn.visibility = View.GONE
            binding.addToCartBtn.visibility = View.GONE

            binding.updateBtn.visibility = View.VISIBLE
            binding.updateBtn.setOnClickListener {
                // Navigate to UpdateProductFragment
                val action =
                    ProductDetailDirections.actionProductDetail2ToUpdateProduct(args.currentProduct)
                findNavController().navigate(action)
            }

            binding.deleteBtn.visibility = View.VISIBLE
            binding.deleteBtn.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete this product?")
                    .setPositiveButton("Accept") { dialog, which ->
                        productRepository.deleteProduct(args.currentProduct) {
                            if (it) {
                                findNavController().popBackStack()
                            } else {
                                Log.e("ProductDetail", "Error deleting product")
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        } else {
            binding.addToCartBtn.setOnClickListener {
                cartViewModel.addProductToCart(args.currentProduct.id, 1)
            }
        }
        binding.buyNowBtn.visibility = if (!isSeller) View.VISIBLE else View.GONE
    }

    private fun setUpProductView(args: ProductDetailArgs) {
        args.currentProduct.let { product ->
            binding.productPrice.text = context?.getString(R.string.price_format, product.price)
            binding.description.text = product.description
            binding.productType.text = product.type.toString()
            binding.suitEnvironment.text = product.suitEnvironment.toString()
            binding.suitClimate.text = product.suitClimate.toString()
            binding.productRating.text = product.averageRate.toString()
            binding.productSoldQuantity.text = product.quantitySold.toString()
        }

        try {
            Glide.with(this)
                .load(args.currentProduct.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.img_placeholder)
                .into(binding.productImage)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("ProductDetail", "Error loading image")
        }
    }
}