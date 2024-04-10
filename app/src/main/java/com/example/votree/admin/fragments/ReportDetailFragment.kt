package com.example.votree.admin.fragments

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.models.Report
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale


class ReportDetailFragment : Fragment() {

    private val db = Firebase.firestore
    private var report: Report? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_report_detail, container, false)
        val deleteButton = view?.findViewById<Button>(R.id.deleteButton)
        val unbanButton = view?.findViewById<Button>(R.id.unbanButton)
        val timeOutButton = view?.findViewById<Button>(R.id.timeOutButton)

        deleteButton?.setOnClickListener {
            db.collection("reports").document(report!!.id).delete()
                .addOnSuccessListener {
                    activity?.supportFragmentManager?.popBackStack("report_list_fragment", POP_BACK_STACK_INCLUSIVE)
                }
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        report = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("report", Report::class.java)
        } else {
            arguments?.getParcelable("report", Report::class.java)
        }

        updateUI()
    }

    private fun updateUI() {
        report?.let {nonNullTip ->
            view?.findViewById<ImageView>(R.id.reportImage)?.let { imageView ->
                Glide.with(this)
                    .load(nonNullTip.imageList[0])
                    .into(imageView)
            }

            if (nonNullTip.productId != null && nonNullTip.productId != "") {
                view?.findViewById<TextView>(R.id.reportType)?.text = "Product"
            } else if (nonNullTip.tipId != null && nonNullTip.tipId != "") {
                view?.findViewById<TextView>(R.id.reportType)?.text = "Tip"
            } else {
                view?.findViewById<TextView>(R.id.reportType)?.text = "User"
            }

            view?.findViewById<TextView>(R.id.reportDescription)?.text = nonNullTip.content

            val reportedByText = java.lang.String.format(resources.getString(R.string.reported_by), nonNullTip.reporterId)
            view?.findViewById<TextView>(R.id.reportedBy)?.text = reportedByText

            val beingReportedText = java.lang.String.format(resources.getString(R.string.being_reported), nonNullTip.userId)
            view?.findViewById<TextView>(R.id.beingReported)?.text = beingReportedText

            val reportedDate = dateFormat(nonNullTip.createdAt.toString())
            val reportedDateText = java.lang.String.format(resources.getString(R.string.reported_date), reportedDate)
            view?.findViewById<TextView>(R.id.reportedDate)?.text = reportedDateText
        }
    }

    private fun dateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val inputDate = inputFormat.parse(date)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        return outputFormat.format(inputDate)
    }
}