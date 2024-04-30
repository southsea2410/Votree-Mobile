package com.example.votree.admin.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.admin.adapters.TipListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Tip
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TipListFragment : BaseListFragment<Tip>(), OnItemClickListener {

    override val adapter: BaseListAdapter<Tip> by lazy { TipListAdapter(this) }
    override val itemList = mutableListOf<Tip>()
    override val collectionName = "ProductTip"

    override fun getLayoutId(): Int = R.layout.fragment_list

    public override fun fetchDataFromFirestore() {
        super.fetchDataFromFirestore()
        if (currentUserId != "") {
            db.collection(collectionName)
                .whereEqualTo("userId", currentUserId)
                .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("TipListFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    itemList.clear()
                    for (doc in snapshots!!) {
                        val tip = doc.toObject(Tip::class.java)
                        tip.id = doc.id
                        itemList.add(tip)
                    }
                    adapter.setData(itemList)
                }
        } else {
            db.collection(collectionName)
                .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("TipListFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    itemList.clear()
                    for (doc in snapshots!!) {
                        val tip = doc.toObject(Tip::class.java)
                        tip.id = doc.id
                        itemList.add(tip)
                    }
                    adapter.setData(itemList)
                }
        }
    }

    override fun onTipItemClicked(view: View?, position: Int) {
        (activity as AdminMainActivity).onTipItemClicked(view, position)
        val topAppBar: MaterialToolbar = (activity as AdminMainActivity).findViewById(R.id.topAppBar)
        topAppBar.menu.findItem(R.id.more).title = "Delete Tip"
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.more -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Tip")
                        .setMessage("Are you sure you want to delete this tip?")
                        .setPositiveButton("Yes") { _, _ ->
                            db.collection(collectionName).document(adapter.getItem(position).id).delete()
                                .addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!") }
                                .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error deleting document", e) }

                            (activity as FragmentActivity).supportFragmentManager.popBackStack()
                        }
                        .setNegativeButton("No") { _, _ -> }
                        .show()
                    true
                }

                else -> false
            }
        }

        val fragment = TipDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("tip", adapter.getItem(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("tip_list_fragment").commit()
    }

    override fun searchItem(query: String) {
        val searchResult = itemList.filter {
            it.title.contains(query, ignoreCase = true) || it.shortDescription.contains(query, ignoreCase = true)
        }

        adapter.setData(searchResult)
    }
}
