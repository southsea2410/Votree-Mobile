package com.example.votree.tips

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.ActivityTipDetailBinding
import com.example.votree.tips.models.ProductTip
import java.text.SimpleDateFormat
import java.util.Locale

class TipDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTipDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.tipDetailToolbar)
        binding.tipDetailToolbar.setNavigationOnClickListener {
            finish()
        }
        val tipData = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra("tipData", ProductTip::class.java)
        else
            intent.getParcelableExtra("tipData")

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
}