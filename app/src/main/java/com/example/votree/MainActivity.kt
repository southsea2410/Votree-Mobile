package com.example.votree
//
//import android.os.Build
//import android.os.Bundle
//import androidx.annotation.RequiresApi
//import androidx.appcompat.app.AppCompatActivity
//import androidx.navigation.findNavController
//import androidx.navigation.fragment.NavHostFragment
//import androidx.navigation.ui.AppBarConfiguration
//import androidx.navigation.ui.setupActionBarWithNavController
//import com.example.votree.databinding.ActivityMainBinding
//import com.example.votree.utils.PermissionManager
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//    private lateinit var permissionManager: PermissionManager
//
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val toolbar = binding.toolbar
//        setSupportActionBar(toolbar)
//
//        permissionManager = PermissionManager(this)
//        permissionManager.checkPermissions()
//
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.productList_f) as NavHostFragment
//        val navController = navHostFragment.navController
//
//        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
//    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.productList_f)
//        return navController.navigateUp() || super.onSupportNavigateUp()
//    }
//}

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.admin.activities.TipListActivity
import com.example.votree.users.activities.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val intent = Intent(this, TipListActivity::class.java)
            startActivity(intent)
        }
    }
//    private lateinit var auth: FirebaseAuth
//
//    private lateinit var firebaseAuth: FirebaseAuth
//    private lateinit var etToken: EditText
//
//    @SuppressLint("MissingInflatedId")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Initialize Firebase Auth
//        firebaseAuth = FirebaseAuth.getInstance()
//
//        // Check if user is signed in (non-null) and update UI accordingly
//        val currentUser = firebaseAuth.currentUser
//        if (currentUser == null) {
//            // User is not signed in, redirect to SignInActivity
//            val intent = Intent(this, SignInActivity::class.java)
//            startActivity(intent)
//            finish() // Prevent user from going back to MainActivity if they press back button
//        } else {
//            setContentView(R.layout.activity_main)
//
//            val email = intent.getStringExtra("email")
//            val displayName = intent.getStringExtra("name")
//
//            findViewById<TextView>(R.id.userInfoTextView).text = "Email: $email\nName: $displayName"
//
//            findViewById<Button>(R.id.logoutButton).setOnClickListener {
//                val intent = Intent(this, SignInActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
//
//            etToken = findViewById(R.id.etToken)
//            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val token = task.result
//                    etToken.setText(token)
//                } else {
//                    println("Fetching FCM registration token failed")
//                }
//            }
//        }
//    }
}
