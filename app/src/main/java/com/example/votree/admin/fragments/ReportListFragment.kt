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
import com.example.votree.admin.adapters.ReportListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Report
import com.example.votree.models.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ReportListFragment : Fragment(), OnItemClickListener {

    private val db = Firebase.firestore
    private val adapter: ReportListAdapter by lazy { ReportListAdapter(this) }
    private val reportList = mutableListOf<Report>()
    private var previousBackStackEntryCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_report_list, container, false)

        val recyclerViewReportList: RecyclerView? = view?.findViewById(R.id.reportListRecycleView)
        recyclerViewReportList?.adapter = adapter
        recyclerViewReportList?.layoutManager = LinearLayoutManager(context)

        fetchDataFromFirestore()

//        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
//
//        fragmentManager.addOnBackStackChangedListener {
//
//            val currentBackStackEntryCount = fragmentManager.backStackEntryCount
//
//            if (currentBackStackEntryCount < previousBackStackEntryCount) {
//                fetchDataFromFirestore()
                (activity as AdminMainActivity).setupNormalActionBar()
//            }

//            previousBackStackEntryCount = currentBackStackEntryCount
//        }

        return view
    }

    override fun onTipItemClicked(view: View?, position: Int) {}

    override fun onAccountItemClicked(view: View?, position: Int) {}

    override fun onReportItemClicked(view: View?, position: Int, processStatus: Boolean) {
        (activity as AdminMainActivity).onReportItemClicked(view, position, processStatus)

        val fragment = ReportDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("report", adapter.getReport(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("report_list_fragment").commit()
    }

    private fun fetchDataFromFirestore() {
        db.collection("reports")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ReportListActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                reportList.clear()

                for (doc in snapshots!!) {
                    val report = doc.toObject(Report::class.java)
                    report.id = doc.id
                    reportList.add(report)
                }

                adapter.setData(reportList)
            }
    }

    override fun searchItem(query: String) {
        val searchResult = reportList.filter {
            it.reporterId.contains(query, ignoreCase = true) || it.shortDescription.contains(query, ignoreCase = true)
        }

        adapter.setData(searchResult)
    }
}