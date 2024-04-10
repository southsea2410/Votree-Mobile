package com.example.votree.tips.models

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class ProductTip(
    var approvalStatus: Int = 0,
    var content: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null,
    var imageList: MutableList<String> = mutableListOf(""),
    var shortDescription: String = "",
    var title: String = "",
    var userId: String = "",
    var vote: Int = 0,
    var id: String = "",
) : Parcelable {
    override fun toString(): String {
        return "ProductTip(approvalStatus=$approvalStatus, content='$content', createdAt=$createdAt, updatedAt=$updatedAt, imageList=$imageList, shortDescription='$shortDescription', title='$title', userId='$userId', vote=$vote, id='$id')"
    }
}