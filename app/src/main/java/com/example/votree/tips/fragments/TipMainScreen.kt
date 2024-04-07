package com.example.votree.tips.fragments

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.service.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.databinding.FragmentTipMainScreenBinding
import com.example.votree.tips.adapters.TipAdapter
import com.example.votree.tips.adapters.TipCarouselAdapter
import com.example.votree.tips.view_models.TipsViewModel
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.HeroCarouselStrategy

class TipMainScreen : Fragment() {
    private val viewModel: TipsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTipMainScreenBinding.inflate(inflater, container, false)

        // Setup Carousel adapter
        TipsViewModel().queryTopTips()
        val carouselAdapter = TipCarouselAdapter()
        val carouselRecyclerView = binding.carouselRecyclerView
        carouselRecyclerView.layoutManager = CarouselLayoutManager(HeroCarouselStrategy())
        carouselRecyclerView.adapter = carouselAdapter
        carouselRecyclerView.setHasFixedSize(true)
        val snapHelper = CarouselSnapHelper()
        snapHelper.attachToRecyclerView(carouselRecyclerView)


        // Setup adapter
        TipsViewModel().queryAllTips()
        val adapter = TipAdapter()
        val tipRecyclerView = binding.tipRecyclerView
        tipRecyclerView.adapter = adapter
        tipRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        // Observe the tip list
        viewModel.topTipList.observe(viewLifecycleOwner) { topTipList ->
            carouselAdapter.submitList(topTipList)
        }
        viewModel.tipList.observe(viewLifecycleOwner) { tipList ->
            adapter.submitList(tipList)
        }

        // FAB setup
        binding.fabNavWriteTipAction.setOnClickListener{
            val intent = Intent(requireContext(), WriteTip::class.java)
            startActivity(intent)
        }

        return binding.root
    }
}