package com.example.votree.users.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.FragmentStoreProfile2Binding
import com.example.votree.users.view_models.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class StoreProfile2 : Fragment() {
    private lateinit var binding: FragmentStoreProfile2Binding
    private val viewModel : ProfileViewModel by viewModels()
    val args : StoreProfile2Args by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoreProfile2Binding.inflate(inflater, container, false)
        setupData(binding)
        setupToolbar()
        return binding.root
    }
    private fun setupData(binding: FragmentStoreProfile2Binding) {
        val storeId = args.storeId
        Log.d("StoreProfile2", "Store ID: $storeId")
        binding.storeProfile2Toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        val formatter = SimpleDateFormat("dd/mm/yyyy", Locale.getDefault())

        // Query user from database
        viewModel.queryStore(storeId).observe(viewLifecycleOwner) { userStore ->
            binding.storeProfile2Name.text = userStore.store.storeName
            Glide.with(requireActivity())
                .load(userStore.user.avatar)
                .placeholder(R.drawable.img_placeholder)
                .into(binding.storeProfile2Avatar)
            addProfileField(binding, "Username", userStore.user.username)
            addProfileField(binding, "Email", userStore.user.email)
            addProfileField(binding, "Phone Number", userStore.user.phoneNumber)
            addProfileField(binding, "Store Name", userStore.store.storeName)
            addProfileField(binding, "Store Location", userStore.store.storeLocation)
            addProfileField(binding, "Join Date", formatter.format(userStore.user.createdAt))
        }
    }
    private fun addProfileField(binding: FragmentStoreProfile2Binding, label: String, value: String) {
        fun ProfileField(label: String, value: String) : LinearLayout {
            val baseLayout = layoutInflater.inflate(R.layout.profile_information_item, binding.storeProfile2InfoContainer, false)
            baseLayout.findViewById<TextView>(R.id.store_profile_2_field_label).text = label
            baseLayout.findViewById<TextView>(R.id.store_profile_2_field_value).text = value
            return baseLayout as LinearLayout
        }
        Log.d("StoreProfile2", "Adding profile field: $label - $value")
        binding.storeProfile2InfoContainer.addView(ProfileField(label, value))
    }

    private fun setupToolbar() {
        binding.storeProfile2Toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.storeProfile2Toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.storeProfile2_to_StoreReport -> {
                    val action = StoreProfile2Directions.actionStoreProfile2ToStoreReport(args.storeId)
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
        }
    }
}