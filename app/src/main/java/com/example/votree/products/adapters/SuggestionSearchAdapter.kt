package com.example.votree.products.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R

class SuggestionSearchAdapter(
    private val suggestions: MutableList<String>,
    private val onSuggestionClicked: (String) -> Unit
) : RecyclerView.Adapter<SuggestionSearchAdapter.SuggestionViewHolder>() {

    interface onSuggestionClickListener {
        fun onSuggestionClick(suggestion: String)
    }
    private var listener: onSuggestionClickListener? = null

    fun setOnSuggestionClickListener(listener: onSuggestionClickListener) {
        this.listener = listener
    }

    class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productNameTextView: TextView = itemView.findViewById(R.id.productName_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_suggestion_search_adapter, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.productNameTextView.text = suggestion
        holder.itemView.setOnClickListener {
            onSuggestionClicked(suggestion)
        }
    }

    override fun getItemCount(): Int = suggestions.size
    fun updateSuggestions(newSuggestions: List<String>) {
        suggestions.clear()
        suggestions.addAll(newSuggestions)
        notifyDataSetChanged()
    }
}
