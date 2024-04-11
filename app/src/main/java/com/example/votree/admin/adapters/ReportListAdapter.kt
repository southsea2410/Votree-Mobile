package com.example.votree.admin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Report
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ReportListAdapter(private var listener: OnItemClickListener) :
    RecyclerView.Adapter<ReportListAdapter.ViewHolder>() {

    private var reportList = emptyList<Report>()
    val db = Firebase.firestore

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val reportView = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ViewHolder(reportView)
    }

    override fun getItemCount(): Int {
        return reportList.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentReport = reportList[position]

        Glide.with(viewHolder.itemView.context)
            .load(currentReport.imageList[0])
            .into(viewHolder.itemView.findViewById(R.id.report_list_item_image))

        viewHolder.itemView.findViewById<TextView>(R.id.report_list_item_short_description).text = currentReport.shortDescription
        when (currentReport.processStatus) {
            true -> viewHolder.itemView.findViewById<TextView>(R.id.report_list_item_short_description).setTextColor(viewHolder.itemView.resources.getColor(R.color.md_theme_primary))
            false -> viewHolder.itemView.findViewById<TextView>(R.id.report_list_item_short_description).setTextColor(viewHolder.itemView.resources.getColor(R.color.md_theme_error))
        }

        db.collection("users").document(currentReport.reporterId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    viewHolder.itemView.findViewById<TextView>(R.id.report_list_item_reporter).text = document.data?.get("username").toString()
                }
            }

        viewHolder.itemView.setOnClickListener {
            listener.onReportItemClicked(viewHolder.itemView, position, currentReport.processStatus)
        }
    }

    fun setData(reportList: List<Report>) {
        this.reportList = reportList
        notifyDataSetChanged()
    }

    fun getReport(position: Int): Report {
        return reportList[position]
    }
}