package com.example.votree.users.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.votree.R
import com.example.votree.users.models.User
import com.google.android.material.textview.MaterialTextView

class UserAdapter (private val user: User) : RecyclerView.Adapter<UserAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView: MaterialTextView = itemView.findViewById(R.id.Name)
        val phoneView: MaterialTextView = itemView.findViewById(R.id.Phone)
        val addressView : MaterialTextView = itemView.findViewById(R.id.Address)
        val emailView: MaterialTextView = itemView.findViewById(R.id.Email)

        fun bind(context: Context, user: User) {
            nameView.text = user.fullName
            phoneView.text = user.phoneNumber
            addressView.text = user.address
            emailView.text = user.email
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_personal_account, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        holder.bind(context, user)
    }

    override fun getItemCount(): Int {
        return 1
    }
}