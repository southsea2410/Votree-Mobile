package com.example.votree.admin.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.AccountListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AccountListFragment : Fragment(), OnItemClickListener {

    private val db = Firebase.firestore
    private val adapter: AccountListAdapter by lazy { AccountListAdapter(this) }
    private val accountList = mutableListOf<User>()
    private var previousBackStackEntryCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_list, container, false)

        val recyclerViewAccountList: RecyclerView? = view?.findViewById(R.id.accountListRecycleView)
        recyclerViewAccountList?.adapter = adapter
        recyclerViewAccountList?.layoutManager = LinearLayoutManager(context)

        fetchDataFromFirestore()

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.addOnBackStackChangedListener {

            val currentBackStackEntryCount = fragmentManager.backStackEntryCount

            if (currentBackStackEntryCount < previousBackStackEntryCount) {
                fetchDataFromFirestore()
                (activity as AdminMainActivity).setupNormalActionBar()
            }

            previousBackStackEntryCount = currentBackStackEntryCount
        }

        return view
    }

    override fun onTipItemClicked(view: View?, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onAccountItemClicked(view: View?, position: Int) {
        (activity as AdminMainActivity).onAccountItemClicked(view, position)

        val fragment = AccountDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("account", adapter.getAccount(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("account_list_fragment").commit()
    }

    override fun onReportItemClicked(view: View?, position: Int, processStatus: Boolean) { }

    private fun fetchDataFromFirestore() {
        db.collection("users")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AccountListActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                accountList.clear()

                for (doc in snapshots!!) {
                    val account = doc.toObject(User::class.java)
                    account.id = doc.id
                    accountList.add(account)
                }

                adapter.setData(accountList)
            }
    }

    override fun searchItem(query: String) {
        val searchResult = accountList.filter {
            it.userName.contains(query, ignoreCase = true) || it.role.contains(query, ignoreCase = true)
        }

        adapter.setData(searchResult)
    }
}