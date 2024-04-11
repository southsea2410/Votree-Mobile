package com.example.votree.products.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.products.fragments.ProductListDirections
import com.example.votree.products.models.Product

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var productList = emptyList<Product>()

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var productName = itemView.findViewById<TextView>(R.id.productName_tv)
        var productPrice = itemView.findViewById<TextView>(R.id.price_tv)
        var shortDescription = itemView.findViewById<TextView>(R.id.shortDescription_tv)
        var averageRate = itemView.findViewById<TextView>(R.id.productRate_tv)
        var quantityOfSold = itemView.findViewById<TextView>(R.id.productSold_tv)
        var productListLayout = itemView.findViewById<View>(R.id.productListLayout)
        val productImage = itemView.findViewById<ImageView>(R.id.productImage_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.product_card, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentItem = productList[position]
        holder.itemView.apply {
            holder.productName.text = currentItem.productName
            holder.productPrice.text = currentItem.price.toString()
            holder.shortDescription.text = currentItem.shortDescription
            holder.averageRate.text = currentItem.averageRate.toString()
            holder.quantityOfSold.text = currentItem.quantitySold.toString()
            Glide.with(this)
                .load(currentItem.imageUrl)
//                .placeholder(R.drawable.placeholder_image)
//                .error(R.drawable.error_image)
                .into(holder.productImage)
        }

        holder.productListLayout.setOnClickListener {
            val action = ProductListDirections.actionProductListToProductDetail(currentItem)
            holder.itemView.findNavController().navigate(action)
        }
    }

    fun setData(product: List<Product>) {
        this.productList = product
        notifyDataSetChanged()
    }
}