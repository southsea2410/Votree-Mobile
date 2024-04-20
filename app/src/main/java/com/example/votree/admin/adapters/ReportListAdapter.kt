package com.example.votree.admin.adapters

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Report

@Suppress("DEPRECATION")
class ReportListAdapter(private val listener: OnItemClickListener, private val isDialog: Boolean = false) :
    BaseListAdapter<Report>(listener) {

    override var singleitem_selection_position = 0

    override fun getLayoutId(): Int = R.layout.item_report

    override fun createViewHolder(itemView: View): BaseViewHolder {
        return ViewHolder(itemView)
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {

        override fun bind(item: Report) {
            super.bind(item)

            Glide.with(itemView.context)
                .load(item.imageList[0])
                .into(itemView.findViewById(R.id.report_list_item_image))

            itemView.findViewById<TextView>(R.id.report_list_item_short_description).text =
                item.shortDescription

            when (item.processStatus) {
                true -> itemView.findViewById<TextView>(R.id.report_list_item_short_description)
                    .setTextColor(
                        itemView.resources.getColor(
                            R.color.md_theme_primary
                        )
                    )

                false -> itemView.findViewById<TextView>(R.id.report_list_item_short_description)
                    .setTextColor(
                        itemView.resources.getColor(
                            R.color.md_theme_error
                        )
                    )
            }

            db.collection("users").document(item.reporterId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        itemView.findViewById<TextView>(R.id.report_list_item_reporter).text =
                            document.data?.get("username").toString()
                    }
                }

            if (isDialog) {
                if (absoluteAdapterPosition == singleitem_selection_position) {
                    itemView.setBackgroundResource(android.R.color.holo_blue_bright)
                } else {
                    itemView.setBackgroundResource(android.R.color.transparent)
                }
            } else {
                itemView.setOnClickListener {
                    listener.onReportItemClicked(itemView, absoluteAdapterPosition, item.processStatus)
                }
            }
        }
    }
}
