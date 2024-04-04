package com.example.votree.admin.adapters

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Tip

class TipListAdapter(private var listener: OnItemClickListener) :
    RecyclerView.Adapter<TipListAdapter.ViewHolder>() {

    private var tipList = emptyList<Tip>()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val tipView = LayoutInflater.from(parent.context).inflate(R.layout.item_tip, parent, false)
        return ViewHolder(tipView)
    }

    override fun getItemCount(): Int {
        return tipList.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentTip = tipList[position]
        val firstImageUrl = currentTip.imageList.firstOrNull()

        Glide.with(viewHolder.itemView.context)
            .load(firstImageUrl)
            .into(viewHolder.itemView.findViewById(R.id.list_item_avatar))


        when (currentTip.approvalStatus) {
            -1 -> {
                viewHolder.itemView.findViewById<LinearLayout>(R.id.tip_row_layout).background = ContextCompat.getDrawable(viewHolder.itemView.context, R.color.md_theme_tertiaryContainer)
                viewHolder.itemView.findViewById<ImageView>(R.id.item_icon).setImageResource(R.drawable.red_tick)
            }
            1 -> {
                viewHolder.itemView.findViewById<LinearLayout>(R.id.tip_row_layout).background = ContextCompat.getDrawable(viewHolder.itemView.context, R.color.md_theme_primaryContainer)
                viewHolder.itemView.findViewById<ImageView>(R.id.item_icon).setImageResource(R.drawable.green_tick)
            }
            else -> {
                viewHolder.itemView.findViewById<LinearLayout>(R.id.tip_row_layout).background = ContextCompat.getDrawable(viewHolder.itemView.context, R.color.md_theme_onPrimary)
                viewHolder.itemView.findViewById<ImageView>(R.id.item_icon).setImageResource(R.drawable.baseline_arrow_right_24)
            }
        }
        viewHolder.itemView.findViewById<TextView>(R.id.list_item_title).text = currentTip.title
        viewHolder.itemView.findViewById<TextView>(R.id.list_item_short_description).text = currentTip.shortDescription
        viewHolder.itemView.setOnClickListener {
            listener.onTipItemClicked(viewHolder.itemView, position)
        }
    }

    fun setData(tips: List<Tip>) {
        this.tipList = tips
        notifyDataSetChanged()
    }

    fun getTip(position: Int): Parcelable {
        return tipList[position]
    }
}