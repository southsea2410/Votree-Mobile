package com.example.votree.admin.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.admin.adapters.ReportListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Report

class ReportListFragment : BaseListFragment<Report>(), OnItemClickListener {

    override val adapter: BaseListAdapter<Report> by lazy { ReportListAdapter(this) }
    override val itemList = mutableListOf<Report>()
    override val collectionName = "reports"

    override fun getLayoutId(): Int = R.layout.fragment_list

    override fun fetchDataFromFirestore() {
        super.fetchDataFromFirestore()
        if (currentUserId != "") {
            db.collection(collectionName).whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("ReportListFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    itemList.clear()
                    for (doc in snapshots!!) {
                        val report = doc.toObject(Report::class.java)
                        report.id = doc.id
                        itemList.add(report)
                    }
                    adapter.setData(itemList)
                }

            db.collection(collectionName).whereEqualTo("reporterId", currentUserId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("ReportListFragment", "listen:error", e)
                        return@addSnapshotListener
                    }

                    for (doc in snapshots!!) {
                        val report = doc.toObject(Report::class.java)
                        report.id = doc.id
                        if (report.reporterId != report.userId) {
                            itemList.add(report)
                        }
                    }

                    adapter.setData(itemList)
                }
        }
        else {
            db.collection(collectionName)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("ReportListFragment", "listen:error", e)
                        return@addSnapshotListener
                    }
                    itemList.clear()
                    for (doc in snapshots!!) {
                        val report = doc.toObject(Report::class.java)
                        report.id = doc.id
                        itemList.add(report)
                    }
                    adapter.setData(itemList)
                }
        }
    }

    override fun onReportItemClicked(view: View?, position: Int, processStatus: Boolean) {
        (activity as AdminMainActivity).onReportItemClicked(view, position, processStatus)
        val fragment = ReportDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("report", adapter.getItem(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("report_list_fragment").commit()
    }

    override fun searchItem(query: String) {
        val searchResult = itemList.filter {
            val user = getUserById(it.reporterId)
            user.username.contains(query, ignoreCase = true) || it.shortDescription.contains(query, ignoreCase = true)
        }

        adapter.setData(searchResult)
    }
}
