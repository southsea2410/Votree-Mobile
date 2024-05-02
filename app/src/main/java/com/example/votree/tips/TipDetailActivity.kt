package com.example.votree.tips

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.example.votree.tips.view_models.CommentViewModel
import com.example.votree.tips.view_models.TipsViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import java.text.SimpleDateFormat
import java.util.Locale

class TipDetailActivity : AppCompatActivity(), MaterialButtonToggleGroup.OnButtonCheckedListener {
    private val viewModel: TipsViewModel by viewModels()
    private val commentViewModel: CommentViewModel by viewModels()
    private val commentAdapter = TipCommentAdapter()
    private var textToSpeechHelper: TextToSpeechHelper? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTipDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tipData = getTipData()
        if (tipData !== null){
            enableEdgeToEdge()
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, 0)
                insets
            }
            setupToolbar(binding)
            setupData(tipData, binding)
            setupComments(binding)
        }
        else finish()

        binding.tipDetailCommentRecyclerView.adapter = commentAdapter
        binding.tipDetailCommentRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        commentViewModel.queryComments(getTipData()!!)
        commentViewModel.commentList.observe(this){
            Log.d("TipDetailActivity", "Comment list updated")
            commentAdapter.submitList(it)
        }

        textToSpeechHelper = TextToSpeechHelper(this)

        val titleText = binding.tipDetailTitleTextView
        val authorText = binding.tipDetailAuthorTextView
        val detailContent = binding.tipDetailContentTextView
        val icSpeaker = binding.textToSpeechButton

        icSpeaker.setOnClickListener {
            if (isPlaying) {
                icSpeaker.setImageResource(R.drawable.ic_speaker_idle)
                textToSpeechHelper?.stop()
            }
            else {
                icSpeaker.setImageResource(R.drawable.ic_speaker_playing)
                textToSpeechHelper?.speak(titleText.text.toString(), authorText.text.toString(), detailContent.text.toString())
            }
            isPlaying = !isPlaying
        }
    }

    override fun onPause() {
        super.onPause()

        textToSpeechHelper?.shutdown()
        // TODO: Implement store profile
//        binding.tipDetailAuthorStoreLayout.setOnClickListener{
//            val intent = Intent(this, StoreProfile2::class.java)
//            if (tipData == null) return@setOnClickListener
//            intent.putExtra("userId", tipData.userId)
//            startActivity(intent)
//        }
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
            if (it.avatar.isNotBlank())
            Glide.with(this.applicationContext)
                .load(it.avatar)
                .placeholder(R.drawable.img_placeholder)
                .into(binding.tipDetailAvatarImageView)
            else binding.tipDetailAvatarImageView.visibility = View.GONE
        }

    }

    private fun setupComments(binding: ActivityTipDetailBinding){
        val comment = binding.tipDetailCommentEditText
        val commentBtn = binding.tipDetailSendCommentBtn
        commentBtn.setOnClickListener{
            val commentContent = comment.text.toString()
            if (commentContent.isNotBlank()){
                commentViewModel.castComment(getTipData()!!, commentContent)
                comment.text?.clear()
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