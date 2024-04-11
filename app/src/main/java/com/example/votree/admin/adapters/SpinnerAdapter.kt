package com.example.votree.admin.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class SpinnerAdapter (context: Context, textViewResourceId: Int, private val values: List<String>) : ArrayAdapter<String>(context, textViewResourceId, values) {
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getDropDownView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getView(position, convertView, parent)
    }
}