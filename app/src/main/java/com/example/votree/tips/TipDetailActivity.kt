package com.example.votree.tips

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.ActivityTipDetailBinding
import com.example.votree.tips.models.ProductTip
import com.google.android.gms.ads.AdView
import java.text.SimpleDateFormat
import java.util.Locale

class TipDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTipDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding)

        val tipData = getTipData()

        if (tipData !== null){
            enableEdgeToEdge()
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            binding.tipDetailTitleTextView.text = tipData.title
            binding.tipDetailContentTextView.text = tipData.content
            val sdf = SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH)
            val formattedDate = tipData.createdAt?.let { sdf.format(it) + "  |  "} + tipData.vote.toString() + " votes"
            binding.tipDetailDateTextView.text = formattedDate

            Glide.with(this.applicationContext)
                .load(tipData.imageList[0])
                .placeholder(R.drawable.img_placeholder)
                .into(binding.tipDetailImageView)
        }
        else {
            binding.tipDetailTitleTextView.text = "No data"
        }

    }

    private fun setupToolbar(binding: ActivityTipDetailBinding){
        setSupportActionBar(binding.tipDetailToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.tipDetailToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tip_detail_overflow, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Start Report tip activity
        return when (item.itemId) {
            R.id.tip_detail_action_report -> {
                val newIntent = Intent(this, TipReportActivity::class.java)

                val tipData = getTipData()
                newIntent.putExtra("tipData", tipData)

                startActivity(newIntent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getTipData() : ProductTip?{
        val tipData = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra("tipData", ProductTip::class.java)
        else
            intent.getParcelableExtra("tipData")

        return tipData
    }
}