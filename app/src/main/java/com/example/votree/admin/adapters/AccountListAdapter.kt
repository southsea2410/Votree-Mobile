package com.example.votree.admin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.User

class AccountListAdapter(private var listener: OnItemClickListener) :
    RecyclerView.Adapter<AccountListAdapter.ViewHolder>() {

    private var accountList = emptyList<User>()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val accountView = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
        return ViewHolder(accountView)
    }

    override fun getItemCount(): Int {
        return accountList.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentAccount = accountList[position]

        Glide.with(viewHolder.itemView.context)
            .load(currentAccount.avatar)
            .into(viewHolder.itemView.findViewById(R.id.account_list_item_avatar))

        viewHolder.itemView.findViewById<TextView>(R.id.account_list_item_name).text = currentAccount.userName
        viewHolder.itemView.findViewById<TextView>(R.id.account_list_item_role).text = currentAccount.role
        viewHolder.itemView.setOnClickListener {
            listener.onAccountItemClicked(viewHolder.itemView, position)
        }
    }

    fun setData(accountList: List<User>) {
        this.accountList = accountList
        notifyDataSetChanged()
    }

    fun getAccount(position: Int): User {
        return accountList[position]
    }
}