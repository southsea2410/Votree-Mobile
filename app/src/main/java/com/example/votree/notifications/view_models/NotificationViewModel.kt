package com.example.votree.notifications.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votree.notifications.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        Log.d("NotificationViewModel", "Fetching notifications")
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users/$userId/notifications")
//                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                val notificationsList = snapshot.toObjects(Notification::class.java)
                Log.d("NotificationViewModel", "Notifications: $notificationsList")
                _notifications.postValue(notificationsList)
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error fetching notifications", e)
                // Handle exceptions
            }
        }
    }

    fun saveNotification(notification: Notification) {
        viewModelScope.launch {
            val notificationMap = hashMapOf(
                "title" to notification.title,
                "content" to notification.content,
                "isRead" to notification.isRead,
                "createdAt" to com.google.firebase.Timestamp(notification.createdAt),
                "orderId" to notification.orderId  // Include orderId in the map
            )

            try {
                val documentReference = db.collection("users/$userId/notifications").add(notificationMap).await()
                val notificationId = documentReference.id
                db.collection("users/$userId/notifications").document(notificationId)
                    .update("id", notificationId).await()
            } catch (e: Exception) {
                Log.d("NotificationViewModel", "Error saving notification", e)
                // Handle exceptions
            }
        }
    }

    fun removeReadNotifications() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users/$userId/notifications")
                    .whereEqualTo("isRead", true)
                    .get()
                    .await()
                for (document in snapshot.documents) {
                    db.collection("users/$userId/notifications").document(document.id).delete().await()
                }
            } catch (e: Exception) {
                Log.d("NotificationViewModel", "Error removing read notifications", e)
                // Handle exceptions
            }
        }
    }

    fun updateNotificationReadStatus(notificationId: String, isRead: Boolean) {
        viewModelScope.launch {
            try {
                db.collection("users/$userId/notifications").document(notificationId)
                    .update("isRead", isRead).await()
                // After updating, fetch notifications again or update the list locally
            } catch (e: Exception) {
                Log.d("NotificationViewModel", "Error updating notification read status", e)
            }
        }
    }
}