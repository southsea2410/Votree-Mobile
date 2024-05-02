package com.example.votree.admin.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.admin.interfaces.OnItemClickListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

abstract class BaseListAdapter<T>(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<BaseListAdapter<T>.BaseViewHolder>() {

    private var items = emptyList<T>()
    private var respectiveList = emptyList<Int>()
    protected abstract var singleitem_selection_position: Int
    val db = Firebase.firestore

    abstract inner class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClicked(itemView, position)
                }
            }
        }

        open fun bind(item: T) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(getLayoutId(), parent, false)
        return createViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    abstract fun getLayoutId(): Int

    abstract fun createViewHolder(itemView: View): BaseViewHolder

    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: List<T>) {
        this.items = items
        Log.d("BaseListAdapter", "setData: $items")
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: List<T>, respectiveList: List<Int>) {
        this.items = items
        this.respectiveList = respectiveList
        notifyDataSetChanged()
    }

    fun getItem(position: Int): T {
        return items[position]
    }

    fun getSelectedPosition(): Int {
        return singleitem_selection_position
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedPosition(position: Int) {
        singleitem_selection_position = position
        notifyDataSetChanged()
    }

    fun priceFormat(price: String): String {
        return price.substring(0, price.length - 3) + "." + price.substring(price.length - 3) + " VND"
    }

    fun dateFormat(date: String): String {
        return date.substring(7, 9) + "/" + date.substring(5, 6) + date.substring(6, 7) + "/" + date.substring(0, 4)
    }
}

