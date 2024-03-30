package com.example.votree.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Tip (
    @SerialName("id")
    var id: String,
    var userId: String,
    var title: String,
    var shortDescription: String,
    var content: String,
    var imageList: List<String>,
    var vote: Int,
    var approvalStatus: Int,
    var createdAt: String,
    var updatedAt: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    ) {}

    constructor() : this("", "", "", "", "", emptyList(), 0, 0, "", "")

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(userId)
        dest.writeString(title)
        dest.writeString(shortDescription)
        dest.writeString(content)
        dest.writeStringList(imageList)
        dest.writeInt(vote)
        dest.writeInt(approvalStatus)
        dest.writeString(createdAt)
        dest.writeString(updatedAt)
    }

    companion object CREATOR : Parcelable.Creator<Tip> {
        override fun createFromParcel(parcel: Parcel): Tip {
            return Tip(parcel)
        }

        override fun newArray(size: Int): Array<Tip?> {
            return arrayOfNulls(size)
        }

    }
}