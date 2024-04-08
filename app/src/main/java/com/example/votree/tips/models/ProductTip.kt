package com.example.votree.tips.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


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
) {
    override fun toString(): String {
        return "ProductTip(approvalStatus=$approvalStatus, content='$content', createdAt=$createdAt, updatedAt=$updatedAt, imageList=$imageList, shortDescription='$shortDescription', title='$title', userId='$userId', vote=$vote, id='$id')"
    }
}