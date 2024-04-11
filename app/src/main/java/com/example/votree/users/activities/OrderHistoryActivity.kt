package com.example.votree.users.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.databinding.ActivityOrderHistoryBinding
import com.example.votree.products.view_models.OrderHistoryViewModel
import com.example.votree.users.adapters.OrderItemAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class OrderHistoryActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityOrderHistoryBinding
    private val viewModel: OrderHistoryViewModel by viewModels()
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = OrderItemAdapter(emptyList(), this) // Initially empty list
        binding.orderHistoryRv.layoutManager = LinearLayoutManager(this)
        binding.orderHistoryRv.adapter = adapter

        viewModel.transactions.observe(this) { transactions ->
            // Update the adapter with new data
            (binding.orderHistoryRv.adapter as OrderItemAdapter).apply {
                this.transactions = transactions ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
