package com.example.votree.notifications.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.databinding.FragmentNotificationBinding
import com.example.votree.notifications.adapters.NotificationAdapter
import com.example.votree.notifications.view_models.NotificationViewModel

class NotificationFragment : Fragment() {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeNotifications()
    }

    private fun setupRecyclerView() {
        binding.notificationRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = NotificationAdapter()
        }
    }

    private fun observeNotifications() {
        viewModel.notifications.observe(viewLifecycleOwner, Observer { notifications ->
            (binding.notificationRecyclerView.adapter as NotificationAdapter).submitList(notifications)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}