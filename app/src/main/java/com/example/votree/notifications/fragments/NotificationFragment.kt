package com.example.votree.notifications.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.databinding.FragmentNotificationBinding
import com.example.votree.notifications.adapters.NotificationAdapter
import com.example.votree.notifications.models.Notification
import com.example.votree.notifications.view_models.NotificationViewModel
import com.google.android.material.tabs.TabLayout

class NotificationFragment : Fragment(), NotificationAdapter.OnNotificationClickListener {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels()
    private var adapter = NotificationAdapter(this)
    private var selectedTab = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("NotificationFragment", "onViewCreated")
        setupRecyclerView()
        observeNotifications()
    }

    private fun setupRecyclerView() {
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.notificationRecyclerView.adapter = adapter

        binding.sortNotificationTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTab = tab?.position ?: 0
                observeNotifications()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onNotificationClick(notification: Notification) {
        Log.d("NotificationFragment", "Notification clicked: $notification")
        if (!notification.read) {
            viewModel.updateNotificationReadStatus(notification.id, true)
        }
    }

    private fun observeNotifications() {
        viewModel.notifications.observe(viewLifecycleOwner, Observer { notifications ->
            val filteredNotifications = when (selectedTab) {
                0 -> notifications
                1 -> notifications.filter { it.read }
                2 -> notifications.filter { !it.read }
                else -> notifications
            }
            Log.d("NotificationFragment", "Filtered notifications: $filteredNotifications")
            (binding.notificationRecyclerView.adapter as NotificationAdapter).submitList(filteredNotifications)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
