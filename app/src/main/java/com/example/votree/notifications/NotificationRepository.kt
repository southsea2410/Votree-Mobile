package com.example.votree.notifications

import com.example.votree.notifications.models.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NotificationRepository(private val firestore: FirebaseFirestore) {
    suspend fun getNotification(notificationId: String): Notification? {
        return withContext(Dispatchers.IO) {
            val notificationDoc = firestore.collection("notifications")
                .document(notificationId)
                .get()
                .await()

            notificationDoc.toObject(Notification::class.java)
        }
    }

    suspend fun updateNotification(notification: Notification) {
        withContext(Dispatchers.IO) {
            firestore.collection("notifications")
                .document(notification.id)
                .set(notification)
                .await()
        }
    }
}
