package com.example.votree.products.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.CartAdapterBinding
import com.example.votree.databinding.GroupShopItemsBinding
import com.example.votree.products.models.ProductItem
import com.example.votree.products.view_models.CartViewModel

class ProductGroupAdapter(
    var items: List<ProductItem>,
    private val cartViewModel: CartViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_PRODUCT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ProductItem.ProductHeader -> TYPE_HEADER
            is ProductItem.ProductData -> TYPE_PRODUCT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = GroupShopItemsBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }

            else -> {
                val binding = CartAdapterBinding.inflate(inflater, parent, false)
                ProductViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ProductItem.ProductHeader -> (holder as HeaderViewHolder).bind(item)
            is ProductItem.ProductData -> (holder as ProductViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ProductItem>) {
        val diffResult = DiffUtil.calculateDiff(ProductDiffCallback(items, newItems))
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    class HeaderViewHolder(private val binding: GroupShopItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductItem.ProductHeader) {
            binding.shopNameTv.text = item.shopName
        }
    }

    inner class ProductViewHolder(private val binding: CartAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductItem.ProductData) {
            binding.productNameTv.text = item.product.productName
            binding.productPriceTv.text =
                binding.root.context.getString(R.string.price_format, item.product.price)
            binding.quantityEt.setText(item.quantity.toString())
            Glide.with(binding.root)
                .load(item.product.imageUrl)
                .placeholder(R.drawable.img_placeholder)
                .into(binding.productImageIv)

            // Set click listener for remove button
            binding.removeBtn.setOnClickListener {
                cartViewModel.removeCartItem(item.product.id)
            }

            // Set click listener for add button
            binding.addBtn.setOnClickListener {
                cartViewModel.updateCartItem(item.product.id, 1)
                binding.quantityEt.setText((item.quantity + 1).toString())
            }

            // Set click listener for subtract button
            binding.subBtn.setOnClickListener {
                cartViewModel.updateCartItem(item.product.id, -1)
                binding.quantityEt.setText((item.quantity - 1).toString())
            }
        }
    }

    class ProductDiffCallback(
        private val oldList: List<ProductItem>,
        private val newList: List<ProductItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//            return oldList[oldItemPosition].id == newList[newItemPosition].id
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

