package com.example.votree.products.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.products.models.Product

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(), Filterable {

    interface OnProductClickListener {
        fun onProductClick(product: Product)
    }

    private var productList = emptyList<Product>()
    private var filteredProductList = productList
    private var listener: OnProductClickListener? = null

    fun setOnProductClickListener(listener: OnProductClickListener) {
        this.listener = listener
    }

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

    override fun getItemCount(): Int = filteredProductList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()
                filteredProductList = if (charString.isEmpty()) {
                    productList
                } else {
                    val filteredList = ArrayList<Product>()
                    for (product in productList) {
                        if (product.productName.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(product)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredProductList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredProductList = results?.values as List<Product>
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
//        val currentItem = productList[position]
        val currentItem = filteredProductList[position]
        holder.itemView.apply {
            holder.productName.text = currentItem.productName
            holder.productPrice.text = currentItem.price.toString()
            holder.shortDescription.text = currentItem.shortDescription
            holder.averageRate.text = currentItem.averageRate.toString()
            holder.quantityOfSold.text = currentItem.quantitySold.toString()
            Glide.with(this)
                .load(currentItem.imageUrl)
                .placeholder(R.drawable.img_placeholder)
//                .error(R.drawable.error_image)
                .into(holder.productImage)
        }

        holder.productListLayout.setOnClickListener {
            listener?.onProductClick(currentItem)
        }
    }

    fun setData(product: List<Product>) {
        this.productList = product
        this.filteredProductList = product
        notifyDataSetChanged()
    }
}