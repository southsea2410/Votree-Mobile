package com.example.votree.users.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.votree.MainActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        println("From: ${remoteMessage.from}")

        if (remoteMessage.notification != null) {
            println("Message Notification Body: ${remoteMessage.notification!!.body}")
        }

        sendNotification(remoteMessage.from ?: "", remoteMessage.notification?.body ?: "")
    }

    private fun sendNotification(from: String, body: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this@MyFirebaseMessagingService, "$from -> $body", Toast.LENGTH_SHORT)
                .show()
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "My channel ID"
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("My new notification")
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Send the token to  server or store it in Firestore
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val deviceToken = hashMapOf(
            "token" to token,
            "timestamp" to FieldValue.serverTimestamp()
        )

        // Get user ID from Firebase Auth
        val userId = Firebase.auth.currentUser?.uid ?: return

        Firebase.firestore.collection("fcmTokens").document(userId)
            .set(deviceToken)
            .addOnSuccessListener { Log.d(TAG, "Token successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing token", e) }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
