package com.example.votree.admin.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.admin.adapters.AccountListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AccountListFragment : BaseListFragment<User>() {

    override val adapter: BaseListAdapter<User> by lazy { AccountListAdapter(this) }
    override val itemList = mutableListOf<User>()
    override val collectionName = "users"

    override fun getLayoutId(): Int = R.layout.fragment_list

    override fun fetchDataFromFirestore() {
        db.collection(collectionName)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AccountListFragment", "listen:error", e)
                    return@addSnapshotListener
                }

                itemList.clear()
                for (doc in snapshots!!) {
                    val account = doc.toObject(User::class.java)
                    account.id = doc.id
                    itemList.add(account)
                }
                adapter.setData(itemList)
            }
    }

    override fun onAccountItemClicked(view: View?, position: Int) {
        (activity as AdminMainActivity).onAccountItemClicked(view, position)
        val topAppBar: MaterialToolbar = (activity as AdminMainActivity).findViewById(R.id.topAppBar)
        topAppBar.menu.findItem(R.id.more).title = "Delete Account"
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.more -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to delete this account?")
                        .setPositiveButton("Yes") { _, _ ->
                            db.collection(collectionName).document(adapter.getItem(position).id).delete()
                                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }

                            (activity as FragmentActivity).supportFragmentManager.popBackStack()
                        }
                        .setNegativeButton("No") { _, _ -> }
                        .show()
                    true
                }

                else -> false
            }
        }

        val fragment = AccountDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("account", adapter.getItem(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("account_list_fragment").commit()
    }

    override fun searchItem(query: String) {
        val searchResult = itemList.filter {
            it.username.contains(query, ignoreCase = true) || it.role.contains(query, ignoreCase = true)
        }

        adapter.setData(searchResult)
    }
}
