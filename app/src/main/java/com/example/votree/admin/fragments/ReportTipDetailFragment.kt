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
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.models.Tip
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class ReportTipDetailFragment : Fragment() {

    private val db = Firebase.firestore
    private var tip: Tip? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tip_detail_report, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tip = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("tip", Tip::class.java)
        } else {
            arguments?.getParcelable("tip", Tip::class.java)
        }

        updateUI()
    }

    private fun updateUI() {

        // !!! WILL USE IN THE FUTURE !!!
//        db.collection("users")
//            .addSnapshotListener { userSnapshots, userError ->
//                if (userError != null) {
//                    return@addSnapshotListener
//                }
//
//                for (userDoc in userSnapshots!!) {
//                    val user = userDoc.toObject(User::class.java)
//                    if (user.id == tip?.userId) {
//                        view?.findViewById<TextView>(R.id.userName)?.text = user.userName
//                        db.collection("stores")
//                            .addSnapshotListener { storeSnapshots, storeError ->
//                                if (storeError != null) {
//                                    return@addSnapshotListener
//                                }
//
//                                for (storeDoc in storeSnapshots!!) {
//                                    val store = storeDoc.toObject(Store::class.java)
//                                    if (store.id == user.storeId) {
//                                        view?.findViewById<TextView>(R.id.storeName)?.text = store.storeName
//                                    }
//                                }
//                            }
//                    }
//                }
//            }

        // Access the views in the fragment layout and update them with tip details
        tip?.let { nonNullTip ->
            val tipStatusTextView: TextView? = view?.findViewById(R.id.tipStatus)

            view?.findViewById<TextView>(R.id.tipName)?.text = nonNullTip.title
            view?.findViewById<ImageView>(R.id.tipStatusIcon)?.setColorFilter(
                when (nonNullTip.approvalStatus) {
                    0 -> resources.getColor(R.color.md_theme_pending, null)
                    1 -> resources.getColor(R.color.md_theme_primary, null)
                    -1 -> resources.getColor(R.color.md_theme_error, null)
                    else -> resources.getColor(R.color.md_theme_primary, null)
                }
            )
            tipStatusTextView?.text = when (nonNullTip.approvalStatus) {
                0 -> "Pending"
                1 -> "Approved"
                -1 -> "Rejected"
                else -> "Unknown"
            }
            tipStatusTextView?.setTextColor(
                when (nonNullTip.approvalStatus) {
                    0 -> resources.getColor(R.color.md_theme_pending, null)
                    1 -> resources.getColor(R.color.md_theme_primary, null)
                    -1 -> resources.getColor(R.color.md_theme_error, null)
                    else -> resources.getColor(R.color.md_theme_primary, null)
                }
            )
            view?.findViewById<ImageView>(R.id.tipImage)?.let { imageView ->
                Glide.with(this)
                    .load(nonNullTip.imageList[0])
                    .into(imageView)
            }
            view?.findViewById<TextView>(R.id.dateOfTip)?.text = dateFormat(nonNullTip.updatedAt.toString())
            val upvotesText = resources.getString(R.string.upvotes_placeholder, nonNullTip.vote)
            view?.findViewById<TextView>(R.id.upvotes)?.text = upvotesText
            view?.findViewById<TextView>(R.id.tipDescription)?.text = nonNullTip.content
        }
    }

    private fun dateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val inputDate = inputFormat.parse(date)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        return outputFormat.format(inputDate)
    }
}
