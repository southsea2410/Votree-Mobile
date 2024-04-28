package com.example.votree.notifications.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Notification(
    var id: String = "",
    val title: String,
    val content: String,
    var imageUrl: String = "",
    var isRead: Boolean = false,
    val createdAt: Date = Date()
) : Parcelable