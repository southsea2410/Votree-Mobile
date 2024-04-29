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
import com.example.votree.products.models.Cart
import com.example.votree.products.models.ProductReview
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.products.view_models.CartViewModel
import com.example.votree.users.repositories.StoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        displayShopDetails()
    }

    private fun displayProductDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            val productRepository = ProductRepository(firestore)
            try {
                val productRating =
                    productRepository.getAverageProductRating(args.currentProduct.id)
                withContext(Dispatchers.Main) {
                    binding.productRatingRb.rating = productRating
                    binding.productRating.text = productRating.toString()
                }
            } catch (e: Exception) {
                Log.e("ProductDetailFragment", "Error fetching product details", e)

            }
        }

        with(binding) {
            args.currentProduct.let { product ->
                productName.text = product.productName
                productPrice.text = getString(R.string.price_format, product.price)
                description.text = product.description
                productType.text = product.type.toString()
                suitEnvironment.text = product.suitEnvironment.toString()
                suitClimate.text = product.suitClimate.toString()
                productSoldQuantity.text = product.quantitySold.toString()

                Glide.with(this@ProductDetailFragment)
                    .load(product.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .into(productImage)
            }
        }
    }

    private fun displayShopDetails() {
        args.currentProduct.storeId?.let { storeId ->
            val storeRepository = StoreRepository()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val store = storeRepository.fetchStore(storeId)
                    val numberOfProducts = storeRepository.getNumberOfProductsOfStore(storeId)
                    val averageRating = storeRepository.getAverageProductRating(storeId)

                    // Update the UI on the main thread
                    withContext(Dispatchers.Main) {
                        binding.storeName.text = store.storeName
                        binding.storeSoldProductsTv.text = "$numberOfProducts"
                        binding.storeRatingTv.text = averageRating.toString()
                    }
                } catch (e: Exception) {
                    Log.e("ProductDetailFragment", "Error fetching store details", e)
                    // Handle errors, possibly update the UI to show an error message
                }
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
        val cartWithSelectedProduct = Cart(
            id = "",
            userId = "",
            productsMap = mutableMapOf(args.currentProduct.id to 1),
            totalPrice = 0.0
        )

        val action =
            ProductDetailFragmentDirections.actionProductDetailToCheckout(cartWithSelectedProduct)
        findNavController().navigate(action)
    }
}