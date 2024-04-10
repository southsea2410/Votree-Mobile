package com.example.votree.users.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class User(
    var id: String = "",
    val email: String = "",
    val username: String = "",
    val fullName: String = "",
    val password: String = "",
    val avatar: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val role: String = "",
    val storeId: String = "",
    var expirePremium: Date = Date(),
    var accumulatePoint: Int = 0,
    var totalRevenue: Double = 0.0,
    val transactionIdList: MutableList<String> = mutableListOf(),
    var active: Boolean = true,
    val createdAt: Date = Date(),
    var updatedAt: Date = Date()
) : Parcelable {
    override fun toString(): String {
        return "User(id=$id, email='$email', name='${fullName}')"
    }
}