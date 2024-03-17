package com.example.votree.fragment.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.votree.R

class ProductDetail : Fragment() {

    private val args by navArgs<ProductDetailArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_product_detail, container, false)

        view.findViewById<TextView>(R.id.productPrice).text = args.currentProduct.price.toString()
        view.findViewById<TextView>(R.id.description).text = args.currentProduct.description
        view.findViewById<TextView>(R.id.productType).text = args.currentProduct.type.toString()
        view.findViewById<TextView>(R.id.suitEnvironment).text =
            args.currentProduct.suitEnvironment.toString()
        view.findViewById<TextView>(R.id.suitClimate).text =
            args.currentProduct.suitClimate.toString()
        view.findViewById<TextView>(R.id.productRating).text =
            args.currentProduct.averageRate.toString()
        view.findViewById<TextView>(R.id.productSoldQuantity).text =
            args.currentProduct.quantitySold.toString()

        return view
    }
}