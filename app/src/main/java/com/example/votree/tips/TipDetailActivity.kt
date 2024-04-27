package com.example.votree.tips

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.ActivityTipDetailBinding
import com.example.votree.tips.adapters.TipCommentAdapter
import com.example.votree.tips.models.ProductTip
import com.google.android.gms.ads.AdView
import com.example.votree.tips.view_models.TipsViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import java.text.SimpleDateFormat
import java.util.Locale

class TipDetailActivity : AppCompatActivity(), MaterialButtonToggleGroup.OnButtonCheckedListener {

    private val viewModel: TipsViewModel by viewModels()
    private val commentAdapter = TipCommentAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTipDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tipData = getTipData()
        if (tipData !== null){
            enableEdgeToEdge()
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            setupToolbar(binding)
            setupData(tipData, binding)
            setupComments(binding)
        }
        else finish()

        binding.tipDetailCommentRecyclerView.adapter = commentAdapter
        binding.tipDetailCommentRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        viewModel.queryComments(getTipData()!!)
        viewModel.commentList.observe(this){
            Log.d("TipDetailActivity", "Comment list updated")
            commentAdapter.submitList(it)
        }
    }

    private fun setupData(tipData: ProductTip, binding: ActivityTipDetailBinding){
        binding.tipDetailTitleTextView.text = tipData.title
        binding.tipDetailContentTextView.text = tipData.content
        val sdf = SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH)
        val formattedDate = tipData.createdAt?.let { sdf.format(it) + "  |  "} + tipData.vote_count.toString() + " votes"
        binding.tipDetailDateTextView.text = formattedDate

        val tipBtnGroup = findViewById<MaterialButtonToggleGroup>(R.id.tip_voting_system)
        viewModel.getIsUpvoted(tipData.id).observe(this){
            if (it !== null) {
                if (it) tipBtnGroup.check(R.id.tip_detail_upvote_btn)
                else tipBtnGroup.check(R.id.tip_detail_downvote_btn)
            }
            tipBtnGroup.addOnButtonCheckedListener(this)
        }

        Glide.with(this.applicationContext)
            .load(tipData.imageList[0])
            .placeholder(R.drawable.img_placeholder)
            .into(binding.tipDetailImageView)

        viewModel.getAuthor(tipData.userId).observe(this){
            if (it === null) return@observe
            binding.tipDetailAuthorTextView.text = it.fullName
            binding.tipDetailStoreTextView.text = it.storeName
        }

    }

    private fun setupComments(binding: ActivityTipDetailBinding){
        val comment = binding.tipDetailCommentEditText
        val commentBtn = binding.tipDetailSendCommentBtn
        commentBtn.setOnClickListener{
            val commentContent = comment.text.toString()
            if (commentContent.isNotEmpty()){
                viewModel.castComment(getTipData()!!, commentContent)
                comment.text?.clear()
                viewModel.queryComments(getTipData()!!)
            }
            else{
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
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
            intent.getParcelableExtra("tipData") as ProductTip?

        return tipData
    }

    override fun onButtonChecked(p0: MaterialButtonToggleGroup?, p1: Int, p2: Boolean) {
        when(p1){
            R.id.tip_detail_upvote_btn -> {
                if (p2) viewModel.castVote(getTipData()!!, true)
                else viewModel.unVoteTip(getTipData()!!, true)
                Log.d("TipDetailActivity", "Upvote button clicked" + p2.toString())

            }
            R.id.tip_detail_downvote_btn -> {
                if (p2) viewModel.castVote(getTipData()!!, false)
                else viewModel.unVoteTip(getTipData()!!, false)
                Log.d("TipDetailActivity", "Downvote button clicked" + p2.toString())
            }
        }
    }
}