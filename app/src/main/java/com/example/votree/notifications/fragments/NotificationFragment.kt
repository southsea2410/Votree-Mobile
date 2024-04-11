package com.example.votree.notifications.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.votree.R
import com.example.votree.databinding.FragmentNotificationBinding
import com.example.votree.databinding.FragmentUserProfileBinding

class NotificationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }
}