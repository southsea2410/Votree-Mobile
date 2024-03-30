package com.example.votree.admin.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.votree.R
import com.example.votree.models.Tip

class TipDetailFragment : Fragment() {

    private var tip: Tip? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tip_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the tip object from the arguments bundle
        arguments?.getParcelable<Tip>("tip")?.let { receivedTip ->
            tip = receivedTip
            // Update UI with tip details
            updateUI()
        }
    }

    private fun updateUI() {
        // Access the views in the fragment layout and update them with tip details
        tip?.let { nonNullTip ->
            view?.findViewById<TextView>(R.id.tipUsername)?.text = nonNullTip.title
            view?.findViewById<TextView>(R.id.tipTitle)?.text = nonNullTip.title
//            view?.findViewById<TextView>(R.id.tipShortDescription)?.text = nonNullTip.shortDescription
//            view?.findViewById<TextView>(R.id.tipContent)?.text = nonNullTip.content
            // Update other views as needed
        }
    }
}
