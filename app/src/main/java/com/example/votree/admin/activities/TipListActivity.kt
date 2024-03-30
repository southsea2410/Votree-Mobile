package com.example.votree.admin.activities

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TipListActivity : AppCompatActivity() {

    private val db = Firebase.firestore

//    private lateinit var mTips: TipListViewModel

//    private val adapter: ListAdapter by lazy { ListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip_list)

        val city = hashMapOf(
            "name" to "Los Angeles",
            "state" to "CA",
            "country" to "USA",
        )

        db.collection("cities").document("LA")
            .set(city)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

//        val recycleViewTipList: androidx.recyclerview.widget.RecyclerView = findViewById(R.id.tipListRecycleView)
//        recycleViewTipList.adapter = adapter
//        mTips = ViewModelProvider(this)[TipListViewModel::class.java]
//        mTips.readAllTips.observe(this) { tip ->
//            adapter.setData(tip)
//        }
    }

//    private fun searchTips(query: String) {
//        val searchQuery = "%$query"
//        mTips.searchDatabase(searchQuery).observe(this) { list ->
//            list.let {
//                adapter.setData(it)
//            }
//        }
//    }
}