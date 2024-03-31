package com.example.votree.products.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.products.view_models.CartViewModel

class ProductDetail : Fragment() {

    private val args by navArgs<ProductDetailArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_product_detail, container, false)

        args.currentProduct.let { product ->
            view.findViewById<TextView>(R.id.productPrice)?.text = product.price.toString()
            view.findViewById<TextView>(R.id.description)?.text = product.description
            view.findViewById<TextView>(R.id.productType)?.text = product.type.toString()
            view.findViewById<TextView>(R.id.suitEnvironment)?.text =
                product.suitEnvironment.toString()
            view.findViewById<TextView>(R.id.suitClimate)?.text = product.suitClimate.toString()
            view.findViewById<TextView>(R.id.productRating)?.text =
                product.averageRate.toString()
            view.findViewById<TextView>(R.id.productSoldQuantity)?.text =
                product.quantitySold.toString()
        }

        try {
            Glide.with(this)
                .load(args.currentProduct.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.img_placeholder)
                .into(view.findViewById(R.id.productImage))

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("ProductDetail", "Error loading image")
        }

        var cartViewModel = CartViewModel()

        val addToCartButton = view.findViewById<TextView>(R.id.addToCart_btn)
        addToCartButton.setOnClickListener {
            // Add product to cart
            cartViewModel.addProductToCart(args.currentProduct.id, 1)
        }
        return view
    }

}
