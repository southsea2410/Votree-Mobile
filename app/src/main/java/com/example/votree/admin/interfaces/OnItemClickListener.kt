package com.example.votree.admin.interfaces

import android.view.View
import com.example.votree.models.Tip

interface OnItemClickListener {
    fun onTipItemClicked(view: View?, position: Int)
    fun onAccountItemClicked(view: View?, position: Int)
    fun onReportItemClicked(view: View?, position: Int)
    fun searchItem(query: String)
}