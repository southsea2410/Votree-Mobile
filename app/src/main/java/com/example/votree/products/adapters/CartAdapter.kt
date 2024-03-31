package com.example.votree.products.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.products.models.Cart
import com.example.votree.products.view_models.ProductViewModel

class CartAdapter(
    private val context: Context,
    private val cart: Cart,
    private val productViewModel: ProductViewModel
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cart_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productId = cart.productsMap.keys.toList()[position]
        val quantity = cart.productsMap.values.toList()[position]
        holder.bind(productId, quantity)
    }

    override fun getItemCount(): Int {
        return cart.productsMap.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productNameTextView: TextView = itemView.findViewById(R.id.productName_tv)
        private val productPriceTextView: TextView = itemView.findViewById(R.id.productPrice_tv)
        private val productQuantityTextView: TextView = itemView.findViewById(R.id.quantity_et)
        private val productImageView: ImageView = itemView.findViewById(R.id.productImage_iv)
        private val removeButton: Button = itemView.findViewById(R.id.remove_btn)

        fun bind(productId: String, quantity: Int) {
            // Fetch product information based on product ID
            productViewModel.getProductById(productId).observe(
                itemView.context as androidx.lifecycle.LifecycleOwner,
                Observer { product ->
                    // Bind product data to views
                    productNameTextView.text = product?.productName ?: ""
                    productPriceTextView.text =
                        product?.let { context.getString(R.string.price_format, it.price) } ?: ""
                    productQuantityTextView.text = quantity.toString()
                    Glide.with(itemView)
                        .load(product?.imageUrl)
                        .into(productImageView)
                })

            // Set click listener for remove button
            removeButton.setOnClickListener {
                // Notify listener about item removal
                onRemoveClickListener?.invoke(productId)
            }
        }
    }

    // Callback for remove button click listener
    private var onRemoveClickListener: ((String) -> Unit)? = null

    // Function to set remove button click listener
    fun setOnRemoveClickListener(listener: (String) -> Unit) {
        this.onRemoveClickListener = listener
    }
}
