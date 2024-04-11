package com.example.votree.admin.fragments

import android.R
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.votree.admin.adapters.SpinnerAdapter

class ReportResolveDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adapter = SpinnerAdapter(requireContext(), R.layout.simple_spinner_item, listOf("Resolved", "Unresolved"))

        return super.onCreateDialog(savedInstanceState)
    }
}