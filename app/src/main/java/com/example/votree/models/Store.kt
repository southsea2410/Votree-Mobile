package com.example.votree.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Store (
    @SerialName("id")
    var id: String,
    var storeName: String,
    var storeLocation: String,
    var storeEmail: String,
    var storePhoneNumber: String,
    var discountCodeIdList: List<String>,
    var transactionIdList: List<String>,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.createStringArrayList()!!
    ) {}

    constructor() : this("", "", "", "", "", emptyList(), emptyList())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(storeName)
        dest.writeString(storeLocation)
        dest.writeString(storeEmail)
        dest.writeString(storePhoneNumber)
        dest.writeStringList(discountCodeIdList)
        dest.writeStringList(transactionIdList)
    }

    companion object CREATOR : Parcelable.Creator<Store> {
        override fun createFromParcel(parcel: Parcel): Store {
            return Store(parcel)
        }

        override fun newArray(size: Int): Array<Store?> {
            return arrayOfNulls(size)
        }
    }
}