package com.example.votree.tips.models


data class ProductTip(
    var approvalStatus: Int = 0,
    var content: String = "",
    var createdAt: String = "",
    var updatedAt: String = "",
    var imageList: MutableList<String> = mutableListOf(""),
    var shortDescription: String = "",
    var title: String = "",
    var userId: String = "",
    var vote: Int = 0,
    var id: String = "",
) {
}