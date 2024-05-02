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

class NotificationFragment : Fragment(), NotificationAdapter.OnNotificationClickListener {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels()
    private var adapter = NotificationAdapter(this)

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
        binding.notificationRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = NotificationAdapter(this@NotificationFragment)
        }
    }

    override fun onNotificationClick(notification: Notification) {
        Log.d("NotificationFragment", "Notification clicked: $notification")
        if (!notification.read) {
            viewModel.updateNotificationReadStatus(notification.id, true)
        }
    }

    private fun observeNotifications() {
        viewModel.notifications.observe(viewLifecycleOwner, Observer { notifications ->
            Log.d("NotificationFragment", "Notifications: $notifications")
            (binding.notificationRecyclerView.adapter as NotificationAdapter).submitList(notifications)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}