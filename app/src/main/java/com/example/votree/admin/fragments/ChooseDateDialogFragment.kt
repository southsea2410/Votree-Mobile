package com.example.votree.admin.fragments

import DialogFragmentListener
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R
import com.example.votree.admin.adapters.DateAdapter

class ChooseDateDialogFragment : DialogFragment() {

    private var listener: DialogFragmentListener? = null

    companion object {
        private const val REPORTER_ID = "reporter_id"
        fun newInstance(reporterId: String): ChooseDateDialogFragment {
            val fragment = ChooseDateDialogFragment()
            val args = Bundle()
            args.putString(REPORTER_ID, reporterId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dates = listOf("3 days", "1 week", "30 days", "365 days", "Forever")
        val days = listOf(3, 7, 30, 365, 77777)
        val inflater = requireActivity().layoutInflater
        val rvDates = inflater.inflate(R.layout.fragment_recycleview_date, null)
        val reporterId = arguments?.getString(REPORTER_ID)

        val adapter: DateAdapter = DateAdapter(dates)

        rvDates.findViewById<RecyclerView>(R.id.dateRecyclerViewFragment).adapter = adapter
        rvDates.findViewById<RecyclerView>(R.id.dateRecyclerViewFragment).layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(rvDates).setTitle("Ban Duration")
                .setPositiveButton("Save") { _, _ ->
                    val single_item_selection_position = adapter.getSelectedPosition()
                    listener?.updateExpireBanDateToFirestore(days[single_item_selection_position], reporterId!!)
                    Toast.makeText(context, "Ban successfully", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    null
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    // Attach the listener when the fragment is attached to the activity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DialogFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement DialogFragmentListener")
        }
    }

    // Detach the listener when the fragment is detached from the activity
    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}