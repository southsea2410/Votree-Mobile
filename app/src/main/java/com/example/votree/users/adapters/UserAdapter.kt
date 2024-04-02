package com.example.votree.users.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.products.adapters.ProductAdapter
import com.example.votree.products.fragments.ProductListDirections
import com.example.votree.products.models.Product
import com.example.votree.users.models.User

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fullName = itemView.findViewById<TextView>(R.id.Name)
        var phoneNumber = itemView.findViewById<TextView>(R.id.Phone)
        var address = itemView.findViewById<TextView>(R.id.Address)
        var email = itemView.findViewById<TextView>(R.id.Email)
        var currentPassword = itemView.findViewById<TextView>(R.id.CurPass)
        var newPassword = itemView.findViewById<View>(R.id.NewPass)
        val confirmPassword = itemView.findViewById<ImageView>(R.id.ConPass)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.UserViewHolder {
        return UserAdapter.UserViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.profile, parent, false)
        )
    }



}