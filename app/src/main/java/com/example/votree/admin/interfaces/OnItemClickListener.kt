package com.example.votree.admin.interfaces

import android.view.View
import com.example.votree.models.Tip

interface OnItemClickListener {
    fun onTipItemClicked(view: View?, position: Int)
}