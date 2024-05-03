package com.example.votree.admin.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.models.Store
import com.example.votree.models.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        val annouceButton = view.findViewById<Button>(R.id.annouceButton)
        val viewTransactionListButton = view.findViewById<TextView>(R.id.viewTransactionButton)
////        val viewDiscountsButton = view.findViewById<Button>(R.id.viewDiscountsButton)
//        val viewProductListButton = view.findViewById<Button>(R.id.viewProductListButton)
//        val viewTipListButton = view.findViewById<Button>(R.id.viewTipListButton)
        val viewReportListButton = view.findViewById<TextView>(R.id.viewReportListButton)
        val topAppBar = (activity as AdminMainActivity).findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
        topAppBar.title = "Account Detail"
        topAppBar.setTitleTextColor(resources.getColor(R.color.md_theme_primary))

        viewTransactionListButton.setOnClickListener {
            val accountId = account?.id
            accountId?.let { id ->
                val dialogFragment = TransactionDialogFragment.newInstance(id)
                dialogFragment.show(parentFragmentManager, "TransactionDialogFragment")
            }
        }

//        viewProductListButton.setOnClickListener {
//            val accountId = account?.id
//            accountId?.let { id ->
//                val dialogFragment = ProductDialogFragment.newInstance(id)
//                dialogFragment.show(parentFragmentManager, "ProductDialogFragment")
//            }
//        }
//
//        viewTipListButton.setOnClickListener {
//            val accountId = account?.id
//            accountId?.let { id ->
//                val dialogFragment = TipDialogFragment.newInstance(id)
//                dialogFragment.show(parentFragmentManager, "TipDialogFragment")
//            }
//        }
//
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

        annouceButton?.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_feedback, null)
            val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setTitle("Announce Message")
            val alertDialog = alertDialogBuilder.show()
            val editTextFeedback = dialogView.findViewById<EditText>(R.id.editTextFeedback)
            val buttonReject = dialogView.findViewById<Button>(R.id.buttonReject)
            val buttonClose = dialogView.findViewById<Button>(R.id.buttonClose)
            buttonReject.text = "Send"


            buttonReject.setOnClickListener {
                db.collection("users").document(account!!.id).update("announcedMessage", editTextFeedback.text.toString())
                    .addOnSuccessListener {
                        activity?.supportFragmentManager?.popBackStack("account_list_fragment",
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                    }
                alertDialog.dismiss()
            }

            buttonClose.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        account = arguments?.getParcelable("account", User::class.java)

        updateUI()
    }

    private fun updateUI() {
        account?.let { nonNullAccount ->
            view?.findViewById<TextView>(R.id.account_name)?.text = nonNullAccount.fullName
            view?.findViewById<TextView>(R.id.account_role)?.text = nonNullAccount.role
            view?.findViewById<TextView>(R.id.account_username)?.text = nonNullAccount.username
            view?.findViewById<TextView>(R.id.account_phone_number)?.text = nonNullAccount.phoneNumber
            view?.findViewById<TextView>(R.id.account_email)?.text = nonNullAccount.email
            view?.findViewById<TextView>(R.id.account_address)?.text = nonNullAccount.address
            view?.findViewById<TextView>(R.id.account_accumulate_point)?.text = nonNullAccount.accumulatePoint.toString()
            view?.findViewById<TextView>(R.id.account_created_date)?.text = dateFormat(nonNullAccount.createdAt.toString())
            view?.findViewById<TextView>(R.id.account_updated_date)?.text = dateFormat(nonNullAccount.updatedAt.toString())
            val storeInfoBtn = view?.findViewById<TextView>(R.id.storeInfoBtn)
            val copyUserNameBtn = view?.findViewById<TextView>(R.id.copy_user_name)
            val copyPointBtn = view?.findViewById<TextView>(R.id.copy_point)
            val copyPhone = view?.findViewById<TextView>(R.id.copy_phone)
            val copyEmail = view?.findViewById<TextView>(R.id.copy_email)
            val copyAddress = view?.findViewById<TextView>(R.id.copy_address)
            if (nonNullAccount.role.lowercase() == "user") {
                storeInfoBtn?.setTextColor(resources.getColor(R.color.md_theme_gray_addition_2))
            } else {
                storeInfoBtn?.setTextColor(resources.getColor(R.color.md_theme_primary))
//                storeInfoBtn?.setOnClickListener {
//                    val fragment = StoreDetailFragment()
//                    db.collection("stores").document(nonNullAccount.storeId).get()
//                        .addOnSuccessListener { document ->
//                            if (document != null) {
//                                val store = document.toObject(Store::class.java)
//                                fragment.arguments = Bundle().apply {
//                                    putParcelable("store", store)
//                                }
//                                val fragmentManager = (activity as FragmentActivity).supportFragmentManager
//                                (activity as AdminMainActivity).setCurrentFragment(StoreDetailFragment())
//                                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("account_detail_fragment").commit()
//                            }
//                        }
//                }
            }

            val avatarImage = view?.findViewById<ImageView>(R.id.account_avatar)
            avatarImage?.let {
                Glide.with(this)
                    .load(nonNullAccount.avatar)
                    .into(it)
            }

            avatarImage?.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("imageUrl", nonNullAccount.avatar)

                val fragment = ImageFragment()
                fragment.arguments = bundle

                (activity as AdminMainActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            if (nonNullAccount.expireBanDate.after(Date())) {
                view?.findViewById<LinearLayout>(R.id.beBaned)?.visibility = View.VISIBLE
                view?.findViewById<TextView>(R.id.account_time_out_date)?.text = dateFormat(nonNullAccount.expireBanDate.toString())
            } else {
                view?.findViewById<LinearLayout>(R.id.beBaned)?.visibility = View.GONE
            }

            copyUserNameBtn?.setOnClickListener {
                val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", nonNullAccount.username)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
            }
            copyPhone?.setOnClickListener {
                val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", nonNullAccount.phoneNumber)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
            }
            copyEmail?.setOnClickListener {
                val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", nonNullAccount.email)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
            }
            copyAddress?.setOnClickListener {
                val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", nonNullAccount.address)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
            }
            copyPointBtn?.setOnClickListener {
                val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", nonNullAccount.accumulatePoint.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val inputDate = inputFormat.parse(date)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        return outputFormat.format(inputDate)
    }
}