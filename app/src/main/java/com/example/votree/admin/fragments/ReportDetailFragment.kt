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
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.models.Report
import com.example.votree.models.Tip
import com.example.votree.models.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        val viewDetailButton = view?.findViewById<Button>(R.id.viewDetailButton)
        val viewReporterButton = view?.findViewById<Button>(R.id.viewReporterButton)
        val viewUserButton = view?.findViewById<Button>(R.id.viewUserButton)
        val unresolveButton = view?.findViewById<Button>(R.id.unresolveButton)
        val warnButton = view?.findViewById<Button>(R.id.warnButton)

        viewDetailButton?.setOnClickListener {
            if (report?.tipId != null && report?.tipId != "") {
                val fragment = TipDetailFragment()
                db.collection("ProductTip").document(report!!.tipId!!).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val tip = document.toObject(Tip::class.java)
                            fragment.arguments = Bundle().apply {
                                putParcelable("tip", tip)
                            }
                            val fragmentManager = (activity as FragmentActivity).supportFragmentManager
                            (activity as AdminMainActivity).setCurrentFragment(TipDetailFragment())
                            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("report_detail_fragment").commit()
                        }
                    }
            } else {
                // for different type of report
                val fragment = TipDetailFragment()
            }
        }

        viewReporterButton?.setOnClickListener {
            val fragment = AccountDetailFragment()
            db.collection("users").document(report!!.reporterId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(User::class.java)
                        fragment.arguments = Bundle().apply {
                            putParcelable("account", user)
                        }
                        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
                        (activity as AdminMainActivity).setCurrentFragment(AccountDetailFragment())
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("report_detail_fragment").commit()
                    }
                }
        }

        viewUserButton?.setOnClickListener {
            val fragment = AccountDetailFragment()
            db.collection("users").document(report!!.userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(User::class.java)
                        fragment.arguments = Bundle().apply {
                            putParcelable("account", user)
                        }
                        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
                        (activity as AdminMainActivity).setCurrentFragment(AccountDetailFragment())
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("report_detail_fragment").commit()
                    }
                }
        }

        unresolveButton?.setOnClickListener {
            context?.let { it1 ->
                MaterialAlertDialogBuilder(it1)
                    .setTitle("Unresolve Report")
                    .setMessage("This article is not violating the platform's rules?")
                    .setNegativeButton("Cancel") { _, _ -> }
                    .setPositiveButton("Accept") { _, _ ->
                        db.collection("reports").document(report!!.id).update("processingMethod", "Unresolved")
                            .addOnSuccessListener {
                                activity?.supportFragmentManager?.popBackStack("report_list_fragment", POP_BACK_STACK_INCLUSIVE)
                            }
                    }
                    .show()
            }
        }

        warnButton?.setOnClickListener {
            context?.let { it1 ->
                MaterialAlertDialogBuilder(it1)
                    .setTitle("Warn User")
                    .setMessage("This article violates the platform's rules?")
                    .setNegativeButton("Cancel") { _, _ -> }
                    .setPositiveButton("Accept") { _, _ ->
                        db.collection("reports").document(report!!.id).update("processingMethod", "Warned")
                            .addOnSuccessListener {
                                activity?.supportFragmentManager?.popBackStack("report_list_fragment", POP_BACK_STACK_INCLUSIVE)
                            }
                    }
                    .show()
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

            db.collection("users").document(nonNullTip.reporterId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val reportedByText = java.lang.String.format(resources.getString(R.string.reported_by), document.data?.get("username").toString())
                        view?.findViewById<TextView>(R.id.reportedBy)?.text = reportedByText
                    }
                }

            db.collection("users").document(nonNullTip.userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val beingReportedText = java.lang.String.format(resources.getString(R.string.being_reported), document.data?.get("username").toString())
                        view?.findViewById<TextView>(R.id.beingReported)?.text = beingReportedText
                    }
                }

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

    fun getReporterId(): String? {
        return report?.reporterId
    }
}