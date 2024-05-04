package com.example.votree.notifications.view_holders

import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.NotificationAdapterBinding
import com.example.votree.notifications.adapters.NotificationAdapter
import com.example.votree.notifications.models.Notification
import com.example.votree.notifications.view_models.NotificationViewModel
import com.example.votree.products.models.Product
import com.example.votree.products.models.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//class NotificationViewHolder(
//    private val binding: NotificationAdapterBinding,
//    private val listener: NotificationAdapter.OnNotificationClickListener,
//    private val transactionRepository: TransactionRepository,
//    private val productRepository: ProductRepository,
//    private val viewLifecycleOwner: LifecycleOwner
//) : RecyclerView.ViewHolder(binding.root) {
//
//    private val loadedProducts = mutableMapOf<String, Product>()
//
//    fun bind(notification: Notification) {
//        binding.notificationTitleTv.text = notification.title
//        binding.notificationContentTv.text = notification.content
//
//        // Format the notification time
//        val formattedTime = formatNotificationTime(notification.createdAt)
//        binding.notificationTimeTv.text = formattedTime
//
//        updateNotificationBackground(notification)
//
//        if (notification.orderId.isNotEmpty()) {
//            fetchTransactionInfo(notification.orderId) {
//            }
//        }
//
//        binding.root.setOnClickListener {
//            listener.onNotificationClick(notification)
//            markNotificationAsRead(notification)
//            updateNotificationBackground(notification)
//
//            if (notification.orderId.isNotEmpty()) {
//            }
//        }
//
////        loadProductImage(notification)
//    }
//
//    private fun updateNotificationBackground(notification: Notification) {
//        if (notification.read) {
//            binding.notificationLayout.setBackgroundColor(Color.parseColor("#DEE5D8"))
//        } else {
//            binding.notificationLayout.setBackgroundColor(Color.parseColor("#F7FBF1"))
//        }
//    }
//
//    private fun markNotificationAsRead(notification: Notification) {
//        notification.read = true
//        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
//            val notificationViewModel = NotificationViewModel()
//            notificationViewModel.updateNotificationReadStatus(notification.id, true)
//        }
//    }
//
//    private fun loadProductImage(notification: Notification) {
//        if (notification.orderId.isNotEmpty()) {
//            fetchTransactionInfo(notification.orderId) { transaction ->
//                val firstProductId = transaction.productsMap.keys.firstOrNull()
//                if (firstProductId != null) {
//                    Log.d("NotificationViewHolder", "Loading product image for productId: $firstProductId")
//                    loadProductForId(firstProductId) { product ->
//                        Log.d("NotificationViewHolder", "Product image URL: ${product.imageUrl}")
//                        binding.notificationContentTv.text = product.productName
////                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
////                            Glide.with(binding.root)
////                                .load(product.imageUrl)
////                                .error(R.drawable.img_placeholder)
////                                .into(binding.notificationImageIv)
////                        }
//                        CoroutineScope(Dispatchers.IO).launch {
//                            val product = productRepository.getProduct(firstProductId)
//                            loadedProducts[firstProductId] = product
//                            Log.d("NotificationViewHolder", "Product image URL: ${product.imageUrl}")
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun loadProductForId(productId: String, onProductFetched: (Product) -> Unit) {
//        if (loadedProducts.containsKey(productId)) {
//            onProductFetched(loadedProducts[productId]!!)
//        } else {
//            fetchProductInfo(productId) { product ->
//                loadedProducts[productId] = product
//                onProductFetched(product)
//            }
//        }
//    }
//
//    private fun fetchProductInfo(productId: String, onProductFetched: (Product) -> Unit) {
//        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                val product = productRepository.getProduct(productId)
//                onProductFetched(product)
//            } catch (e: Exception) {
//                // Handle the error
//            }
//        }
//    }
//
//    private fun fetchTransactionInfo(orderId: String, onTransactionFetched: (Transaction) -> Unit) {
//        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                val transaction = transactionRepository.getTransaction(orderId)
//                onTransactionFetched(transaction)
//            } catch (e: Exception) {
//            }
//        }
//    }
//
//    private fun formatNotificationTime(createdAt: Date): String {
//        val formatter = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
//        return formatter.format(createdAt)
//    }
//}

class NotificationViewHolder(
    private val binding: NotificationAdapterBinding,
    private val listener: NotificationAdapter.OnNotificationClickListener,
    private val notificationViewModel: NotificationViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(notification: Notification) {
        binding.notificationTitleTv.text = notification.title
        binding.notificationContentTv.text = notification.content

        // Format the notification time
        val formattedTime = formatNotificationTime(notification.createdAt)
        binding.notificationTimeTv.text = formattedTime

        updateNotificationBackground(notification)

        if (notification.orderId.isNotEmpty()) {
            notificationViewModel.fetchTransactionInfo(notification.orderId)
            notificationViewModel.fetchProductInfo(notification)
        }

        binding.root.setOnClickListener {
            listener.onNotificationClick(notification)
            markNotificationAsRead(notification)
            updateNotificationBackground(notification)

        }

        notificationViewModel.transactionData.observe(viewLifecycleOwner) { transaction ->
            updateTransactionInfo(transaction)
        }

        notificationViewModel.productData.observe(viewLifecycleOwner) { product ->
            updateProductInfo(product)
        }
    }

    private fun updateTransactionInfo(transaction: Transaction?) {

         binding.notificationContentTv.text = transaction?.name
    }

    private fun updateProductInfo(product: Product?) {
         binding.notificationTitleTv.text = "New Order: ${product?.productName}"
         Glide.with(binding.root)
             .load(product?.imageUrl)
             .error(R.drawable.img_placeholder)
             .into(binding.notificationImageIv)
    }

    private fun updateNotificationBackground(notification: Notification) {
        if (notification.read) {
            binding.notificationLayout.setBackgroundColor(Color.parseColor("#DEE5D8"))
        } else {
            binding.notificationLayout.setBackgroundColor(Color.parseColor("#F7FBF1"))
        }
    }

    private fun markNotificationAsRead(notification: Notification) {
        notification.read = true
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            notificationViewModel.updateNotificationReadStatus(notification.id, true)
        }
    }

    private fun formatNotificationTime(createdAt: Date): String {
        val formatter = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
        return formatter.format(createdAt)
    }
}


