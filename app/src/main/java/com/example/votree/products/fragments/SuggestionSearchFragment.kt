package com.example.votree.products.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.MainActivity
import com.example.votree.R
import com.example.votree.databinding.FragmentSuggestionSearchBinding
import com.example.votree.products.adapters.SuggestionSearchAdapter
import com.example.votree.products.view_models.ProductViewModel

class SuggestionSearchFragment : Fragment(), MainActivity.SearchQueryListener{
    private lateinit var binding: FragmentSuggestionSearchBinding
    private lateinit var suggestionAdapter: SuggestionSearchAdapter
    private lateinit var productViewModel: ProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSuggestionSearchBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupViewModel()
        setQuerySearchListener()
    }

    private fun setupRecyclerView() {
        suggestionAdapter = SuggestionSearchAdapter(mutableListOf()) { suggestion ->
            Log.d("SuggestionSearchFragment", "Suggestion clicked: $suggestion")
        }
        binding.suggestionsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.suggestionsRecyclerView.adapter = suggestionAdapter
    }

    private fun setupViewModel() {
        productViewModel = ViewModelProvider(requireActivity()).get(ProductViewModel::class.java)
    }

    private fun setQuerySearchListener() {
        suggestionAdapter.setOnSuggestionClickListener(object : SuggestionSearchAdapter.onSuggestionClickListener {
            override fun onSuggestionClick(suggestion: String) {
                onSearchQuerySubmitted(suggestion)
            }
        })
    }

    override fun onQueryTextChange(newText: String) {
        onSearchQueryChanged(newText)
    }

    private fun onSearchQueryChanged(query: String) {
        productViewModel.getDebouncedProductSuggestions(query).observe(viewLifecycleOwner) { suggestions ->
            Log.d("SuggestionSearchFragment", "Suggestions: $suggestions")
            suggestionAdapter.updateSuggestions(suggestions)
        }
    }

    override fun onQueryTextSubmit(query: String) {
        onSearchQuerySubmitted(query)
    }

    private fun onSearchQuerySubmitted(query: String) {
        productViewModel.getCachedSuggestions(query).observe(viewLifecycleOwner) { suggestions ->
            val navController = findNavController()
            if(navController.currentDestination?.id != R.id.productList){
                val action = SuggestionSearchFragmentDirections.actionSuggestionSearchFragmentToProductList(
                    query)
                findNavController().navigate(action)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity)
            context.setSearchQueryListener(this)
    }

    override fun onDetach() {
        super.onDetach()
        if (activity is MainActivity)
            (activity as MainActivity).setSearchQueryListener(null)
    }
}

