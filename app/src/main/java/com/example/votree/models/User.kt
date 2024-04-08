package com.example.votree.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User (
    @SerialName("id")
    var id: String,
    var userName: String,
    var fullName: String,
    var password: String,
    var phoneNumber: String,
    var address: String,
    var email: String,
    var avatar: List<String>,
    var active: Boolean,
    var role: String,
    var storeId: String,
    var expirePremium: String,
    var accumulatePoint: Int,
    var totalRevenue: Int,
    var transactionIdList: List<String>,
    var createdAt: String,
    var updatedAt: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readBoolean(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {}

    constructor() : this("", "", "", "", "", "", "", emptyList(), false, "", "", "", 0, 0, emptyList(), "", "")

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(userName)
        dest.writeString(fullName)
        dest.writeString(password)
        dest.writeString(phoneNumber)
        dest.writeString(address)
        dest.writeString(email)
        dest.writeStringList(avatar)
        dest.writeBoolean(active)
        dest.writeString(role)
        dest.writeString(storeId)
        dest.writeString(expirePremium)
        dest.writeInt(accumulatePoint)
        dest.writeInt(totalRevenue)
        dest.writeStringList(transactionIdList)
        dest.writeString(createdAt)
        dest.writeString(updatedAt)
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }

    }
}