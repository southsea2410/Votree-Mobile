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
import com.example.votree.admin.adapters.TipListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Tip
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TipListFragment : Fragment(), OnItemClickListener {

    private val db = Firebase.firestore
    private val adapter: TipListAdapter by lazy { TipListAdapter(this) }
    private val tipList = mutableListOf<Tip>()
    private var previousBackStackEntryCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tip_list, container, false)

        val recycleViewTipList: RecyclerView? = view?.findViewById(R.id.tipListRecycleView)
        recycleViewTipList?.adapter = adapter
        recycleViewTipList?.layoutManager = LinearLayoutManager(context)

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

        (activity as AdminMainActivity).onTipItemClicked(view, position)

        val fragment = TipDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("tip", adapter.getTip(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("tip_list_fragment").commit()
    }

    override fun onAccountItemClicked(view: View?, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onReportItemClicked(view: View?, position: Int) {
        TODO("Not yet implemented")
    }

    private fun fetchDataFromFirestore() {
        db.collection("ProductTip2")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("TipListActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                tipList.clear()

                for (doc in snapshots!!) {
                    val tip = doc.toObject(Tip::class.java)
                    tip.id = doc.id
                    tipList.add(tip)
                }

                adapter.setData(tipList)
            }
    }

    override fun searchItem(query: String) {
        val searchResult = tipList.filter {
            it.title.contains(query, ignoreCase = true) || it.shortDescription.contains(query, ignoreCase = true)
        }

        adapter.setData(searchResult)
    }
}