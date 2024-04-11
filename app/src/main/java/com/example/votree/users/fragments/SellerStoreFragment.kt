package com.example.votree.users.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.votree.R
import com.example.votree.databinding.FragmentSellerStoreBinding

class SellerStoreFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSellerStoreBinding.inflate(inflater, container, false)
        return binding.root
    }
}