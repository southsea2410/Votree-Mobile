package com.example.votree.tips

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.databinding.FragmentTipMainScreenBinding
import com.example.votree.tips.adapters.TipAdapter
import com.example.votree.tips.adapters.TipCarouselAdapter
import com.example.votree.tips.view_models.TipsViewModel
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.HeroCarouselStrategy

class TipMainScreenFragment : Fragment() {
    private val viewModel: TipsViewModel by viewModels()
    private val adapter = TipAdapter()
    private val carouselAdapter = TipCarouselAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTipMainScreenBinding.inflate(inflater, container, false)
        setupRecyclerView(binding)
        setupCarousel(binding)
        setupFabButton(binding)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.topTipList.observe(viewLifecycleOwner) { topTipList ->
            carouselAdapter.submitList(topTipList)
        }
        viewModel.tipList.observe(viewLifecycleOwner) { tipList ->
            adapter.submitList(tipList)
        }
    }

    private fun setupRecyclerView(binding: FragmentTipMainScreenBinding) {
        binding.tipRecyclerView.adapter = adapter
        binding.tipRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun setupCarousel(binding: FragmentTipMainScreenBinding) {
        val carouselRecyclerView = binding.carouselRecyclerView
        carouselRecyclerView.layoutManager = CarouselLayoutManager(HeroCarouselStrategy())
        carouselRecyclerView.adapter = carouselAdapter
        carouselRecyclerView.setHasFixedSize(true)
        val snapHelper = CarouselSnapHelper()
        snapHelper.attachToRecyclerView(carouselRecyclerView)
    }

    private fun setupFabButton(binding: FragmentTipMainScreenBinding) {
        binding.fabNavWriteTipAction.setOnClickListener {
            val intent = Intent(requireContext(), WriteTipActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.queryAllTips()
        viewModel.queryTopTips()
    }
}