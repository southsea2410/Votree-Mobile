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
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.models.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class AccountDetailFragment : Fragment() {

    private val db = Firebase.firestore
    private var account: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_detail, container, false)
        val banButton = view.findViewById<Button>(R.id.banButton)
        val viewTransactionListButton = view.findViewById<Button>(R.id.viewTransactionButton)
        val viewDiscountsButton = view.findViewById<Button>(R.id.viewDiscountsButton)
        val viewProductListButton = view.findViewById<Button>(R.id.viewProductListButton)
        val viewTipListButton = view.findViewById<Button>(R.id.viewTipListButton)
        val viewReportListButton = view.findViewById<Button>(R.id.viewReportListButton)
        val topAppBar = (activity as AdminMainActivity).findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
        topAppBar.title = "Account Detail"
        topAppBar.setTitleTextColor(resources.getColor(R.color.md_theme_primary))


//        viewTransactionListButton.setOnClickListener {
//            val reporterId = account?.id
//            reporterId?.let { id ->
//                val dialogFragment = TransactionListDialogFragment.newInstance(id)
//                dialogFragment.show(parentFragmentManager, "TransactionListDialogFragment")
//            }
//        }

        viewTipListButton.setOnClickListener {
            val accountId = account?.id
            accountId?.let { id ->
                val dialogFragment = TipDialogFragment.newInstance(id)
                dialogFragment.show(parentFragmentManager, "TipDialogFragment")
            }
        }

        viewReportListButton.setOnClickListener {
            val accountId = account?.id
            accountId?.let { id ->
                val dialogFragment = ReportDialogFragment.newInstance(id)
                dialogFragment.show(parentFragmentManager, "ReportDialogFragment")
            }
        }

        banButton?.setOnClickListener {
            val accountId = account?.id
            accountId?.let { id ->
                val dialogFragment = ChooseDateDialogFragment.newInstance(id)
                dialogFragment.show(parentFragmentManager, "ChooseDateDialogFragment")
            }
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        account = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("account", User::class.java)
        } else {
            arguments?.getParcelable("account", User::class.java)
        }

        updateUI()
    }

    private fun updateUI() {
        account?.let {nonNullTip ->
            view?.findViewById<TextView>(R.id.account_name)?.text = nonNullTip.fullName
            view?.findViewById<TextView>(R.id.account_role)?.text = nonNullTip.role
            view?.findViewById<TextView>(R.id.account_username)?.text = nonNullTip.username
            view?.findViewById<TextView>(R.id.account_phone_number)?.text = nonNullTip.phoneNumber
            view?.findViewById<TextView>(R.id.account_email)?.text = nonNullTip.email
            view?.findViewById<TextView>(R.id.account_address)?.text = nonNullTip.address
            view?.findViewById<TextView>(R.id.account_accumulate_point)?.text = nonNullTip.accumulatePoint.toString()
            view?.findViewById<TextView>(R.id.account_created_date)?.text = dateFormat(nonNullTip.createdAt.toString())
            view?.findViewById<TextView>(R.id.account_updated_date)?.text = dateFormat(nonNullTip.updatedAt.toString())
            if (nonNullTip.role.lowercase() == "user") {
                view?.findViewById<TextView>(R.id.account_discountList)?.visibility = View.GONE
                view?.findViewById<TextView>(R.id.account_productList)?.visibility = View.GONE
                view?.findViewById<TextView>(R.id.account_tipList)?.visibility = View.GONE
                view?.findViewById<Button>(R.id.viewDiscountsButton)?.visibility = View.GONE
                view?.findViewById<Button>(R.id.viewProductListButton)?.visibility = View.GONE
                view?.findViewById<Button>(R.id.viewTipListButton)?.visibility = View.GONE
            }

            if (nonNullTip.expireBanDate.after(Date())) {
                view?.findViewById<ImageView>(R.id.account_banned)?.setImageResource(R.drawable.baseline_check_box_24)
            } else {
                view?.findViewById<ImageView>(R.id.account_banned)?.setImageResource(R.drawable.baseline_check_box_outline_blank_24)
            }

            view?.findViewById<TextView>(R.id.account_time_out_date)?.text = dateFormat(nonNullTip.expireBanDate.toString())
        }
    }

    private fun dateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val inputDate = inputFormat.parse(date)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        return outputFormat.format(inputDate)
    }
}