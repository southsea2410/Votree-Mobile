package com.example.votree.notifications.view_holders

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.databinding.NotificationAdapterBinding
import com.example.votree.notifications.adapters.NotificationAdapter
import com.example.votree.notifications.models.Notification

class NotificationViewHolder(val binding: NotificationAdapterBinding, val listener: NotificationAdapter.OnNotificationClickListener) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(notification: Notification) {
        binding.notificationTitleTv.text = notification.title
        binding.notificationContentTv.text = notification.content

        // Change background color based on read status
        if (notification.isRead) {
            binding.notificationLayout.setBackgroundColor(Color.parseColor("#DEE5D8"))
        }
        else {
            binding.notificationLayout.setBackgroundColor(Color.parseColor("#F7FBF1"))
        }

        binding.root.setOnClickListener {
            listener.onNotificationClick(notification)
            // Update the UI
            binding.notificationLayout.setBackgroundColor(Color.parseColor("#DEE5D8"))
        }
    }
}