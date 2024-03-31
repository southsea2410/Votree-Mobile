package com.example.votree

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.votree.databinding.ActivityMainBinding
import com.example.votree.users.activities.SignInActivity
import com.example.votree.utils.PermissionManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionManager: PermissionManager
    private lateinit var currentUser: com.google.firebase.auth.FirebaseUser

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Check if user is signed in (non-null) and update UI accordingly
        currentUser = firebaseAuth.currentUser!!
        if (currentUser == null) {
            // User is not signed in, redirect to SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish() // Prevent user from going back to MainActivity if they press back button
        } else {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            storeUserIdInSharedPreferences()

            val toolbar = binding.toolbar
            setSupportActionBar(toolbar)

            permissionManager = PermissionManager(this)
            permissionManager.checkPermissions()

            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.productList_f) as NavHostFragment
            val navController = navHostFragment.navController

            setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))

            findViewById<Button>(R.id.logoutButton).setOnClickListener {
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.productList_f)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun storeUserIdInSharedPreferences() {
        val sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("uid", currentUser.uid)
        editor.apply()
    }
}

