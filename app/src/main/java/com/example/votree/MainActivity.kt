package com.example.votree

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.admin.adapters.TipListAdapter
import com.example.votree.admin.fragments.TipDetailFragment
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Tip
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity(), OnItemClickListener {

    private val db = Firebase.firestore
//    private val storage = Firebase.storage
//    private val storageRef = storage.reference

    private val adapter: TipListAdapter by lazy { TipListAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip_list)

//        val forestRef = storageRef.child("images/productTips/1.jpg")

//        Glide.with(this)
//            .load(forestRef)
//            .into(findViewById(R.id.list_item_avatar))

//        forestRef.metadata.addOnSuccessListener { metadata ->
//
//        }.addOnFailureListener {
//            // Uh-oh, an error occurred!
//        }

        val tipList = mutableListOf<Tip>()

        val recycleViewTipList: androidx.recyclerview.widget.RecyclerView = findViewById(R.id.tipListRecycleView)
        recycleViewTipList.adapter = adapter
        recycleViewTipList.layoutManager = LinearLayoutManager(this)

        db.collection("ProductTip")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("MainActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                for (doc in snapshots!!) {
                    val tip = doc.toObject(Tip::class.java)
                    tipList.add(tip)
                }

                adapter.setData(tipList)
            }
    }

    override fun onTipItemClicked(view: View?, position: Int) {
        val fragment = TipDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("tip", adapter.getTip(position))
        }
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}