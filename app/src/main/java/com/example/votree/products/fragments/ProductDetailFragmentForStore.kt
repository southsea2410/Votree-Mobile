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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.FragmentProductDetailForStoreBinding
import com.example.votree.products.adapters.UserReviewAdapter
import com.example.votree.products.models.ProductReview
import com.example.votree.products.repositories.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ProductDetailFragmentForStore : Fragment() {

    private lateinit var binding: FragmentProductDetailForStoreBinding
    private val args: ProductDetailFragmentForStoreArgs by navArgs()
    private val firestore = FirebaseFirestore.getInstance()
    private val productRepository = ProductRepository(firestore)
    private lateinit var userReviewAdapter: UserReviewAdapter
    private lateinit var reviewRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailForStoreBinding.inflate(inflater, container, false)
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

                Glide.with(this@ProductDetailFragmentForStore)
                    .load(product.imageList[0])
                    .centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .into(productImage)
            }
        }
    }

    private fun setupButtons() {
        with(binding) {
            updateBtn.setOnClickListener { navigateToUpdateProduct() }
            deleteBtn.setOnClickListener { confirmProductDeletion() }
        }
    }

    private fun navigateToUpdateProduct() {
        val action =
            ProductDetailFragmentForStoreDirections.actionProductDetailFragmentForStoreToUpdateProduct(
                args.currentProduct
            )
        findNavController().navigate(action)
    }

    private fun confirmProductDeletion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("Accept") { _, _ ->
                productRepository.deleteProduct(args.currentProduct) { success ->
                    if (success) findNavController().popBackStack()
                    else Log.e("ProductDetail", "Error deleting product")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupReviewAdapter() {
        reviewRecyclerView = binding.reviewRecyclerView
        reviewRecyclerView.layoutManager = LinearLayoutManager(context)
    }


    private fun fetchAndDisplayReviews() {
        val reviews = mutableListOf<ProductReview>()
        // Go to the products/productId/reviews collection in Firestore
        firestore.collection("products").document(args.currentProduct.id).collection("reviews")
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(2)
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
}