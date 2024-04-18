package com.example.votree.products.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.FragmentProductDetailBinding
import com.example.votree.products.adapters.UserReviewAdapter
import com.example.votree.products.models.ProductReview
import com.example.votree.products.view_models.CartViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ProductDetailFragment : Fragment() {

    private lateinit var binding: FragmentProductDetailBinding
    private val args: ProductDetailFragmentArgs by navArgs()
    private val cartViewModel = CartViewModel()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var userReviewAdapter: UserReviewAdapter
    private lateinit var reviewRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        displayProductDetails()
        setupButtons()
        setupReviewAdapter()
        fetchAndDisplayReviews()
    }

    private fun displayProductDetails() {
        with(binding) {
            args.currentProduct.let { product ->
                productPrice.text = getString(R.string.price_format, product.price)
                description.text = product.description
                productType.text = product.type.toString()
                suitEnvironment.text = product.suitEnvironment.toString()
                suitClimate.text = product.suitClimate.toString()
                productRating.text = product.averageRate.toString()
                productSoldQuantity.text = product.quantitySold.toString()

                Glide.with(this@ProductDetailFragment)
                    .load(product.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .into(productImage)
            }
        }
    }

    private fun setupButtons() {
        with(binding) {
            buyNowBtn.setOnClickListener {
                gotoCheckout()
            }
            addToCartBtn.setOnClickListener {
                cartViewModel.addProductToCart(args.currentProduct.id, 1)
            }
        }
    }

    private fun setupReviewAdapter() {
        reviewRecyclerView = binding.reviewRecyclerView
        reviewRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun fetchAndDisplayReviews() {
        val reviews = mutableListOf<ProductReview>()
        firestore.collection("products").document(args.currentProduct.id).collection("reviews")
            .get()
            .addOnSuccessListener { reviewsSnapshot ->
                for (reviewDocument in reviewsSnapshot.documents) {
                    val review = reviewDocument.toObject(ProductReview::class.java)
                    review?.let { reviews.add(it) }
                }
                userReviewAdapter = UserReviewAdapter(reviews, CoroutineScope(Dispatchers.Main))
                reviewRecyclerView.adapter = userReviewAdapter
            }
            .addOnFailureListener { e ->
                Log.e("ProductDetail", "Error fetching reviews", e)
            }
    }

    private fun gotoCheckout() {
        val action =
            ProductDetailFragmentDirections.actionProductDetail2ToCheckout(args.currentProduct)
        findNavController().navigate(action)
    }
}