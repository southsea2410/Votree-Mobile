package com.example.votree.admin.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R

class DateAdapter (private val nDates: List<String>) : RecyclerView.Adapter<DateAdapter.ViewHolder>() {
    var singleitem_selection_position = -1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

        init {
            itemView.setOnClickListener{
                setSelectedPosition(adapterPosition)
            }
        }

        fun bind(position: Int) {
            // Set the background color based on the selected position
            if (position == singleitem_selection_position) {
                itemView.setBackgroundResource(android.R.color.holo_blue_bright)
            } else {
                itemView.setBackgroundResource(android.R.color.transparent)
            }

            val specificDate = nDates[position]
            dateTextView.text = specificDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val dateView = LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
        return ViewHolder(dateView)
    }

    override fun onBindViewHolder(viewHolder: DateAdapter.ViewHolder, position: Int) {
        val specificDate = nDates[position]
        val numberOfDays = viewHolder.dateTextView

        numberOfDays.text = specificDate

        viewHolder.bind(position)
    }

    override fun getItemCount(): Int {
        return nDates.size
    }

    fun getSelectedPosition(): Int {
        return singleitem_selection_position
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedPosition(position: Int) {
        singleitem_selection_position = position
        notifyDataSetChanged()
    }
}