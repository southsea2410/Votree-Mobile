package com.example.votree.products.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.products.adapters.UserReviewAdapter
import com.example.votree.products.models.ProductReview
import com.example.votree.products.view_models.CartViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ProductDetail : Fragment() {

    private val args by navArgs<ProductDetailArgs>()
    private lateinit var cartViewModel: CartViewModel
    private lateinit var userReviewAdapter: UserReviewAdapter
    private lateinit var reviewRecyclerView: RecyclerView
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)
        initializeViewModel()
        displayProductDetails(view)
        setupAddToCartButton(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the RecyclerView
        reviewRecyclerView = view.findViewById(R.id.reviewRecyclerView)
        reviewRecyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch and display reviews for the current product
        fetchAndDisplayReviews()
    }

    private fun fetchAndDisplayReviews() {
        val reviews = mutableListOf<ProductReview>()
        // Go to the products/productId/reviews collection in Firestore
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


    private fun initializeViewModel() {
        cartViewModel = CartViewModel()
    }

    private fun displayProductDetails(view: View) {
        args.currentProduct.let { product ->
            view.findViewById<TextView>(R.id.productPrice)?.text = product.price.toString()
            view.findViewById<TextView>(R.id.description)?.text = product.description
            view.findViewById<TextView>(R.id.productType)?.text = product.type.toString()
            view.findViewById<TextView>(R.id.suitEnvironment)?.text = product.suitEnvironment.toString()
            view.findViewById<TextView>(R.id.suitClimate)?.text = product.suitClimate.toString()
            view.findViewById<TextView>(R.id.productRating)?.text = product.averageRate.toString()
            view.findViewById<TextView>(R.id.productSoldQuantity)?.text = product.quantitySold.toString()
            loadImage(view, product.imageUrl)
        }
    }

    private fun loadImage(view: View, imageUrl: String?) {
        try {
            Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.img_placeholder)
                .into(view.findViewById(R.id.productImage))
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("ProductDetail", "Error loading image")
        }
    }

    private fun setupAddToCartButton(view: View) {
        val addToCartButton = view.findViewById<TextView>(R.id.addToCart_btn)
        addToCartButton.setOnClickListener {
            cartViewModel.addProductToCart(args.currentProduct.id, 1)
        }
    }
}
