package com.example.votree.products.adapters

import android.annotation.SuppressLint
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

    inner class HeaderViewHolder(private val binding: GroupShopItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductItem.ProductHeader) {
            binding.shopNameTv.text = item.shopName
            binding.shopGroupRb.isChecked = item.isChecked
            binding.shopGroupRb.setOnClickListener {
                item.isChecked = !item.isChecked
                binding.shopGroupRb.isChecked = item.isChecked
                checkAllProductsUnderShop(item.storeId, item.isChecked)
            }
        }

        // Implement a function that if the user clicks the checkbox, all the products under that shop will be checked
        // If the user unchecks the checkbox, all the products under that shop will be unchecked
        @SuppressLint("NotifyDataSetChanged")
        private fun checkAllProductsUnderShop(shopId: String, isChecked: Boolean) {
            items.forEach {
                if (it is ProductItem.ProductData && it.product.storeId == shopId) {
                    it.isChecked = isChecked
                }
            }
            notifyDataSetChanged()
        }
    }

    inner class ProductViewHolder(private val binding: CartAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductItem.ProductData) {
            binding.pickUpRb.isChecked = item.isChecked
            binding.productNameTv.text = item.product.productName
            binding.productPriceTv.text =
                binding.root.context.getString(R.string.price_format, item.product.price)
            binding.quantityEt.setText(item.quantity.toString())
            Glide.with(binding.root)
                .load(item.product.imageUrl)
                .placeholder(R.drawable.img_placeholder)
                .into(binding.productImageIv)

            binding.removeBtn.setOnClickListener {
                cartViewModel.removeCartItem(item.product.id)
            }

            binding.addBtn.setOnClickListener {
                cartViewModel.updateCartItem(item.product.id, 1)
                binding.quantityEt.setText((item.quantity + 1).toString())
            }

            binding.subBtn.setOnClickListener {
                cartViewModel.updateCartItem(item.product.id, -1)
                binding.quantityEt.setText((item.quantity - 1).toString())
            }

            binding.pickUpRb.setOnClickListener {
                item.isChecked = !item.isChecked
                binding.pickUpRb.isChecked = item.isChecked
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

