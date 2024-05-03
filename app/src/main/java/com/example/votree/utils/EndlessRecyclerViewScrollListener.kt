package com.example.votree.utils

import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EndlessRecyclerViewScrollListener(
    private val layoutManager: GridLayoutManager,
    private val onLoadMore: () -> Unit
) : RecyclerView.OnScrollListener() {

    private var loading = true
    private var previousTotal = 0
    private var visibleThreshold = 5

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (dy <= 0) {
            Log.d("EndlessScroll", "dy <= 0")
            return
        } else {
            Log.d("EndlessScroll", "dy > 0")
            val visibleItemCount = recyclerView.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

            if (loading && totalItemCount > previousTotal) {
                loading = false
                previousTotal = totalItemCount
                Log.d("EndlessScroll", "loading = false")
            }

            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                Log.d("EndlessScroll", "loading = true")
                onLoadMore()
                loading = true
            }
        }
    }
}


