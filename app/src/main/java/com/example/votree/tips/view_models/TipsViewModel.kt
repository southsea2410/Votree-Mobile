package com.example.votree.tips.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.votree.tips.models.ProductTip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TipsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private var _tipList = MutableLiveData<List<ProductTip>>()
    private var _topTipList = MutableLiveData<List<ProductTip>>()
    val tipList : LiveData<List<ProductTip>> = _tipList
    val topTipList : LiveData<List<ProductTip>> = _topTipList

    init {
        queryTopTips()
        queryAllTips()
    }

    fun queryAllTips() {
        val collection = firestore.collection("ProductTip")
        collection
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                _tipList.value = documents.toObjects(ProductTip::class.java)
                Log.d("TipViewModel", "Done getting documents: " + _tipList.value?.size)
            }
            .addOnFailureListener{
                Log.d("TipViewModel", "Error getting documents: ", it)
            }
    }

    fun queryTopTips() {
        val collection = firestore.collection("ProductTip")
        collection
            .orderBy("vote", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                _topTipList.value = documents.toObjects(ProductTip::class.java)
                Log.d("TipViewModel", "Done getting documents: " + _topTipList.value?.size)
            }
            .addOnFailureListener{
                Log.d("TipViewModel", "Error getting documents: ", it)
            }
    }
}