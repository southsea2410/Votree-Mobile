package com.example.votree

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.admin.activities.TipListActivity

class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Make a button to go to Tip Management Activity. Can be deleted if you want.
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val intent = Intent(this, TipListActivity::class.java)
            startActivity(intent)
        }
    }
}
